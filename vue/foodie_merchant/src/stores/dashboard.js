import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'
import statisticsApi from '@/api/statistics'

/**
 * 仪表盘状态管理 Store
 */
export const useDashboardStore = defineStore('dashboard', () => {
  // 今日经营概况
  const todayStats = reactive({
    todayRevenue: 0,
    todayIncome: 0,
    totalOrderCount: 0,
    pendingOrderCount: 0,
    cancelledOrderCount: 0,
    completionRate: 0
  })

  // 累计经营数据
  const overviewStats = reactive({
    totalRevenue: 0,
    totalOrders: 0,
    avgOrderAmount: 0,
    merchantRating: 0,
    totalReviews: 0
  })

  // 趋势图表数据
  const trendData = ref({
    dateList: [],
    revenueList: [],
    incomeList: [],
    orderCountList: []
  })

  const loading = reactive({
    today: false,
    overview: false,
    trend: false
  })

  /**
   * 获取今日统计数据
   */
  const fetchTodayStats = async () => {
    loading.today = true
    try {
      const { data } = await statisticsApi.getTodayStatistics()
      if (data) {
        Object.assign(todayStats, data)
      }
    } catch (error) {
      console.error('Failed to fetch today stats:', error)
    } finally {
      loading.today = false
    }
  }

  /**
   * 获取概览统计数据
   */
  const fetchOverviewStats = async () => {
    loading.overview = true
    try {
      const { data } = await statisticsApi.getOverviewStatistics()
      if (data) {
        Object.assign(overviewStats, {
          ...data,
          merchantRating: Number(data.merchantRating || 0)
        })
      }
    } catch (error) {
      console.error('Failed to fetch overview stats:', error)
    } finally {
      loading.overview = false
    }
  }

  /**
   * 获取趋势统计数据
   * @param {number} days 天数 (7|30)
   */
  const fetchTrendStats = async (days = 7) => {
    loading.trend = true
    try {
      const { data } = await statisticsApi.getTrendStatistics({ days })
      if (data) {
        trendData.value = data
      }
    } catch (error) {
      console.error('Failed to fetch trend stats:', error)
    } finally {
      loading.trend = false
    }
  }

  return {
    todayStats,
    overviewStats,
    trendData,
    loading,
    fetchTodayStats,
    fetchOverviewStats,
    fetchTrendStats
  }
})
