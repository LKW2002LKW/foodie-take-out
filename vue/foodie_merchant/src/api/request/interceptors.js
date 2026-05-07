import { ElMessage } from 'element-plus'
import { routerConfig } from '@/config/router'
import { useMerchantStore } from '@/stores/modules/merchantStore'

// 拦截器统一处理商家端令牌透传、认证失效与错误提示。
export const registerRequestInterceptors = (service) => {
  service.interceptors.request.use(
    (config) => {
      const merchantStore = useMerchantStore()
      if (merchantStore.token) {
        config.headers.Authorization = `Bearer ${merchantStore.token}`
      }
      return config
    },
    (error) => Promise.reject(error),
  )

  service.interceptors.response.use(
    (response) => response.data,
    (error) => {
      if (error.response?.status === 401 || error.response?.status === 403) {
        const merchantStore = useMerchantStore()
        merchantStore.clearAuth()
        ElMessage.error('认证失败，请重新登录')
        window.location.replace(routerConfig.authRedirect)
      } else {
        ElMessage.error(error.message || '请求失败')
      }

      return Promise.reject(error)
    },
  )
}
