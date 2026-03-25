<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>今日订单数</template>
          <div class="card-value">{{ stats.todayOrderCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>今日营收</template>
          <div class="card-value">¥{{ stats.todayRevenue || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>今日新增用户</template>
          <div class="card-value">{{ stats.todayNewUserCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>待审核商户</template>
          <div class="card-value text-warning">{{ stats.pendingMerchantCount || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Detailed Stats -->
    <el-row :gutter="20" style="margin-top: 20px;">
       <el-col :span="4">
         <div class="sub-stat">总用户: {{ stats.totalUserCount }}</div>
       </el-col>
       <el-col :span="4">
         <div class="sub-stat">总商户: {{ stats.totalMerchantCount }}</div>
       </el-col>
       <el-col :span="4">
         <div class="sub-stat">营业中: {{ stats.openMerchantCount }}</div>
       </el-col>
    </el-row>

    <el-card class="chart-card" shadow="never">
      <template #header>
        <div class="chart-header">
          <span>趋势分析</span>
          <el-radio-group v-model="days" size="small" @change="fetchTrend">
            <el-radio-button :label="7">近7天</el-radio-button>
            <el-radio-button :label="30">近30天</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="chartRef" style="height: 400px; width: 100%;"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import * as echarts from 'echarts'
import request from '../../utils/request'

const stats = ref({})
const days = ref(7)
const chartRef = ref(null)
let chartInstance = null

const fetchStats = async () => {
  try {
    const res = await request.get('/dashboard/data')
    stats.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const fetchTrend = async () => {
  try {
    const res = await request.get(`/dashboard/trend?days=${days.value}`)
    const data = res.data
    renderChart(data)
  } catch (error) {
    console.error(error)
  }
}

const renderChart = (data) => {
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  
  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单量', '营收额', '新增用户'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: data.dateList },
    yAxis: [
      { type: 'value', name: '数量' },
      { type: 'value', name: '金额', position: 'right' }
    ],
    series: [
      {
        name: '订单量',
        type: 'line',
        data: data.orderCountList,
        smooth: true
      },
      {
        name: '营收额',
        type: 'line',
        yAxisIndex: 1,
        data: data.revenueList,
        smooth: true,
        itemStyle: { color: '#E6A23C' }
      },
      {
        name: '新增用户',
        type: 'line',
        data: data.userCountList,
        smooth: true,
        itemStyle: { color: '#67C23A' }
      }
    ]
  }
  chartInstance.setOption(option)
}

onMounted(() => {
  fetchStats()
  fetchTrend()
  window.addEventListener('resize', () => {
    chartInstance && chartInstance.resize()
  })
})
</script>

<style scoped>
.card-value {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  color: #409EFF;
}
.text-warning {
  color: #E6A23C;
}
.chart-card {
  margin-top: 20px;
}
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.sub-stat {
  background: #fff;
  padding: 10px;
  text-align: center;
  border-radius: 4px;
  color: #606266;
}
</style>
