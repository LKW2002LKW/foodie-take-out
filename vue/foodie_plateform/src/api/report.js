import request from '@/utils/request'

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
