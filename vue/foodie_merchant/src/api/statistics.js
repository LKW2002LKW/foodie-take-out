import request from '@/utils/request'

export default {
  /**
   * 获取今日统计数据
   */
  getTodayStatistics() {
    return request({
      url: '/merchant/statistics/today',
      method: 'get'
    })
  },

  /**
   * 获取趋势统计数据 (近7天或30天)
   * @param {Object} params { days: 7 | 30 }
   */
  getTrendStatistics(params) {
    return request({
      url: '/merchant/statistics/trend',
      method: 'get',
      params
    })
  },

  /**
   * 获取概览统计数据
   */
  getOverviewStatistics() {
    return request({
      url: '/merchant/statistics/overview',
      method: 'get'
    })
  }
}
