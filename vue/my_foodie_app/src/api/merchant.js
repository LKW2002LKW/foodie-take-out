import request from '../utils/request'

/**
 * 分页查询商户列表
 * @param {Object} params 
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} [params.name] - 商户名称过滤
 * @param {number} [params.sortType] - 排序方式: 1=距离排序
 * @param {number} [params.longitude] - 经度
 * @param {number} [params.latitude] - 纬度
 */
export const getMerchantPage = (params) => {
  return request({
    url: '/user/merchant/page',
    method: 'get',
    params
  })
}

/**
 * 查询商户详情
 * @param {number} id - 商户ID
 */
export const getMerchantDetail = (id) => {
  return request({
    url: `/user/merchant/${id}`,
    method: 'get'
  })
}

/**
 * 查询商户分类列表
 * @param {Object} params
 * @param {number} params.id - 商户ID (Restful path param handled in caller or pass separate)
 * Note: specific requirement says GET /user/merchant/{id}/categories
 * So we usually pass id as first arg.
 */
export const getMerchantCategories = (id, params = {}) => {
  return request({
    url: `/user/merchant/${id}/categories`,
    method: 'get',
    params
  })
}
