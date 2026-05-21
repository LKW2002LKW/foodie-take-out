<template>
  <div class="report-container">
    <!-- 数据报表页作为装配层，图表查询与实例管理逻辑统一来自 useReportPage。 -->
    <el-card>
      <el-tabs v-model="activeTab" type="card" @tab-change="handleTabChange">
        <el-tab-pane label="营业额报表" name="revenue">
          <div class="chart-controls">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              :shortcuts="shortcuts"
              @change="fetchRevenue"
            />
          </div>
          <div ref="revenueChartRef" class="chart-container"></div>
          <div class="stat-summary" v-if="revenueData">
            <h3>总营业额: <span class="text-primary">¥{{ revenueData.totalRevenue }}</span></h3>
          </div>
        </el-tab-pane>

        <el-tab-pane label="订单统计" name="order">
          <div class="chart-controls">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              :shortcuts="shortcuts"
              @change="fetchOrder"
            />
          </div>
          <div ref="orderChartRef" class="chart-container"></div>
          <div class="stat-summary" v-if="orderData">
            <el-descriptions :column="3" border>
              <el-descriptions-item label="总订单数">{{ orderData.totalOrderCount }}</el-descriptions-item>
              <el-descriptions-item label="有效订单">{{ orderData.validOrderCount }}</el-descriptions-item>
              <el-descriptions-item label="完成率">{{ orderData.orderCompletionRate }}%</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-tab-pane>

        <el-tab-pane label="用户增长" name="userGrowth">
          <div class="chart-controls">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              :shortcuts="shortcuts"
              @change="fetchUserGrowth"
            />
          </div>
          <div ref="userGrowthChartRef" class="chart-container"></div>
          <div class="stat-summary" v-if="userGrowthData">
            <h3>新增用户数: <span class="text-success">{{ userGrowthData.totalNewUserCount }}</span></h3>
          </div>
        </el-tab-pane>

        <el-tab-pane label="热销商品TOP10" name="hotDishes">
          <el-table :data="hotDishesList" border stripe style="width: 100%">
            <el-table-column type="index" label="排名" width="80" />
            <el-table-column prop="dishName" label="菜品名称" />
            <el-table-column label="图片" width="100">
              <template #default="scope">
                <el-image
                  style="width: 50px; height: 50px"
                  :src="scope.row.dishImage"
                  :preview-src-list="[scope.row.dishImage]"
                  preview-teleported
                />
              </template>
            </el-table-column>
            <el-table-column prop="merchantName" label="所属商户" />
            <el-table-column prop="salesCount" label="销量" sortable />
            <el-table-column label="销售占比">
              <template #default="scope">
                <el-progress :percentage="scope.row.salesRatio" />
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { useReportPage } from '@/composables/business/useReportPage'

const {
  activeTab,
  dateRange,
  fetchOrder,
  fetchRevenue,
  fetchUserGrowth,
  handleTabChange,
  hotDishesList,
  orderChartRef,
  orderData,
  revenueChartRef,
  revenueData,
  shortcuts,
  userGrowthChartRef,
  userGrowthData,
} = useReportPage()
</script>

<style scoped>
.chart-controls {
  margin-bottom: 20px;
}
.chart-container {
  width: 100%;
  height: 400px;
}
.stat-summary {
  margin-top: 20px;
  text-align: center;
}
.text-primary {
  color: #409EFF;
}
.text-success {
  color: #67C23A;
}
</style>
