<template>
  <div class="report-container">
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
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '../../utils/request'
import dayjs from 'dayjs'

const activeTab = ref('revenue')
const dateRange = ref([])
// Default last 7 days
const defaultStart = dayjs().subtract(6, 'day').format('YYYY-MM-DD')
const defaultEnd = dayjs().format('YYYY-MM-DD')

dateRange.value = [defaultStart, defaultEnd]

const shortcuts = [
  { text: '最近7天', value: () => [new Date(new Date().getTime() - 3600 * 1000 * 24 * 6), new Date()] },
  { text: '最近30天', value: () => [new Date(new Date().getTime() - 3600 * 1000 * 24 * 29), new Date()] },
]

// Revenue
const revenueChartRef = ref(null)
const revenueData = ref(null)
let revenueChartInstance = null

// Order
const orderChartRef = ref(null)
const orderData = ref(null)
let orderChartInstance = null

// User Growth
const userGrowthChartRef = ref(null)
const userGrowthData = ref(null)
let userGrowthChartInstance = null

// Hot Dishes
const hotDishesList = ref([])

const fetchRevenue = async () => {
  if (!dateRange.value) return
  const res = await request.get('/report/revenue', {
    params: { beginDate: dateRange.value[0], endDate: dateRange.value[1] }
  })
  revenueData.value = res.data
  renderRevenueChart(res.data)
}

const renderRevenueChart = (data) => {
  if (!revenueChartRef.value) return
  if (!revenueChartInstance) revenueChartInstance = echarts.init(revenueChartRef.value)
  
  const option = {
    title: { text: '营业额趋势' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.dateList },
    yAxis: { type: 'value' },
    series: [{
      data: data.revenueList,
      type: 'line',
      smooth: true,
      areaStyle: {},
      itemStyle: { color: '#409EFF' }
    }]
  }
  revenueChartInstance.setOption(option)
}

const fetchOrder = async () => {
    if (!dateRange.value) return
    const res = await request.get('/report/order', {
        params: { beginDate: dateRange.value[0], endDate: dateRange.value[1] }
    })
    orderData.value = res.data
    renderOrderChart(res.data)
}

const renderOrderChart = (data) => {
  if (!orderChartRef.value) return
  if (!orderChartInstance) orderChartInstance = echarts.init(orderChartRef.value)
  
  const option = {
    title: { text: '订单统计' },
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单总数', '有效订单'] },
    xAxis: { type: 'category', data: data.dateList },
    yAxis: { type: 'value' },
    series: [
        {
            name: '订单总数',
            data: data.orderCountList,
            type: 'bar',
            itemStyle: { color: '#E6A23C' }
        },
        {
            name: '有效订单',
            data: data.validOrderCountList,
            type: 'bar', // Using bar for comparison can be good, or line
            itemStyle: { color: '#67C23A' }
        }
    ]
  }
  orderChartInstance.setOption(option)
}

const fetchUserGrowth = async () => {
    if (!dateRange.value) return
    const res = await request.get('/report/user-growth', {
        params: { beginDate: dateRange.value[0], endDate: dateRange.value[1] }
    })
    userGrowthData.value = res.data
    renderUserGrowthChart(res.data)
}

const renderUserGrowthChart = (data) => {
  if (!userGrowthChartRef.value) return
  if (!userGrowthChartInstance) userGrowthChartInstance = echarts.init(userGrowthChartRef.value)
  
  const option = {
    title: { text: '用户增长' },
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增用户', '累计用户'] },
    xAxis: { type: 'category', data: data.dateList },
    yAxis: [
        { type: 'value', name: '新增' },
        { type: 'value', name: '累计', position: 'right' }
    ],
    series: [
        {
            name: '新增用户',
            data: data.newUserCountList,
            type: 'bar',
            itemStyle: { color: '#409EFF' }
        },
        {
            name: '累计用户',
            data: data.totalUserCountList,
            type: 'line',
            yAxisIndex: 1,
            itemStyle: { color: '#F56C6C' }
        }
    ]
  }
  userGrowthChartInstance.setOption(option)
}

const fetchHotDishes = async () => {
    const res = await request.get('/report/hot-dishes')
    hotDishesList.value = res.data
}

const handleTabChange = (name) => {
    nextTick(() => {
        if (name === 'revenue') {
            fetchRevenue()
             revenueChartInstance && revenueChartInstance.resize()
        } else if (name === 'order') {
            fetchOrder()
             orderChartInstance && orderChartInstance.resize()
        } else if (name === 'userGrowth') {
            fetchUserGrowth()
            userGrowthChartInstance && userGrowthChartInstance.resize()
        } else if (name === 'hotDishes') {
            fetchHotDishes()
        }
    })
}

onMounted(() => {
    fetchRevenue()
    window.addEventListener('resize', () => {
        revenueChartInstance && revenueChartInstance.resize()
        orderChartInstance && orderChartInstance.resize()
        userGrowthChartInstance && userGrowthChartInstance.resize()
    })
})
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
