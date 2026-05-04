import request from './request'

// 添加购物车
export const addCart = (params) => {
  return request({
    url: '/user/shoppingCart/add',
    method: 'post',
    data: {
      ...params,
      number: 1,
    },
  })
}

// 减少/移除 购物车
export const subCart = (params) => {
  return request({
    url: '/user/shoppingCart/sub',
    method: 'post',
    data: params,
  })
}

// 获取购物车列表
export const getCartList = (merchantIds) => {
  return request({
    url: '/user/shoppingCart/list',
    method: 'get',
    params: { merchantIds },
  })
}

// 清空购物车
export const cleanCart = (merchantId) => {
  return request({
    url: '/user/shoppingCart/clean',
    method: 'delete',
    params: merchantId == null ? {} : { merchantId },
  })
}
