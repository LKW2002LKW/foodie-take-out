import { defineStore } from 'pinia'
import { reactive, ref } from 'vue'
import statisticsApi from '@/api/modules/statistics'

// 工作台数据仓库，集中管理今日统计、概览统计与趋势图数据。
export const useDashboardStore = defineStore('dashboard', () => {
  const todayStats = reactive({
    todayRevenue: 0,
    todayIncome: 0,
    totalOrderCount: 0,
    pendingOrderCount: 0,
    cancelledOrderCount: 0,
    completionRate: 0,
  })

  const overviewStats = reactive({
    totalRevenue: 0,
    totalOrders: 0,
    avgOrderAmount: 0,
    merchantRating: 0,
    totalReviews: 0,
  })

  const trendData = ref({
    dateList: [],
    revenueList: [],
    incomeList: [],
    orderCountList: [],
  })

  const loading = reactive({
    today: false,
    overview: false,
    trend: false,
  })

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

  const fetchOverviewStats = async () => {
    loading.overview = true
    try {
      const { data } = await statisticsApi.getOverviewStatistics()
      if (data) {
        Object.assign(overviewStats, {
          ...data,
          merchantRating: Number(data.merchantRating || 0),
        })
      }
    } catch (error) {
      console.error('Failed to fetch overview stats:', error)
    } finally {
      loading.overview = false
    }
  }

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
    fetchOverviewStats,
    fetchTodayStats,
    fetchTrendStats,
    loading,
    overviewStats,
    todayStats,
    trendData,
  }
})
