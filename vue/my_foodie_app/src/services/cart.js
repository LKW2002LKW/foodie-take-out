import request from '../utils/request'

/**
 * 添加购物车
 * @param {object} data - ShoppingCartDTO
 */
export const addCart = (data) => {
  return request({
    url: '/user/shoppingCart/add',
    method: 'post',
    data,
  })
}

/**
 * 减少/移除购物车
 * @param {object} data - ShoppingCartDTO
 */
export const subCart = (data) => {
  return request({
    url: '/user/shoppingCart/sub',
    method: 'post',
    data,
  })
}

/**
 * 获取购物车列表
 * @param {string} merchantIds - 多个ID用逗ナス(,)分隔
 */
export const getCartList = (merchantIds) => {
  return request({
    url: '/user/shoppingCart/list',
    method: 'get',
    params: { merchantIds },
  })
}

/**
 * 清空购物车
 * @param {number} merchantId
 */
export const cleanCart = (merchantId) => {
  return request({
    url: '/user/shoppingCart/clean',
    method: 'delete',
    params: { merchantId },
  })
}
