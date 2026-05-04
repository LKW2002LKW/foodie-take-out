import request from './request'

/**
 * 查询套餐列表
 * @param {Object} params
 * @param {number} params.merchantId - 商户ID
 * @param {number} [params.categoryId] - 分类ID
 */
export const getSetmealList = (params) => {
  return request({
    url: '/user/setmeal/list',
    method: 'get',
    params
  })
}

/**
 * 查询套餐详情
 * @param {number} id 
 */
export const getSetmealDetail = (id) => {
  return request({
    url: `/user/setmeal/${id}`,
    method: 'get'
  })
}
