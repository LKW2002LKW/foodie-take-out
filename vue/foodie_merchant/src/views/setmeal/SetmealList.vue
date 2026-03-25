<template>
  <div class="setmeal-list-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">套餐管理</h1>
        <p class="page-subtitle">管理所有套餐，组合销售提升客单价</p>
      </div>
      <div>
        <el-button type="danger" plain icon="Delete" :disabled="selection.length === 0" @click="handleBatchDelete">
          批量删除
        </el-button>
        <el-button type="primary" icon="Plus" size="large" @click="handleCreate">新增套餐</el-button>
      </div>
    </div>

    <!-- 筛选区域 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="套餐名称">
          <el-input v-model="queryParams.name" placeholder="请输入套餐名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="套餐分类">
          <el-select v-model="queryParams.categoryId" placeholder="全部分类" clearable style="width: 150px" @change="handleQuery">
            <el-option
              v-for="item in categoryList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="售卖状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 120px" @change="handleQuery">
            <el-option label="起售" :value="1" />
            <el-option label="停售" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表区域 -->
    <el-card shadow="hover" class="list-card" v-loading="loading">
      <el-table 
        :data="setmealList" 
        style="width: 100%" 
        @selection-change="handleSelectionChange"
        stripe
      >
        <el-table-column type="selection" width="55" align="center" />
        
        <el-table-column label="图片" width="100" align="center">
          <template #default="{ row }">
            <el-image 
              style="width: 60px; height: 60px; border-radius: 4px;" 
              :src="row.image" 
              :preview-src-list="[row.image]" 
              fit="cover"
              preview-teleported
            >
               <template #error>
                <div class="image-slot">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>

        <el-table-column prop="name" label="套餐名称" min-width="150" show-overflow-tooltip />
        
        <el-table-column prop="categoryName" label="分类" width="120" align="center">
             <template #default="{ row }">
                 <el-tag effect="plain" type="warning">{{ row.categoryName || '未分类' }}</el-tag>
             </template>
        </el-table-column>

        <el-table-column prop="price" label="售价" width="120" align="center">
            <template #default="{ row }">
                <span class="price-text">￥{{ Number(row.price).toFixed(2) }}</span>
            </template>
        </el-table-column>

        <el-table-column prop="status" label="售卖状态" width="120" align="center">
          <template #default="{ row }">
             <div class="status-indicator">
                 <span v-if="row.status === 1" class="status-dot success"></span>
                 <span v-else class="status-dot danger"></span>
                 {{ row.status === 1 ? '起售' : '停售' }}
             </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="updateTime" label="更新时间" width="180" align="center" show-overflow-tooltip />

        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">修改</el-button>
            <el-button 
                link 
                :type="row.status === 1 ? 'warning' : 'success'" 
                @click="handleStatusChange(row)"
            >
                {{ row.status === 1 ? '停售' : '起售' }}
            </el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </el-card>

    <!-- 弹窗表单 -->
    <SetmealForm 
      v-model:visible="formVisible"
      :is-edit="isEdit"
      :setmeal-id="currentSetmealId"
      @success="handleFormSuccess"
    />

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Refresh, Plus, Delete, Edit, Picture } from '@element-plus/icons-vue';
import SetmealForm from './SetmealForm.vue';
import setmealApi from '@/api/setmeal';
import categoryApi from '@/api/category';

// --- State ---
const loading = ref(false);
const setmealList = ref([]);
const total = ref(0);
const categoryList = ref([]);
const selection = ref([]);

const formVisible = ref(false);
const isEdit = ref(false);
const currentSetmealId = ref(undefined);

const queryParams = reactive({
  page: 1,
  pageSize: 10,
  name: '',
  categoryId: undefined,
  status: undefined
});

// --- Lifecycle ---
onMounted(() => {
  getCategoryList();
  getList();
});

// --- Methods ---

const getCategoryList = async () => {
    try {
        const res = await categoryApi.getCategoryList(2); // 2: Setmeal
        categoryList.value = res.data || [];
    } catch (e) { console.error(e); }
};

const getList = async () => {
  loading.value = true;
  try {
    const res = await setmealApi.getSetmealPage(queryParams);
    if (res.data) {
        setmealList.value = res.data.records || [];
        total.value = res.data.total || 0;
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.page = 1;
  getList();
};

const resetQuery = () => {
  queryParams.name = '';
  queryParams.categoryId = undefined;
  queryParams.status = undefined;
  handleQuery();
};

const handleSizeChange = (val) => {
  queryParams.pageSize = val;
  getList();
};

const handleCurrentChange = (val) => {
  queryParams.page = val;
  getList();
};

const handleSelectionChange = (val) => {
    selection.value = val;
};

// Actions
const handleCreate = () => {
    isEdit.value = false;
    currentSetmealId.value = undefined;
    formVisible.value = true;
};

const handleUpdate = (row) => {
    isEdit.value = true;
    currentSetmealId.value = row.id;
    formVisible.value = true;
};

const handleFormSuccess = () => {
    getList();
};

const handleDelete = (row) => {
    if (row.status === 1) {
        ElMessage.warning('起售中的套餐禁止删除，请先停售！');
        return;
    }
    ElMessageBox.confirm(`确认删除套餐 "${row.name}" 吗?`, '警告', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
    }).then(async () => {
        try {
            await setmealApi.deleteSetmeal(row.id);
            ElMessage.success('删除成功');
            getList();
        } catch (e) {
            console.error(e);
        }
    }).catch(() => {});
};

const handleBatchDelete = () => {
    if (selection.value.length === 0) return;
    
    // Check if any selected item is active
    const hasActive = selection.value.some(item => item.status === 1);
    if (hasActive) {
        ElMessage.warning('选中的套餐中有正在起售的，无法删除！');
        return;
    }

    const ids = selection.value.map(item => item.id).join(',');
    
    ElMessageBox.confirm(`确认批量删除选中的 ${selection.value.length} 个套餐吗?`, '警告', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
    }).then(async () => {
        try {
            await setmealApi.deleteSetmealBatch(ids);
            ElMessage.success('批量删除成功');
            getList();
        } catch (e) {
            console.error(e);
        }
    }).catch(() => {});
};

const handleStatusChange = (row) => {
    const targetStatus = row.status === 1 ? 0 : 1;
    const actionText = targetStatus === 1 ? '起售' : '停售';
    
    ElMessageBox.confirm(`确认${actionText}套餐 "${row.name}" 吗?`, '提示', {
        type: targetStatus === 1 ? 'success' : 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
    }).then(async () => {
        try {
            await setmealApi.updateSetmealStatus(row.id, targetStatus);
            ElMessage.success(`${actionText}成功`);
            getList();
        } catch (e) {
            // Backend should return error message if dishes are inactive
            // utils/request.js typically handles showing error.msg via ElMessage
            // So we just log here
            console.error(e);
        }
    }).catch(() => {});
};

</script>

<style scoped>
.setmeal-list-page { padding: 0; }
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}
.page-subtitle {
  color: #909399;
  font-size: 14px;
  margin: 0;
}
.filter-card { margin-bottom: 20px; border-radius: 8px; }
.list-card { border-radius: 8px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }

.price-text {
    font-weight: bold;
    color: #f56c6c;
}

.status-indicator {
    display: flex;
    align-items: center;
    justify-content: center;
}
.status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    margin-right: 6px;
}
.status-dot.success { background-color: #67c23a; }
.status-dot.danger { background-color: #f56c6c; }

.image-slot {
    display: flex; justify-content: center; align-items: center;
    width: 100%; height: 100%;
    background: #f5f7fa; color: #909399;
}
</style>
