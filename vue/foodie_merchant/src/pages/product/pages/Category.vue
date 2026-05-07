<template>
  <div class="category">
    <!-- 分类管理页作为装配层，筛选、弹窗与提交动作统一来自 useCategoryPage。 -->
    <div class="category__header">
      <div class="category__header-info">
        <h1 class="category__title">分类管理</h1>
        <p class="category__subtitle">管理菜品和套餐的分类，合理的分类有助于顾客点餐</p>
      </div>
      <el-button
        type="primary"
        icon="Plus"
        size="large"
        class="category__add-btn"
        @click="handleCreate"
      >
        新增分类
      </el-button>
    </div>

    <el-card shadow="never" class="category__filter-card">
      <el-form :inline="true" :model="queryParams" class="category__filter-form">
        <el-form-item label="分类名称">
          <el-input
            v-model="queryParams.name"
            placeholder="搜索分类名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="分类类型">
          <el-select v-model="queryParams.type" placeholder="全部类型" clearable @change="handleQuery">
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

    <el-card shadow="never" class="category__list-card">
      <el-table
        v-loading="loading"
        :data="categoryList"
        class="category__table"
        border
        stripe
      >
        <el-table-column label="分类名称" min-width="150">
          <template #default="{ row }">
            <span class="category__name">{{ row.name }}</span>
          </template>
        </el-table-column>

        <el-table-column label="分类类型" width="150" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.type === 1 ? 'success' : 'warning'"
              effect="light"
              round
            >
              {{ row.type === 1 ? '菜品分类' : '套餐分类' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="sort" label="排序权重" width="120" align="center" sortable />

        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              inline-prompt
              active-text="启用"
              inactive-text="禁用"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>

        <el-table-column label="最后更新" width="200" align="center">
          <template #default="{ row }">
            <span class="category__time">{{ row.updateTime }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无分类数据" />
        </template>
      </el-table>

      <div class="category__pagination">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          background
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '新增分类'"
      width="480px"
      class="category__dialog"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        label-position="top"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="10" show-word-limit />
        </el-form-item>

        <el-form-item label="分类类型" prop="type">
          <el-radio-group v-model="form.type" :disabled="isEdit">
            <el-radio-button :label="1">菜品分类</el-radio-button>
            <el-radio-button :label="2">套餐分类</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="排序权重" prop="sort">
          <el-input-number
            v-model="form.sort"
            :min="0"
            :max="99"
            controls-position="right"
            style="width: 100%"
          />
          <p class="category__form-tip">提示：数值越小，在点餐页面显示越靠前</p>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="category__dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitForm">
            保存提交
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { useCategoryPage } from '@/composables/business/useCategoryPage'

const {
  categoryList,
  dialogVisible,
  fetchList,
  form,
  formRef,
  handleCreate,
  handleDelete,
  handleQuery,
  handleStatusChange,
  handleUpdate,
  isEdit,
  loading,
  queryParams,
  resetQuery,
  rules,
  submitForm,
  submitLoading,
  total,
} = useCategoryPage()
</script>

<style scoped>
.category {
  padding: 0;
}
.category__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.category__title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px;
}
.category__subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}
.category__add-btn {
  border-radius: 10px;
  font-weight: 600;
}
.category__filter-card {
  margin-bottom: 24px;
  border-radius: 12px;
  border: none;
  background: #fff;
}
.category__filter-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #475569;
}
.category__list-card {
  border-radius: 12px;
  border: none;
  background: #fff;
}
.category__table {
  border-radius: 8px;
  overflow: hidden;
}
.category__name {
  font-weight: 600;
  color: #1e293b;
}
.category__time {
  font-size: 13px;
  color: #64748b;
}
.category__pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}
.category__form-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 8px;
}
.category__dialog :deep(.el-dialog__header) {
  margin-right: 0;
  padding-bottom: 20px;
  border-bottom: 1px solid #f1f5f9;
}
.category__dialog :deep(.el-dialog__title) {
  font-weight: 700;
  color: #1e293b;
}
.category__dialog :deep(.el-form-item__label) {
  font-weight: 600;
  color: #475569;
}
.category__dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
