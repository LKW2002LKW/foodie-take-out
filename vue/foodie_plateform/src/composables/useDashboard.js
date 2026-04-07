import { ref, reactive } from 'vue'
import dashboardApi from '@/api/dashboard'

/**
 * 平台端仪表盘核心数据 Composable
 */
export function useDashboard() {
  const loading = ref(false)
  const stats = reactive({
    todayOrderCount: 0,
    todayRevenue: 0,
    todayNewUserCount: 0,
    pendingMerchantCount: 0,
    totalUserCount: 0,
    totalMerchantCount: 0,
    openMerchantCount: 0
  })

  const trendData = ref({
    dateList: [],
    orderCountList: [],
    revenueList: [],
    userCountList: []
  })

  /**
   * 获取今日核心统计
   */
  const fetchStats = async () => {
    loading.value = true
    try {
      const { data } = await dashboardApi.getStats()
      if (data) Object.assign(stats, data)
    } catch (e) {
      console.error('Fetch stats failed:', e)
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取趋势图表
   */
  const fetchTrend = async (days = 7) => {
    try {
      const { data } = await dashboardApi.getTrend({ days })
      if (data) trendData.value = data
    } catch (e) {
      console.error('Fetch trend failed:', e)
    }
  }

  return {
    loading,
    stats,
    trendData,
    fetchStats,
    fetchTrend
  }
}
