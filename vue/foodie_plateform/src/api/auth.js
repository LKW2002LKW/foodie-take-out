import request from '@/utils/request'

/**
 * 平台端认证 API
 */
export default {
  login(data) {
    return request({
      url: '/admin/login',
      method: 'post',
      data
    })
  },
  logout() {
    return request({
      url: '/admin/logout',
      method: 'post'
    })
  },
  getInfo() {
    return request({
      url: '/admin/info',
      method: 'get'
    })
  }
}
