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
