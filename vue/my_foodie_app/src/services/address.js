import request from '../utils/request'

/**
 * 查询地址列表
 */
export const getAddressList = () => {
  return request({ url: '/user/address/list', method: 'get' })
}

/**
 * 获取默认地址
 */
export const getDefaultAddress = () => {
  return request({ url: '/user/address/default', method: 'get' })
}

/**
 * 查询地址详情
 */
export const getAddressDetail = (id) => {
  return request({ url: `/user/address/${id}`, method: 'get' })
}

/**
 * 新增地址
 */
export const addAddress = (data) => {
  return request({ url: '/user/address', method: 'post', data })
}

/**
 * 修改地址
 */
export const updateAddress = (data) => {
  return request({ url: '/user/address', method: 'put', data })
}

/**
 * 单个删除地址
 */
export const deleteAddress = (id) => {
  return request({ url: `/user/address/${id}`, method: 'delete' })
}

/**
 * 批量删除地址 (核心新增)
 * DELETE /user/address/batch
 * Body: [id1, id2, ...]
 */
export const batchDeleteAddress = (ids) => {
  return request({
    url: '/user/address/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 设置默认地址
 */
export const setDefaultAddress = (id) => {
  return request({ url: `/user/address/default/${id}`, method: 'put' })
}
