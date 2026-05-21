import request from './request'

/**
 * 提交订单评价
 * @param {Object} data
 * @param {number} data.orderId - 订单ID
 * @param {number} data.rating - 评分 1-5
 * @param {string} [data.content] - 评价内容
 * @param {string} [data.images] - 图片URL，逗号分隔
 */
export const submitOrderReview = (data) => {
  return request({
    url: '/user/order/review',
    method: 'post',
    data
  })
}

export const reviewOrder = submitOrderReview

/**
 * 查询商户评价统计
 * @param {number} merchantId 
 */
export const getReviewStats = (merchantId) => {
  return request({
    url: `/user/review/stats/${merchantId}`,
    method: 'get'
  })
}

/**
 * 分页查询评价列表
 * @param {Object} params
 * @param {number} params.merchantId - 商户ID
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页条数
 * @param {number} [params.ratingFilter] - 评分筛选: 0=全部, 5=好评(4-5星), 3=中评(3星), 1=差评(1-2星)
 * @param {boolean} [params.hasImage] - 是否有图
 */
export const getReviewPage = (params) => {
  return request({
    url: '/user/review/page',
    method: 'get',
    params
  })
}

/**
 * 查询评价详情
 * @param {number} id 
 */
export const getReviewDetail = (id) => {
  return request({
    url: `/user/review/${id}`,
    method: 'get'
  })
}

/**
 * 我的评价分页
 * @param {Object} params
 * @param {number} params.page
 * @param {number} params.pageSize
 */
export const getMyReviewPage = (params) => {
  return request({
    url: '/user/review/my',
    method: 'get',
    params
  })
}

/**
 * 删除我的评价
 * @param {number} reviewId
 */
export const deleteMyReview = (reviewId) => {
  return request({
    url: `/user/review/${reviewId}`,
    method: 'delete'
  })
}
