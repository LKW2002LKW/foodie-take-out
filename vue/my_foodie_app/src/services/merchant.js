import request from '../utils/request'

/**
 * 商家分页列表
 * GET /user/merchant/page
 * @param {Object} params
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} [params.name] - 搜索商家名称
 * @param {number} [params.categoryId] - 分类 ID
 * @param {number} [params.sortType] - 排序方式 (0:综合, 1:距离, 2:销量, 3:评分, 4:起送最低, 5:配送最低)
 * @param {number} [params.longitude] - 用户经度
 * @param {number} [params.latitude] - 用户纬度
 */
export const getMerchantPage = (params) => {
  return request({
    url: '/user/merchant/page',
    method: 'get',
    params,
  })
}

/**
 * 查询商户详情
 */
export const getMerchantDetail = (id) => {
  return request({
    url: `/user/merchant/${id}`,
    method: 'get',
  })
}

/**
 * 查询商户的分类列表
 */
export const getMerchantCategories = (id) => {
  return request({
    url: `/user/merchant/${id}/categories`,
    method: 'get',
  })
}
