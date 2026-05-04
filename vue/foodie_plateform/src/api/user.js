import request from '@/utils/request'

export default {
  getUserPage(params) {
    return request.get('/user/page', { params })
  },
  getUserDetail(id) {
    return request.get(`/user/${id}`)
  },
  enableUser(id) {
    return request.put(`/user/enable/${id}`)
  },
  disableUser(id) {
    return request.put(`/user/disable/${id}`)
  },
}
