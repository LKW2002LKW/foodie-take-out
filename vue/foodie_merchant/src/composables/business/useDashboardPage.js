import { computed, onMounted, ref, watch } from 'vue'
import { Bell, Clock, List, Money, TrendCharts } from '@element-plus/icons-vue'
import { useDashboardCharts } from '@/composables/useDashboardCharts'
import { useTimer } from '@/composables/useTimer'
import { useDashboardStore } from '@/stores/modules/dashboardStore'

// 工作台页面总控组合式，负责统计卡片、趋势图和时间展示。
export const useDashboardPage = () => {
  const store = useDashboardStore()
  const { currentTime } = useTimer()

  const revenueChartRef = ref(null)
  const orderChartRef = ref(null)
  const trendDays = ref(7)
  const { renderCharts } = useDashboardCharts(revenueChartRef, orderChartRef)

  const statCards = computed(() => [
    {
      title: '今日营业额',
      value: store.todayStats.todayRevenue.toFixed(2),
      unit: '￥',
      subTitle: '实收金额',
      subValue: store.todayStats.todayIncome.toFixed(2),
      subUnit: '￥',
      icon: Money,
      type: 'primary',
    },
    {
      title: '今日订单数',
      value: store.todayStats.totalOrderCount,
      subTitle: '已取消订单',
      subValue: store.todayStats.cancelledOrderCount,
      icon: List,
      type: 'success',
    },
    {
      title: '待处理订单',
      value: store.todayStats.pendingOrderCount,
      subTitle: '需尽快响应',
      subValue: '急',
      icon: Bell,
      type: 'warning',
    },
    {
      title: '今日完成率',
      value: `${store.todayStats.completionRate}%`,
      subTitle: '服务质量',
      subValue: store.todayStats.completionRate >= 90 ? '优' : '良',
      icon: TrendCharts,
      type: 'danger',
    },
  ])

  const overviewItems = computed(() => [
    { label: '累计营业额', value: store.overviewStats.totalRevenue.toFixed(2), unit: '￥' },
    { label: '累计订单', value: store.overviewStats.totalOrders },
    { label: '客单价', value: store.overviewStats.avgOrderAmount.toFixed(2), unit: '￥' },
    { label: '店铺评分', value: store.overviewStats.merchantRating, type: 'rate' },
  ])

  const handleTrendChange = (value) => {
    store.fetchTrendStats(value)
  }

  watch(
    () => store.trendData,
    (newData) => {
      if (newData.dateList.length) {
        renderCharts(newData)
      }
    },
    { deep: true },
  )

  onMounted(async () => {
    await Promise.all([
      store.fetchTodayStats(),
      store.fetchOverviewStats(),
      store.fetchTrendStats(trendDays.value),
    ])
  })

  return {
    currentTime,
    handleTrendChange,
    orderChartRef,
    overviewItems,
    revenueChartRef,
    statCards,
    store,
    timerIcon: Clock,
    trendDays,
  }
}
