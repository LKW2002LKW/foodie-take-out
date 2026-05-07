<template>
  <div class="order-container">
    <!-- 订单监控页只承载筛选、列表和弹窗视图，查询逻辑下沉到 useOrderPage。 -->
    <el-card>
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="queryParams.orderNumber" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="待支付" :value="1" />
            <el-option label="待接单" :value="2" />
            <el-option label="已接单" :value="3" />
            <el-option label="配送中" :value="4" />
            <el-option label="已完成" :value="5" />
            <el-option label="已取消" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button type="danger" @click="showAbnormal">异常订单</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border v-loading="loading">
        <el-table-column prop="orderNumber" label="订单号" min-width="150" />
        <el-table-column prop="merchantName" label="商户名称" min-width="150" />
        <el-table-column prop="orderTime" label="下单时间" width="180" />
        <el-table-column prop="totalAmount" label="总金额" width="100">
          <template #default="scope">¥{{ scope.row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="consignee" label="收货人" width="100" />
        <el-table-column prop="phone" label="电话" width="120" />
        <el-table-column label="支付状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.payStatus === 1 ? 'success' : 'info'">
              {{ scope.row.payStatus === 1 ? '已支付' : '未支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="100">
          <template #default="scope">
            <el-tag>{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleDetail(scope.row)">详情</el-button>
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

    <el-dialog v-model="detailVisible" title="订单详情" width="700px">
      <div v-if="currentOrder">
        <h4>基础信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNumber }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ currentOrder.orderTime }}</el-descriptions-item>
          <el-descriptions-item label="商户">{{ currentOrder.merchantName }}</el-descriptions-item>
          <el-descriptions-item label="用户">{{ currentOrder.userName }} ({{ currentOrder.userPhone }})</el-descriptions-item>
          <el-descriptions-item label="支付状态">{{ currentOrder.payStatusText }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">{{ currentOrder.statusText }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ currentOrder.consignee }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.phone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.address }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '无' }}</el-descriptions-item>
        </el-descriptions>

        <h4>菜品明细</h4>
        <el-table :data="currentOrder.orderDetailList" border style="width: 100%">
          <el-table-column prop="name" label="菜品名称" />
          <el-table-column prop="dishFlavor" label="口味" />
          <el-table-column prop="number" label="数量" width="80" />
          <el-table-column prop="amount" label="价格" width="100" />
        </el-table>

        <div class="amount-summary">
          <p>菜品合计: ¥{{ currentOrder.amount }}</p>
          <p>配送费: ¥{{ currentOrder.deliveryFee }}</p>
          <p>打包费: ¥{{ currentOrder.packAmount }}</p>
          <p class="total">实付金额: ¥{{ currentOrder.totalAmount }}</p>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="abnormalVisible" title="异常订单（超时未接）" width="800px">
      <el-table :data="abnormalData" border>
        <el-table-column prop="orderNumber" label="订单号" />
        <el-table-column prop="orderTime" label="下单时间" />
        <el-table-column prop="totalAmount" label="金额" />
        <el-table-column label="状态">
          <template #default>
            <el-tag type="danger">超时未接</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { useOrderPage } from '@/composables/business/useOrderPage'

const {
  abnormalData,
  abnormalVisible,
  currentOrder,
  dateRange,
  detailVisible,
  getList,
  getStatusText,
  handleDateChange,
  handleDetail,
  handleSearch,
  loading,
  queryParams,
  showAbnormal,
  tableData,
  total,
} = useOrderPage()
</script>

<style scoped>
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
.amount-summary {
  text-align: right;
  margin-top: 20px;
}
.amount-summary p {
  margin: 5px 0;
}
.amount-summary .total {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}
</style>
