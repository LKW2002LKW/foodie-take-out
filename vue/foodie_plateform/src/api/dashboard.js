import request from '@/utils/request'

/**
 * 平台端仪表盘 API
 */
export default {
  /**
   * 获取今日核心统计指标
   */
  getStats() {
    return request({
      url: '/admin/dashboard/data',
      method: 'get'
    })
  },
  /**
   * 获取趋势图表数据
   * @param {Object} params { days: 7 | 30 }
   */
  getTrend(params) {
    return request({
      url: '/admin/dashboard/trend',
      method: 'get',
      params
    })
  }
}
