import request from '../utils/request'

export const getMerchantPage = (params) => {
  return request({
    url: '/user/merchant/page',
    method: 'get',
    params,
  })
}

export const getMerchantDetail = (id) => {
  return request({
    url: `/user/merchant/${id}`,
    method: 'get',
  })
}

export const getMerchantCategories = (id) => {
  return request({
    url: `/user/merchant/${id}/categories`,
    method: 'get',
  })
}
