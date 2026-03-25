import request from '@/utils/request'

export default {
  /**
   * 评价分页查询
   * @param {Object} params { page, pageSize, rating, replied, beginTime, endTime }
   */
  getReviewPage(params) {
    return request({
      url: '/merchant/review/page',
      method: 'get',
      params
    })
  },

  /**
   * 差评列表查询
   * @param {Object} params { page, pageSize }
   */
  getBadReviewPage(params) {
    return request({
      url: '/merchant/review/bad-reviews',
      method: 'get',
      params
    })
  },

  /**
   * 商户回复评价
   * @param {Object} data { id, merchantReply }
   */
  replyReview(data) {
    return request({
      url: '/merchant/review/reply',
      method: 'put',
      data
    })
  }
}
