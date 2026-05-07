import { ElMessage } from 'element-plus'
import { routerConfig } from '@/config/router'
import { PLATFORM_TOKEN_KEY } from '@/constants/storageKeys'

// 拦截器统一处理平台令牌透传、接口失败提示与登录失效跳转。
export const setupInterceptors = (service) => {
  service.interceptors.request.use(
    (config) => {
      const token = localStorage.getItem(PLATFORM_TOKEN_KEY)
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      return config
    },
    (error) => Promise.reject(error),
  )

  service.interceptors.response.use(
    (response) => {
      const res = response.data
      if (res.code !== 1) {
        ElMessage.error(res.msg || '系统错误')
        if (res.code === 401 || res.code === 403) {
          localStorage.removeItem(PLATFORM_TOKEN_KEY)
          window.location.replace(routerConfig.authRedirect)
        }
        return Promise.reject(new Error(res.msg || 'Error'))
      }
      return res
    },
    (error) => {
      ElMessage.error(error.message || '请求失败')
      return Promise.reject(error)
    },
  )
}
