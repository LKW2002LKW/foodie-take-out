import request from '../utils/request'

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
 * @param {number} [params.ratingFilter] - 评分筛选: 5=好评(5星), 3=中评(3-4星), 1=差评(1-2星)
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
