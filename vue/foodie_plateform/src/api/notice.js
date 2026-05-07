import request from '@/utils/request'

// 公告管理原始请求实现，供公告列表与发布流程复用。
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
