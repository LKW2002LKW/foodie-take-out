import request from '@/utils/request'

export default {
  getOrderPage(params) {
    return request.get('/order/page', { params })
  },
  getOrderDetail(id) {
    return request.get(`/order/${id}`)
  },
  getAbnormalOrderPage(params) {
    return request.get('/order/abnormal', { params })
  },
}
