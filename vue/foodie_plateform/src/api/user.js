import request from '@/utils/request'

// 用户管理原始请求实现，供用户列表、详情与状态控制使用。
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
