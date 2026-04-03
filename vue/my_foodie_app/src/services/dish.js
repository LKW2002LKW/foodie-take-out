import request from '../utils/request'

export const getDishList = (params) => {
  return request({
    url: '/user/dish/list',
    method: 'get',
    params,
  })
}

export const getDishDetail = (id) => {
  return request({
    url: `/user/dish/${id}`,
    method: 'get',
  })
}
