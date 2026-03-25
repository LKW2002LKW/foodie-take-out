import request from '@/utils/request'

// 查询地址列表
export function getAddressList() {
  return request({
    url: '/user/address/list',
    method: 'get',
  })
}

// 新增地址
export function addAddress(data) {
  return request({
    url: '/user/address',
    method: 'post',
    data
  })
}

// 修改地址
export function updateAddress(data) {
  return request({
    url: '/user/address',
    method: 'put',
    data
  })
}

// 删除地址
export function deleteAddress(id) {
  return request({
    url: `/user/address/${id}`,
    method: 'delete'
  })
}

// 设置默认地址
export function setDefaultAddress(id) {
  return request({
    url: `/user/address/default/${id}`,
    method: 'put'
  })
}

// 获取默认地址
export function getDefaultAddress() {
  return request({
    url: '/user/address/default',
    method: 'get'
  })
}