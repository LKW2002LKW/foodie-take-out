import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import reportApi from '@/api/modules/report'

const formatDate = (date) => {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

const createShortcutRange = (days) => {
  const end = new Date()
  const start = new Date(Date.now() - 3600 * 1000 * 24 * (days - 1))
  return [start, end]
}

// 数据报表页组合式，负责多报表查询、ECharts 实例维护与切换刷新。
export const useReportPage = () => {
  const activeTab = ref('revenue')
  const dateRange = ref([
    formatDate(new Date(Date.now() - 3600 * 1000 * 24 * 6)),
    formatDate(new Date()),
  ])

  const shortcuts = [
    { text: '最近7天', value: () => createShortcutRange(7) },
    { text: '最近30天', value: () => createShortcutRange(30) },
  ]

  const revenueChartRef = ref(null)
  const orderChartRef = ref(null)
  const userGrowthChartRef = ref(null)
  const revenueData = ref(null)
  const orderData = ref(null)
  const userGrowthData = ref(null)
  const hotDishesList = ref([])

  let revenueChartInstance = null
  let orderChartInstance = null
  let userGrowthChartInstance = null

  const currentParams = () => ({
    beginDate: dateRange.value?.[0],
    endDate: dateRange.value?.[1],
  })

  const ensureChart = (refEl, instance) => {
    if (!refEl.value) return instance
    return instance || echarts.init(refEl.value)
  }

  const fetchRevenue = async () => {
    if (!dateRange.value) return
    const res = await reportApi.getRevenueReport(currentParams())
    revenueData.value = res.data
    revenueChartInstance = ensureChart(revenueChartRef, revenueChartInstance)
    revenueChartInstance?.setOption({
      title: { text: '营业额趋势' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: res.data.dateList },
      yAxis: { type: 'value' },
      series: [{
        data: res.data.revenueList,
        type: 'line',
        smooth: true,
        areaStyle: {},
        itemStyle: { color: '#409EFF' },
      }],
    })
  }

  const fetchOrder = async () => {
    if (!dateRange.value) return
    const res = await reportApi.getOrderReport(currentParams())
    orderData.value = res.data
    orderChartInstance = ensureChart(orderChartRef, orderChartInstance)
    orderChartInstance?.setOption({
      title: { text: '订单统计' },
      tooltip: { trigger: 'axis' },
      legend: { data: ['订单总数', '有效订单'] },
      xAxis: { type: 'category', data: res.data.dateList },
      yAxis: { type: 'value' },
      series: [
        {
          name: '订单总数',
          data: res.data.orderCountList,
          type: 'bar',
          itemStyle: { color: '#E6A23C' },
        },
        {
          name: '有效订单',
          data: res.data.validOrderCountList,
          type: 'bar',
          itemStyle: { color: '#67C23A' },
        },
      ],
    })
  }

  const fetchUserGrowth = async () => {
    if (!dateRange.value) return
    const res = await reportApi.getUserGrowthReport(currentParams())
    userGrowthData.value = res.data
    userGrowthChartInstance = ensureChart(userGrowthChartRef, userGrowthChartInstance)
    userGrowthChartInstance?.setOption({
      title: { text: '用户增长' },
      tooltip: { trigger: 'axis' },
      legend: { data: ['新增用户', '累计用户'] },
      xAxis: { type: 'category', data: res.data.dateList },
      yAxis: [
        { type: 'value', name: '新增' },
        { type: 'value', name: '累计', position: 'right' },
      ],
      series: [
        {
          name: '新增用户',
          data: res.data.newUserCountList,
          type: 'bar',
          itemStyle: { color: '#409EFF' },
        },
        {
          name: '累计用户',
          data: res.data.totalUserCountList,
          type: 'line',
          yAxisIndex: 1,
          itemStyle: { color: '#F56C6C' },
        },
      ],
    })
  }

  const fetchHotDishes = async () => {
    const res = await reportApi.getHotDishesReport()
    hotDishesList.value = res.data || []
  }

  const handleTabChange = (name) => {
    nextTick(() => {
      if (name === 'revenue') {
        fetchRevenue()
        revenueChartInstance?.resize()
      } else if (name === 'order') {
        fetchOrder()
        orderChartInstance?.resize()
      } else if (name === 'userGrowth') {
        fetchUserGrowth()
        userGrowthChartInstance?.resize()
      } else if (name === 'hotDishes') {
        fetchHotDishes()
      }
    })
  }

  const handleResize = () => {
    revenueChartInstance?.resize()
    orderChartInstance?.resize()
    userGrowthChartInstance?.resize()
  }

  onMounted(() => {
    fetchRevenue()
    window.addEventListener('resize', handleResize)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
    revenueChartInstance?.dispose()
    orderChartInstance?.dispose()
    userGrowthChartInstance?.dispose()
  })

  return {
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
  }
}
