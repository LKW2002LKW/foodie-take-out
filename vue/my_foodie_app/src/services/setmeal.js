import request from '../utils/request'

export const getSetmealList = (params) => {
  return request({
    url: '/user/setmeal/list',
    method: 'get',
    params,
  })
}

export const getSetmealDetail = (id) => {
  return request({
    url: `/user/setmeal/${id}`,
    method: 'get',
  })
}
