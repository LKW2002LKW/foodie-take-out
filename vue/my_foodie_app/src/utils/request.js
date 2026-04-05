import axios from 'axios'
import { useUserStore } from '../store/modules/user'
import { showFailToast } from 'vant'
import router from '../router'

const service = axios.create({
  //baseURL: 'http://0.0.0.0:8083', // Removed for Proxy
  //baseURL: '', // Use relative path to trigger Vite proxy
  timeout: 5000
})

// Request interceptor
service.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      // The API requires 'user-token' header
      config.headers['user-token'] = userStore.token
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
    console.log('Response:', res) // Debug log
    // You can handle global errors here based on custom code
    // The user's documentation says success is code: 1. However some new APIs use 200.
    if (res.code !== 1 && res.code !== 200) {
      console.warn('Response Code not 1 or 200:', res) // Debug log
      // showFailToast(res.msg || 'Error')
      // You might want to let the calling component handle the error or throw it here
      // For now, we'll return the whole response object so components can check res.code
      return res
    }
    return res
  },
  error => {
    console.error('Request Interceptor Error:', error) // Debug log
    if (error.response && error.response.status === 401) {
      const userStore = useUserStore()
      userStore.clearToken()
      showFailToast('登录已过期，请重新登录')
      router.push('/login')
    } else {
      showFailToast(error.message || 'Request Error')
    }
    return Promise.reject(error)
  }
)

export default service
