import request from '../utils/request'

/**
 * 提交订单
 * @param {Object} data 
 * @param {number} data.addressBookId - 地址ID
 * @param {number|string} data.merchantId - 商户ID
 * @param {number} data.payMethod - 支付方式 1微信 2支付宝
 * @param {string} [data.remark] - 备注
 * @param {number} [data.packAmount] - 打包费
 * @param {number} [data.amount] - 总金额 // Backend usually calculates, but might need validation
 */
export const submitOrder = (data) => {
  return request({
    url: '/user/order/submit',
    method: 'post',
    data
  })
}

/**
 * 订单支付
 * @param {Object} data
 * @param {string} data.orderNumber - 订单号
 * @param {number} data.payMethod - 支付方式
 */
export const payment = (data) => {
  return request({
    url: '/user/order/payment',
    method: 'post',
    data
  })
}

/**
 * 支付成功回调 (Mock)
 * @param {string} orderNumber 
 */
export const paySuccess = (orderNumber) => {
    // Usually a GET or POST, prompt says POST /user/order/paySuccess/{orderNumber}
    return request({
        url: `/user/order/paySuccess/${orderNumber}`,
        method: 'post'
    })
}


/**
 * 8.2 订单分页查询
 * @param {Object} params
 * @param {number} params.page
 * @param {number} params.pageSize
 * @param {number} [params.status]
 */
export const getOrderPage = (params) => {
  return request({
    url: '/user/order/page',
    method: 'get',
    params
  })
}

/**
 * 8.3 查询订单详情 (根据ID)
 * GET /user/order/detail/{id}
 * @param {number} id 
 */
export const getOrderDetail = (id) => {
  return request({
    url: `/user/order/detail/${id}`,
    method: 'get'
  })
}

/**
 * 查询订单详情 (根据订单号)
 * GET /user/order/detail/by-order-number/{orderNumber}
 * @param {string} orderNumber
 */
export const getOrderDetailByNumber = (orderNumber) => {
  return request({
    url: `/user/order/detail/by-order-number/${orderNumber}`,
    method: 'get'
  })
}

/**
 * 8.4 订单状态跟踪
 */
export const getOrderTrack = (id) => {
  return request({
    url: `/user/order/track/${id}`,
    method: 'get'
  })
}

/**
 * 8.5 取消订单
 * @param {Object} data { id, cancelReason }
 */
export const cancelOrder = (data) => {
  return request({
    url: '/user/order/cancel',
    method: 'put',
    data
  })
}

/**
 * 8.6 评价订单
 * @param {Object} data { orderId, rating, content, images }
 */
export const reviewOrder = (data) => {
  return request({
    url: '/user/order/review',
    method: 'post',
    data
  })
}

/**
 * 8.7 删除订单
 */
export const deleteOrder = (id) => {
  return request({
    url: `/user/order/${id}`,
    method: 'delete'
  })
}

