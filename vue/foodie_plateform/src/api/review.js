import request from '@/utils/request'

// 评价监管原始请求实现，覆盖评价列表、预警与删除处理。
export default {
  getReviewPage(params) {
    return request.get('/review/page', { params })
  },
  getWarningReviewPage(params) {
    return request.get('/review/warning', { params })
  },
  deleteReview(id) {
    return request.delete(`/review/${id}`)
  },
}
