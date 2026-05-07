import request from '@/utils/request'

// 订单监管原始请求实现，供订单列表、详情和异常订单查询使用。
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
