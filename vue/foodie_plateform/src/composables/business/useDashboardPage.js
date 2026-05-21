import { computed, onMounted, ref, watch } from 'vue'
import { Money, ShoppingCart, Shop, User } from '@element-plus/icons-vue'
import dashboardApi from '@/api/modules/dashboard'
import { useCharts } from '@/composables/useCharts'
import { useDashboardStore } from '@/stores/modules/dashboardStore'

// 平台工作台组合式，负责统计卡片、趋势图与刷新动作装配。
export const useDashboardPage = () => {
  const days = ref(7)
  const chartRef = ref(null)
  const loading = ref(false)
  const dashboardStore = useDashboardStore()
  const { renderChart } = useCharts(chartRef)

  const fetchStats = async () => {
    loading.value = true
    try {
      const { data } = await dashboardApi.getStats()
      if (data) {
        dashboardStore.updateStats(data)
      }
    } catch (error) {
      console.error('Fetch stats failed:', error)
    } finally {
      loading.value = false
    }
  }

  const fetchTrend = async (targetDays = 7) => {
    try {
      const { data } = await dashboardApi.getTrend({ days: targetDays })
      if (data) {
        dashboardStore.updateTrendData(data)
      }
    } catch (error) {
      console.error('Fetch trend failed:', error)
    }
  }

  const coreStats = computed(() => [
    { label: '今日订单', value: dashboardStore.stats.todayOrderCount, icon: ShoppingCart, type: 'primary' },
    { label: '今日营收', value: Number(dashboardStore.stats.todayRevenue || 0).toFixed(2), unit: '￥', icon: Money, type: 'success' },
    { label: '新增用户', value: dashboardStore.stats.todayNewUserCount, icon: User, type: 'info' },
    { label: '待审商户', value: dashboardStore.stats.pendingMerchantCount, icon: Shop, type: 'warning' },
  ])

  const detailedStats = computed(() => [
    { label: '平台总用户', value: dashboardStore.stats.totalUserCount },
    { label: '注册商户总数', value: dashboardStore.stats.totalMerchantCount },
    { label: '当前营业商户', value: dashboardStore.stats.openMerchantCount },
    { label: '系统运行状态', value: '正常' },
  ])

  const handleRefresh = () => {
    fetchStats()
    fetchTrend(days.value)
  }

  watch(
    () => ({ ...dashboardStore.trendData }),
    (newData) => {
      if (newData.dateList?.length) {
        renderChart(newData)
      }
    },
    { deep: true },
  )

  onMounted(() => {
    handleRefresh()
  })

  return {
    chartRef,
    coreStats,
    days,
    detailedStats,
    fetchTrend,
    handleRefresh,
    loading,
  }
}
