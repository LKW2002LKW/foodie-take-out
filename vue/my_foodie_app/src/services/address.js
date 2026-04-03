import request from '../utils/request'

export const getAddressList = () => {
  return request({
    url: '/user/addressBook/list',
    method: 'get',
  })
}

export const addAddress = (data) => {
  return request({
    url: '/user/addressBook',
    method: 'post',
    data,
  })
}

export const updateAddress = (data) => {
  return request({
    url: '/user/addressBook',
    method: 'put',
    data,
  })
}

export const deleteAddress = (id) => {
  return request({
    url: `/user/addressBook/${id}`,
    method: 'delete',
  })
}

export const setDefaultAddress = (id) => {
  return request({
    url: `/user/addressBook/default?id=${id}`,
    method: 'put',
  })
}

export const getDefaultAddress = () => {
  return request({
    url: '/user/addressBook/default',
    method: 'get',
  })
}

export const getAddressDetail = (id) => {
  return request({
    url: `/user/addressBook/${id}`,
    method: 'get',
  })
}
