import request from '../utils/request'

export const loginByCode = (phone, code) => {
  return request({
    url: '/user/login',
    method: 'post',
    data: { phone, code, loginType: 1 },
  })
}

export const loginByPassword = (phone, password) => {
  return request({
    url: '/user/login',
    method: 'post',
    data: { phone, password, loginType: 2 },
  })
}

export const sendCode = (phone) => {
  return request({
    url: '/user/code',
    method: 'post',
    params: { phone },
  })
}

export const getUserInfo = () => {
  return request({
    url: '/user/info',
    method: 'get',
  })
}
