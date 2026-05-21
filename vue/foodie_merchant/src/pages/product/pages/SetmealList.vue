<template>
  <div class="setmeal-list-page">
    <!-- 套餐列表页作为装配层，列表查询、上下架与表单弹窗逻辑统一来自 useSetmealListPage。 -->
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

    <el-card shadow="hover" class="list-card" v-loading="loading">
      <el-table
        :data="setmealList"
        style="width: 100%"
        stripe
        @selection-change="handleSelectionChange"
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

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <SetmealForm
      v-model:visible="formVisible"
      :is-edit="isEdit"
      :setmeal-id="currentSetmealId"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup>
import { Picture } from '@element-plus/icons-vue'
import SetmealForm from '@/pages/product/components/SetmealForm.vue'
import { useSetmealListPage } from '@/composables/business/useSetmealListPage'

const {
  categoryList,
  currentSetmealId,
  formVisible,
  handleBatchDelete,
  handleCreate,
  handleCurrentChange,
  handleDelete,
  handleFormSuccess,
  handleQuery,
  handleSelectionChange,
  handleSizeChange,
  handleStatusChange,
  handleUpdate,
  isEdit,
  loading,
  queryParams,
  resetQuery,
  selection,
  setmealList,
  total,
} = useSetmealListPage()
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
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
}
</style>
