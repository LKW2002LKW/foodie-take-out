import request from '@/utils/request'

// 平台报表原始请求实现，统一提供营收、订单与用户增长报表数据。
export default {
  getRevenueReport(params) {
    return request.get('/report/revenue', { params })
  },
  getOrderReport(params) {
    return request.get('/report/order', { params })
  },
  getUserGrowthReport(params) {
    return request.get('/report/user-growth', { params })
  },
  getHotDishesReport() {
    return request.get('/report/hot-dishes')
  },
}
