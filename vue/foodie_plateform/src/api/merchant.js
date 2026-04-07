import request from '@/utils/request'

/**
 * 平台端商户管理 API
 */
export default {
  /**
   * 分页获取商户列表
   */
  getMerchantPage(params) {
    return request({
      url: '/admin/merchant/page',
      method: 'get',
      params
    })
  },
  /**
   * 获取商户详情
   */
  getMerchantDetail(id) {
    return request({
      url: `/admin/merchant/${id}`,
      method: 'get'
    })
  },
  /**
   * 审核商户
   * @param {Object} data { id, auditStatus, auditReason }
   */
  auditMerchant(data) {
    return request({
      url: '/admin/merchant/audit',
      method: 'put',
      data
    })
  },
  /**
   * 启用商户
   */
  enableMerchant(id) {
    return request({
      url: `/admin/merchant/enable/${id}`,
      method: 'put'
    })
  },
  /**
   * 禁用商户
   */
  disableMerchant(id) {
    return request({
      url: `/admin/merchant/disable/${id}`,
      method: 'put'
    })
  }
}
