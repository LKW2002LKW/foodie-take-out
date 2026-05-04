import request from '@/utils/request'

export default {
  getNoticePage(params) {
    return request.get('/notice/page', { params })
  },
  deleteNotice(id) {
    return request.delete(`/notice/${id}`)
  },
  publishNotice(data) {
    return request.post('/notice/publish', data)
  },
  saveNoticeDraft(data) {
    return request.post('/notice/draft', data)
  },
}
