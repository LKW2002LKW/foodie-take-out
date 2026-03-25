import request from '@/utils/request'

export default {
  /**
   * 分页查询订单
   * @param {Object} params { page, pageSize, orderNumber, phone, status, beginTime, endTime }
   */
  getOrderPage(params) {
    return request({
      url: '/merchant/order/page',
      method: 'get',
      params
    })
  },

  /**
   * 获取订单统计 (待接单、待派送、配送中)
   */
  getOrderStatistics() {
    return request({
      url: '/merchant/order/statistics',
      method: 'get'
    })
  },

  /**
   * 查询订单详情
   * @param {String|Number} id 
   */
  getOrderDetails(id) {
    return request({
      url: `/merchant/order/details/${id}`,
      method: 'get'
    })
  },

  /**
   * 接单
   * @param {Object} data { id }
   */
  confirmOrder(data) {
    return request({
      url: '/merchant/order/confirm',
      method: 'put',
      data
    })
  },

  /**
   * 拒单
   * @param {Object} data { id, reason }
   */
  rejectOrder(data) {
    return request({
      url: '/merchant/order/rejection',
      method: 'put',
      data
    })
  },

  /**
   * 派送订单
   * @param {Object} data { id }
   */
  deliveryOrder(data) {
    return request({
      url: '/merchant/order/delivery',
      method: 'put',
      data
    })
  },

  /**
   * 完成订单
   * @param {Object} data { id }
   */
  completeOrder(data) {
    return request({
      url: '/merchant/order/complete',
      method: 'put',
      data
    })
  },

  /**
   * 取消订单
   * @param {Object} data { id, reason }
   */
  cancelOrder(data) {
    return request({
      url: '/merchant/order/cancel',
      method: 'put',
      data
    })
  }
}
