import request from '../utils/request'

// 1. 发送验证码
// POST http://localhost:8083/user/code?phone=...
export const sendCode = (phone) => {
  return request({
    url: '/user/code',
    method: 'post',
    params: { phone }
  })
}

// 2. 用户注册
// POST http://localhost:8083/user/register
export const register = (phone, code, password, nickname) => {
  return request({
    url: '/user/register',
    method: 'post',
    data: {
      phone,
      code,
      password,
      nickname
    }
  })
}

// 3. 验证码登录
// POST http://localhost:8083/user/login (loginType: 1)
export const loginByCode = (phone, code) => {
  return request({
    url: '/user/login',
    method: 'post',
    data: {
      phone,
      code,
      loginType: 1
    }
  })
}

// 4. 密码登录
// POST http://localhost:8083/user/login (loginType: 2)
export const loginByPassword = (phone, password) => {
  return request({
    url: '/user/login',
    method: 'post',
    data: {
      phone,
      password,
      loginType: 2
    }
  })
}

// 5. 微信登录
// POST http://localhost:8083/user/wechat/login
export const loginByWeChat = (code) => {
  return request({
    url: '/user/wechat/login',
    method: 'post',
    data: { code }
  })
}

// 6. 获取当前用户信息
// GET http://localhost:8083/user/info
export const getUserInfo = () => {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

// 7. 退出登录
// POST http://localhost:8083/user/logout
export const logout = () => {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

/**
 * 1. 获取用户信息
 */
export const getUserProfile = () => {
  return request({
    url: '/user/profile/info',
    method: 'get'
  })
}

/**
 * 2. 更新用户资料
 * @param {Object} data { nickname, avatar, sex, birthday }
 */
export const updateUserProfile = (data) => {
  return request({
    url: '/user/profile/update',
    method: 'put',
    data
  })
}

/**
 * 3. 修改密码
 * @param {Object} data { oldPassword, newPassword, confirmPassword }
 */
export const updatePassword = (data) => {
  return request({
    url: '/user/profile/password',
    method: 'put',
    data
  })
}

/**
 * 4. 发送绑定手机验证码
 * @param {string} phone 
 */
export const sendBindPhoneCode = (phone) => {
  return request({
    url: '/user/profile/sms-code',
    method: 'post',
    params: { phone }
  })
}

/**
 * 5. 绑定手机号
 * @param {Object} data { phone, code, password }
 */
export const bindPhone = (data) => {
  return request({
    url: '/user/profile/bind-phone',
    method: 'post',
    data
  })
}

/**
 * 6. 绑定微信
 * @param {Object} data { openid }
 */
export const bindWeChat = (data) => {
  return request({
    url: '/user/profile/wechat',
    method: 'post',
    data
  })
}
