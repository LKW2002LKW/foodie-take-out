<template>
  <div class="product">
    <!-- 头部欢迎区域 -->
    <header class="product__header">
      <div class="product__header-info">
        <h1 class="product__title">菜品管理</h1>
        <p class="product__subtitle">配置菜品信息、售卖状态及口味定制</p>
      </div>
      <div class="product__header-actions">
        <el-button 
          v-if="selection.length"
          type="danger" 
          plain 
          icon="Delete" 
          @click="handleBatchDelete"
        >
          批量删除 ({{ selection.length }})
        </el-button>
        <el-button 
          type="primary" 
          icon="Plus" 
          size="large" 
          class="product__add-btn"
          @click="handleCreate"
        >
          新增菜品
        </el-button>
      </div>
    </header>

    <!-- 筛选过滤区域 -->
    <el-card shadow="never" class="product__filter-card">
      <el-form :inline="true" :model="queryParams" class="product__filter-form">
        <el-form-item label="菜品名称">
          <el-input 
            v-model="queryParams.name" 
            placeholder="搜索菜品名称" 
            clearable 
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select 
            v-model="queryParams.categoryId" 
            placeholder="全部分类" 
            clearable 
            style="width: 160px"
            @change="handleQuery"
          >
            <el-option
              v-for="item in categoryList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="queryParams.status" 
            placeholder="全部状态" 
            clearable 
            style="width: 120px"
            @change="handleQuery"
          >
            <el-option label="起售中" :value="1" />
            <el-option label="已停售" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表数据展示 -->
    <el-card shadow="never" class="product__list-card">
      <el-table 
        v-loading="loading" 
        :data="dishList" 
        class="product__table"
        @selection-change="selection = $event"
      >
        <el-table-column type="selection" width="55" align="center" />
        
        <el-table-column label="菜品图片" width="120" align="center">
          <template #default="{ row }">
            <el-image 
              class="product__img"
              :src="row.image" 
              :preview-src-list="[row.image]" 
              fit="cover"
              preview-teleported
            >
              <template #error>
                <div class="product__img-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>

        <el-table-column label="菜品信息" min-width="200">
          <template #default="{ row }">
            <div class="product__info">
              <span class="product__name">{{ row.name }}</span>
              <span class="product__desc" v-if="row.description">{{ row.description }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="所属分类" width="150" align="center">
          <template #default="{ row }">
            <el-tag effect="plain" class="product__tag">{{ row.categoryName || '默认分类' }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="销售价格" width="150" align="right">
          <template #default="{ row }">
            <span class="product__price">￥{{ Number(row.price).toFixed(2) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="售卖状态" width="120" align="center">
          <template #default="{ row }">
            <div :class="['product__status', row.status === 1 ? 'product__status--active' : 'product__status--inactive']">
              <span class="product__status-dot"></span>
              {{ row.status === 1 ? '正在售卖' : '已下架' }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">修改</el-button>
            <el-button 
              link 
              :type="row.status === 1 ? 'warning' : 'success'" 
              :icon="row.status === 1 ? 'Bottom' : 'Top'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '停售' : '起售' }}
            </el-button>
            <el-divider direction="vertical" />
            <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="未找到符合条件的菜品" />
        </template>
      </el-table>

      <!-- 分页控制 -->
      <div class="product__pagination">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          background
          @size-change="fetchDishList"
          @current-change="fetchDishList"
        />
      </div>
    </el-card>

    <!-- 编辑/新增弹窗组件 -->
    <DishForm 
      v-model:visible="formVisible"
      :is-edit="isEdit"
      :dish-id="currentDishId"
      @success="fetchDishList"
    />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { Search, Refresh, Plus, Delete, Edit, Picture, Top, Bottom } from '@element-plus/icons-vue'
import DishForm from '@/components/DishForm.vue'
import { useProduct } from '@/composables/useProduct'

/**
 * 菜品列表管理
 */

const {
  loading,
  dishList,
  total,
  categoryList,
  selection,
  queryParams,
  formVisible,
  isEdit,
  currentDishId,
  fetchDishList,
  fetchCategoryList,
  handleQuery,
  resetQuery,
  handleCreate,
  handleUpdate,
  handleDelete,
  handleBatchDelete,
  toggleStatus
} = useProduct()

onMounted(() => {
  fetchCategoryList()
  fetchDishList()
})
</script>

<style scoped>
.product {
  padding: 0;
}

/* Header */
.product__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.product__title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px;
}

.product__subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.product__header-actions {
  display: flex;
  gap: 12px;
}

.product__add-btn {
  border-radius: 10px;
  font-weight: 600;
}

/* Filter */
.product__filter-card {
  margin-bottom: 24px;
  border-radius: 12px;
  border: none;
}

.product__filter-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #475569;
}

/* List */
.product__list-card {
  border-radius: 12px;
  border: none;
}

.product__img {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.product__img-placeholder {
  width: 64px;
  height: 64px;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  border-radius: 8px;
}

.product__info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product__name {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.product__desc {
  font-size: 12px;
  color: #64748b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 250px;
}

.product__price {
  font-family: 'Inter', monospace;
  font-weight: 700;
  color: #ef4444;
  font-size: 16px;
}

.product__tag {
  border-radius: 6px;
}

/* Status Indicator */
.product__status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 500;
}

.product__status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.product__status--active { color: #10b981; }
.product__status--active .product__status-dot { background: #10b981; box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.2); }

.product__status--inactive { color: #94a3b8; }
.product__status--inactive .product__status-dot { background: #94a3b8; }

.product__pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-table__row) {
  height: 80px;
}
</style>
