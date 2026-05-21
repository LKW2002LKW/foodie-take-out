<template>
  <div class="order-list-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">订单管理</h1>
        <p class="page-subtitle">查看及处理店铺订单</p>
      </div>
    </div>

    <!-- 筛选区域 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="订单号">
          <el-input v-model="queryParams.orderNumber" placeholder="请输入订单号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订单列表 -->
    <el-card shadow="hover" class="list-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部订单" name="all" />
        <el-tab-pane label="待付款" name="1" />
        <el-tab-pane name="2">
          <template #label>
            待接单 <el-badge :value="statistics.toBeConfirmed" class="item-badge" v-if="statistics.toBeConfirmed > 0" />
          </template>
        </el-tab-pane>
        <el-tab-pane name="3">
          <template #label>
             已接单 <el-badge :value="statistics.confirmed" class="item-badge" type="primary" v-if="statistics.confirmed > 0" />
          </template>
        </el-tab-pane>
        <el-tab-pane name="4">
           <template #label>
             派送中 <el-badge :value="statistics.deliveryInProgress" class="item-badge" type="warning" v-if="statistics.deliveryInProgress > 0" />
          </template>
        </el-tab-pane>
        <el-tab-pane label="已完成" name="5" />
        <el-tab-pane label="已取消" name="6" />
      </el-tabs>

      <el-table v-loading="loading" :data="orderList" style="width: 100%" stripe>
        <el-table-column prop="orderNumber" label="订单号" min-width="140" />
        <el-table-column label="订单状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="用户信息" min-width="180">
          <template #default="{ row }">
            <div><el-icon><User /></el-icon> {{ row.consignee }}</div>
            <div><el-icon><Iphone /></el-icon> {{ row.phone }}</div>
            <div class="text-truncate" :title="row.address"><el-icon><Location /></el-icon> {{ row.address }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="orderTime" label="下单时间" width="170" sortable />
        <el-table-column label="实收金额" width="120" align="center">
          <template #default="{ row }">
             <div class="price-text">￥{{ Number(row.totalAmount).toFixed(2) }}</div>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="{ row }">
             <el-button link type="primary" @click="handleViewDetail(row)">详情</el-button>
             
             <!-- 待接单 (2) -->
             <template v-if="row.status === 2">
                <el-button link type="success" @click="handleOrderAction('confirm', row)">接单</el-button>
                <el-button link type="danger" @click="handleOrderAction('reject', row)">拒单</el-button>
             </template>

             <!-- 已接单 (3) -->
             <template v-if="row.status === 3">
                <el-button link type="warning" @click="handleOrderAction('delivery', row)">派送</el-button>
                <el-button link type="danger" @click="handleOrderAction('cancel', row)">取消</el-button>
             </template>

             <!-- 派送中 (4) -->
             <template v-if="row.status === 4">
                <el-button link type="success" @click="handleOrderAction('complete', row)">完成</el-button>
                <el-button link type="danger" @click="handleOrderAction('cancel', row)">取消</el-button>
             </template>

              <!-- 待付款 (1) -->
             <template v-if="row.status === 1">
                <el-button link type="danger" @click="handleOrderAction('cancel', row)">取消</el-button>
             </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </el-card>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="700px" append-to-body destroy-on-close>
       <div v-if="currentOrder" class="order-detail">
          <el-descriptions title="基础信息" :column="2" border>
             <el-descriptions-item label="订单号">{{ currentOrder.orderNumber }}</el-descriptions-item>
             <el-descriptions-item label="下单时间">{{ currentOrder.orderTime }}</el-descriptions-item>
             <el-descriptions-item label="支付状态">{{ getPayStatusText(currentOrder.payStatus) }}</el-descriptions-item>
             <el-descriptions-item label="支付方式">{{ getPayMethodText(currentOrder.payMethod) }}</el-descriptions-item>
             <el-descriptions-item label="当前状态">
                 <el-tag :type="getStatusType(currentOrder.status)">{{ getStatusText(currentOrder.status) }}</el-tag>
             </el-descriptions-item>
             <el-descriptions-item label="预计送达">{{ currentOrder.estimatedDeliveryTime || '-' }}</el-descriptions-item>
          </el-descriptions>

          <div class="section-title">收货信息</div>
          <el-descriptions :column="1" border>
             <el-descriptions-item label="收货人">{{ currentOrder.consignee }} ({{ currentOrder.phone }})</el-descriptions-item>
             <el-descriptions-item label="收货地址">{{ currentOrder.address }}</el-descriptions-item>
             <el-descriptions-item label="备注">
                <span class="remark-text">{{ currentOrder.remark || '无' }}</span>
             </el-descriptions-item>
          </el-descriptions>

           <div class="section-title">菜品信息</div>
           <el-table :data="currentOrder.orderDetailList" border style="width: 100%">
               <el-table-column label="菜品图片" width="80" align="center">
                   <template #default="{ row }">
                       <el-image :src="row.image" style="width: 50px; height: 50px" fit="cover" />
                   </template>
               </el-table-column>
               <el-table-column prop="name" label="菜品名称" />
               <el-table-column prop="dishFlavor" label="规格/口味">
                   <template #default="{ row }">
                       {{ row.dishFlavor || '-' }}
                   </template>
               </el-table-column>
               <el-table-column prop="number" label="数量" width="80" align="center" />
               <el-table-column prop="amount" label="单价" width="100" align="right">
                   <template #default="{ row }">￥{{ row.amount }}</template>
               </el-table-column>
           </el-table>
           
           <div class="amount-summary">
               <div class="so-row"><span>打包费:</span> <span>￥{{ currentOrder.packAmount }}</span></div>
               <div class="so-row"><span>配送费:</span> <span>￥{{ currentOrder.deliveryFee }}</span></div>
               <div class="so-row total"><span>实收金额:</span> <span class="price">￥{{ currentOrder.totalAmount }}</span></div>
           </div>
       </div>
       <template #footer>
          <span class="dialog-footer">
             <el-button @click="detailVisible = false">关闭</el-button>
          </span>
       </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { Iphone, Location, Refresh, Search, User } from '@element-plus/icons-vue'
import { useOrderListPage } from '@/composables/business/useOrderListPage'

const {
  activeTab,
  currentOrder,
  dateRange,
  detailVisible,
  getPayMethodText,
  getPayStatusText,
  getStatusText,
  getStatusType,
  handleCurrentChange,
  handleDateChange,
  handleOrderAction,
  handleQuery,
  handleSizeChange,
  handleTabChange,
  handleViewDetail,
  loading,
  orderList,
  queryParams,
  resetQuery,
  statistics,
  total,
} = useOrderListPage()
</script>

<style scoped>
.order-list-page {
  padding: 0;
}
.page-header {
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
.item-badge {
    margin-left: 5px;
}
.text-truncate {
    white-space: nowrap; 
    overflow: hidden;
    text-overflow: ellipsis; 
}
.price-text {
    font-weight: bold;
    color: #f56c6c;
}
.pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}
/* Detail Styles */
.section-title {
    font-size: 16px;
    font-weight: bold;
    margin: 20px 0 10px;
    padding-left: 10px;
    border-left: 4px solid #409EFF;
}
.remark-text {
    color: #E6A23C;
}
.amount-summary {
    margin-top: 20px;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    border-top: 1px solid #ebeef5;
    padding-top: 15px;
}
.so-row {
    margin-bottom: 8px;
    font-size: 14px;
    width: 200px;
    display: flex;
    justify-content: space-between;
}
.so-row.total {
    font-size: 18px;
    font-weight: bold;
    color: #f56c6c;
    margin-top: 5px;
}
</style>
