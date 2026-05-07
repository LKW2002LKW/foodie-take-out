<template>
  <div class="review-container">
    <!-- 评价管理页只负责列表与筛选渲染，预警和删除逻辑由 useReviewPage 托管。 -->
    <el-card>
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="评价列表" name="all" />
        <el-tab-pane label="差评预警" name="warning" />
      </el-tabs>

      <el-form v-if="activeTab === 'all'" :inline="true" :model="queryParams" class="search-form mt-20">
        <el-form-item label="商户ID">
          <el-input v-model="queryParams.merchantId" placeholder="商户ID" clearable />
        </el-form-item>
        <el-form-item label="评分">
          <el-select v-model="queryParams.rating" placeholder="全部" clearable style="width: 120px;">
            <el-option v-for="i in 5" :key="i" :label="i + '星'" :value="i" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border v-loading="loading" class="mt-20">
        <el-table-column prop="orderNumber" label="订单号" width="150" />
        <el-table-column label="商户" min-width="120">
          <template #default="scope">
            <div>{{ scope.row.merchantName }}</div>
            <div class="sub-text">(ID: {{ scope.row.merchantId }})</div>
          </template>
        </el-table-column>
        <el-table-column label="用户" min-width="120">
          <template #default="scope">
            <div>{{ scope.row.userName }}</div>
            <div class="sub-text">{{ scope.row.userPhone }}</div>
          </template>
        </el-table-column>
        <el-table-column label="评分" width="150">
          <template #default="scope">
            <el-rate v-model="scope.row.rating" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="图片" width="100">
          <template #default="scope">
            <el-image
              v-if="scope.row.images"
              style="width: 50px; height: 50px"
              :src="scope.row.images"
              :preview-src-list="[scope.row.images]"
              preview-teleported
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评价时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-popconfirm title="确认删除该评价吗？" @confirm="handleDelete(scope.row)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useReviewPage } from '@/composables/business/useReviewPage'

const {
  activeTab,
  dateRange,
  getList,
  handleDateChange,
  handleDelete,
  handleSearch,
  handleTabChange,
  loading,
  queryParams,
  resetQuery,
  tableData,
  total,
} = useReviewPage()
</script>

<style scoped>
.mt-20 {
  margin-top: 20px;
}
.sub-text {
  font-size: 12px;
  color: #909399;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>
