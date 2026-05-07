<template>
  <div class="settlement-page">
    <!-- 结算记录页只负责表格与筛选渲染，业务查询逻辑下沉到 useFinanceSettlementPage。 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">结算账单</h1>
        <p class="page-subtitle">查看历史结算记录及打款状态</p>
      </div>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="结算周期">
          <el-date-picker
            v-model="queryParams.settlementCycle"
            type="month"
            placeholder="选择月份"
            format="YYYY-MM"
            value-format="YYYY-MM"
            @change="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 150px" @change="handleQuery">
            <el-option label="待结算" :value="1" />
            <el-option label="已结算" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="list-card" v-loading="loading">
      <el-table :data="tableData" style="width: 100%" stripe>
        <el-table-column prop="settlementCycle" label="结算周期" width="120" align="center" font-weight="bold" />
        <el-table-column prop="orderCount" label="订单总数" width="120" align="center" />
        <el-table-column label="交易总额" align="right" min-width="120">
          <template #default="{ row }">￥{{ Number(row.totalAmount).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="平台抽成" align="right" min-width="120">
          <template #default="{ row }">
            <span class="text-danger">-￥{{ Number(row.platformCommission).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="本期应结" align="right" min-width="150">
          <template #default="{ row }">
            <span class="amount-highlight">￥{{ Number(row.settlementAmount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 2 ? 'success' : 'warning'" effect="dark">
              {{ row.statusText || (row.status === 2 ? '已结算' : '待结算') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="settlementTime" label="打款时间" width="180" align="center">
          <template #default="{ row }">{{ row.settlementTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default>
            <el-button link type="primary" disabled>查看明细</el-button>
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
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useFinanceSettlementPage } from '@/composables/business/useFinanceSettlementPage'

const {
  handleCurrentChange,
  handleQuery,
  handleSizeChange,
  loading,
  queryParams,
  resetQuery,
  tableData,
  total,
} = useFinanceSettlementPage()
</script>

<style scoped>
.settlement-page { padding: 0; }
.page-header { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 700; color: #1a1a1a; margin: 0 0 8px 0; }
.page-subtitle { color: #909399; font-size: 14px; margin: 0; }
.filter-card { margin-bottom: 20px; border-radius: 8px; }
.list-card { border-radius: 8px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.text-danger { color: #f56c6c; }
.amount-highlight { color: #67c23a; font-weight: bold; font-size: 16px; }
</style>
