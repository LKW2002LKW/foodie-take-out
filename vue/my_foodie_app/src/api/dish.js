import request from './request'

/**
 * 查询菜品列表
 * @param {Object} params
 * @param {number} params.merchantId - 商户ID
 * @param {number} [params.categoryId] - 分类ID
 */
export const getDishList = (params) => {
  return request({
    url: '/user/dish/list',
    method: 'get',
    params
  })
}

/**
 * 查询菜品详情
 * @param {number} id 
 */
export const getDishDetail = (id) => {
  return request({
    url: `/user/dish/${id}`,
    method: 'get'
  })
}
