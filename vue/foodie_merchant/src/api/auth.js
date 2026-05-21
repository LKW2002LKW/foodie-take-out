import request from '@/utils/request'

/**
 * 认证相关 API
 */
export default {
  /**
   * 商户登录
   * @param {Object} data { username, password }
   */
  login(data) {
    return request({
      url: '/merchant/admin/login',
      method: 'post',
      data
    })
  },

  /**
   * 商户注册
   * @param {Object} data 商户注册信息
   */
  register(data) {
    return request({
      url: '/merchant/admin/register',
      method: 'post',
      data
    })
  },

  /**
   * 获取当前商户信息
   */
  getMerchantInfo() {
    return request({
      url: '/merchant/admin/info',
      method: 'get'
    })
  }
}
