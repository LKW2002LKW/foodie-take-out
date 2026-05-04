<template>
  <div class="income-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">收入明细</h1>
        <p class="page-subtitle">每一笔订单的收入记录</p>
      </div>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="订单号">
          <el-input v-model="queryParams.orderNumber" placeholder="请输入订单号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="list-card" v-loading="loading">
      <el-table :data="tableData" style="width: 100%" stripe>
        <el-table-column prop="orderNumber" label="订单号" min-width="160" />
        <el-table-column prop="orderTime" label="下单时间" width="180" sortable />
        <el-table-column label="支付状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.payStatus === 1 ? 'success' : 'info'">
              {{ row.payStatusText || (row.payStatus === 1 ? '已支付' : '未支付') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="订单金额" align="right" min-width="120">
          <template #default="{ row }">￥{{ Number(row.totalAmount).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="平台抽成" align="right" min-width="120">
          <template #default="{ row }">
            <span class="text-danger">-￥{{ Number(row.platformCommission).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="商户实收" align="right" min-width="150">
          <template #default="{ row }">
            <span class="amount-highlight">￥{{ Number(row.merchantAmount).toFixed(2) }}</span>
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
  </div>
</template>

<script setup>
import { useFinanceIncomePage } from '@/composables/business/useFinanceIncomePage'

const {
  dateRange,
  handleCurrentChange,
  handleDateChange,
  handleQuery,
  handleSizeChange,
  loading,
  queryParams,
  resetQuery,
  tableData,
  total,
} = useFinanceIncomePage()
</script>

<style scoped>
.income-page { padding: 0; }
.page-header { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 700; color: #1a1a1a; margin: 0 0 8px 0; }
.page-subtitle { color: #909399; font-size: 14px; margin: 0; }
.filter-card { margin-bottom: 20px; border-radius: 8px; }
.list-card { border-radius: 8px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.text-danger { color: #f56c6c; }
.amount-highlight { color: #67c23a; font-weight: bold; font-size: 16px; }
</style>
