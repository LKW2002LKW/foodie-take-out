import request from '@/utils/request'

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
