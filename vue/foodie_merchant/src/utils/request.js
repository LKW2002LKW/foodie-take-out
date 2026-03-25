import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const service = axios.create({
  baseURL: 'http://localhost:8082', // 你的后端接口地址
  timeout: 5000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 在发送请求之前做些什么
    const token = localStorage.getItem('merchant_token')
    
    if (token) {
      // 这里的 token 必须是字符串，不能是 [object Object]
      // 建议在 localStorage.setItem 时确保只存入字符串
      config.headers['Authorization'] = `Bearer ${token}`
      // 或者使用自定义 header
      // config.headers['merchant-token'] = token
    }
    return config
  },
  error => {
    // 对请求错误做些什么
    console.log(error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    // 这里可以根据后端返回的状态码做统一处理
    return res
  },
  error => {
    console.log('err' + error)
    if (error.response && error.response.status === 403) {
      ElMessage.error('认证失败，请重新登录')
      // 可以跳转到登录页
      // window.location.href = '/login'
    } else {
      ElMessage.error(error.message)
    }
    return Promise.reject(error)
  }
)

export default service
