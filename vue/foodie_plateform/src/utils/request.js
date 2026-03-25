import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: 'http://localhost:8081/platform',
  timeout: 5000
})

// Request interceptor
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('platform_token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// Response interceptor
service.interceptors.response.use(
  response => {
    const res = response.data
    // If the custom code is not 1, it is judged as an error.
    if (res.code !== 1) {
      ElMessage.error(res.msg || '系统错误')
      
      // 假设code非1时需要处理的一些特殊情况，比如token过期等
      // if (res.code === 50008 || res.code === 50012 || res.code === 50014) {
      //   // to re-login
      // }
      return Promise.reject(new Error(res.msg || 'Error'))
    } else {
      return res
    }
  },
  error => {
    console.log('err' + error) // for debug
    ElMessage.error(error.message)
    return Promise.reject(error)
  }
)

export default service
