import { reactive } from 'vue'
import { defineStore } from 'pinia'

const createStats = () => ({
  todayOrderCount: 0,
  todayRevenue: 0,
  todayNewUserCount: 0,
  pendingMerchantCount: 0,
  totalUserCount: 0,
  totalMerchantCount: 0,
  openMerchantCount: 0,
})

const createTrendData = () => ({
  dateList: [],
  orderCountList: [],
  revenueList: [],
  userCountList: [],
})

// 工作台数据仓库，集中维护统计卡片与趋势图原始数据。
export const useDashboardStore = defineStore('dashboard', () => {
  const stats = reactive(createStats())
  const trendData = reactive(createTrendData())

  const updateStats = (payload = {}) => {
    Object.assign(stats, createStats(), payload)
  }

  const updateTrendData = (payload = {}) => {
    Object.assign(trendData, createTrendData(), payload)
  }

  return {
    stats,
    trendData,
    updateStats,
    updateTrendData,
  }
})
