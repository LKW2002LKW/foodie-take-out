<template>
  <div class="category-manage-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">分类管理</h1>
        <p class="page-subtitle">管理菜品和套餐的分类，合理的分类有助于顾客点餐</p>
      </div>
      <el-button type="primary" icon="Plus" size="large" @click="handleCreate">新增分类</el-button>
    </div>

    <!-- 筛选区域 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline" size="default">
        <el-form-item label="分类名称">
          <el-input v-model="queryParams.name" placeholder="输入分类名称搜索" clearable style="width: 200px;" @keyup.enter="handleQuery"/>
        </el-form-item>
        <el-form-item label="分类类型">
          <el-select v-model="queryParams.type" placeholder="全部类型" clearable style="width: 150px;" @change="handleQuery">
            <el-option label="菜品分类" :value="1" />
            <el-option label="套餐分类" :value="2" />
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
      <el-table :data="categoryList" style="width: 100%" row-key="id" stripe highlight-current-row>
        <el-table-column prop="name" label="分类名称" min-width="150" />
        
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.type === 1" type="success" size="small" effect="light">菜品分类</el-tag>
            <el-tag v-else-if="row.type === 2" type="warning" size="small" effect="light">套餐分类</el-tag>
            <el-tag v-else type="info" size="small">未知</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="sort" label="排序" width="100" align="center" sortable />

        <el-table-column prop="updateTime" label="更新时间" width="180" align="center" />

        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
             <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                :loading="row.statusLoading"
                @change="handleStatusChange(row)"
              />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">编辑</el-button>
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

    <!-- 新增/修改弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      append-to-body
      destroy-on-close
      class="category-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="10" show-word-limit />
        </el-form-item>
        
        <el-form-item label="分类类型" prop="type">
          <el-radio-group v-model="form.type" :disabled="isEdit">
            <el-radio :label="1" border>菜品分类</el-radio>
            <el-radio :label="2" border>套餐分类</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" controls-position="right" style="width: 100%;" />
          <div class="form-tip">数值越小越靠前</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue';
import categoryApi from '@/api/category';

// --- State ---
const loading = ref(false);
const total = ref(0);
const categoryList = ref([]);
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  name: '',
  type: ''
});

const dialogVisible = ref(false);
const dialogTitle = ref('');
const isEdit = ref(false);
const submitLoading = ref(false);
const formRef = ref(null);

const form = reactive({
  id: undefined,
  name: '',
  type: 1,
  sort: 0
});

const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择分类类型', trigger: 'change' }
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ]
};

// --- Methods ---

// 1. 查询列表
const getList = async () => {
  loading.value = true;
  try {
    const res = await categoryApi.getCategoryPage(queryParams);
    if (res && res.data) {
      categoryList.value = res.data.records || [];
      total.value = res.data.total || 0;
    }
  } catch (error) {
    console.error('Fetch error:', error);
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
  queryParams.type = '';
  handleQuery();
};

// 2. 分页操作
const handleSizeChange = (val) => {
  queryParams.pageSize = val;
  getList();
};

const handleCurrentChange = (val) => {
  queryParams.page = val;
  getList();
};

// 3. 新增/编辑
const resetForm = () => {
  form.id = undefined;
  form.name = '';
  form.type = 1;
  form.sort = 0;
  if (formRef.value) formRef.value.resetFields();
};

const handleCreate = () => {
  resetForm();
  isEdit.value = false;
  dialogTitle.value = '新增分类';
  dialogVisible.value = true;
};

const handleUpdate = async (row) => {
  resetForm();
  isEdit.value = true;
  dialogTitle.value = '编辑分类';
  // 可以调用详情接口，也可以直接使用列表行数据回显
  // 这里为了稳妥演示调用详情接口
  try {
     const res = await categoryApi.getCategoryById(row.id);
     if (res && res.data) {
       Object.assign(form, res.data);
     } else {
        // Fallback
        Object.assign(form, row);
     }
     dialogVisible.value = true;
  } catch (error) {
    ElMessage.error('获取详情失败');
  }
};

const submitForm = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true;
      try {
        if (isEdit.value) {
          await categoryApi.updateCategory(form);
          ElMessage.success('更新成功');
        } else {
          await categoryApi.addCategory(form);
          ElMessage.success('新增成功');
        }
        dialogVisible.value = false;
        getList();
      } catch (error) {
        // 错误提示通常由 request.js 拦截器统一处理，也可在这里单独处理
      } finally {
        submitLoading.value = false;
      }
    }
  });
};

// 4. 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确认删除分类 "${row.name}" 吗？删除前请确保该分类下无关联菜品。`,
    '警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await categoryApi.deleteCategory(row.id);
      ElMessage.success('删除成功');
      getList(); // 刷新列表
    } catch (error) {
      // 捕获 API 抛出的关联菜品错误（假设后端返回 400 或特定 code）
      console.error(error);
    }
  }).catch(() => {});
};

// 5. 状态切换
const handleStatusChange = async (row) => {
  const originalStatus = row.status === 1 ? 0 : 1;
  row.statusLoading = true; // 添加一个临时 loading 状态避免极速连点
  try {
    await categoryApi.updateCategoryStatus(row.id, row.status);
    ElMessage.success('状态更新成功');
  } catch (error) {
    row.status = originalStatus; // 失败回滚
    // Error msg handled by interceptor
  } finally {
    row.statusLoading = false;
  }
};

onMounted(() => {
  getList();
});
</script>

<style scoped>
.category-manage-page {
  padding: 0;
}
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
.filter-card {
  margin-bottom: 20px;
  border-radius: 8px;
}
.list-card {
  border-radius: 8px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: normal;
  margin-top: 4px;
}

/* Element UI Overrides for smoother look */
:deep(.el-table .el-table__cell) {
  padding: 12px 0;
}
:deep(.el-button--link) {
  padding: 4px 8px;
}
</style>

