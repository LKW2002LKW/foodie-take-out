import { showFailToast } from 'vant'
import router from '@/router'
import { routerConfig } from '@/config/router'
import { useUserStore } from '@/stores/modules/userStore'

// 拦截器统一处理令牌透传、通用响应结构与登录失效跳转。
export const setupInterceptors = (service) => {
  service.interceptors.request.use(
    (config) => {
      const userStore = useUserStore()

      if (userStore.token) {
        config.headers['user-token'] = userStore.token
      }

      return config
    },
    (error) => Promise.reject(error),
  )

  service.interceptors.response.use(
    (response) => {
      const res = response.data

      if (res.code !== 1 && res.code !== 200) {
        return res
      }

      return res
    },
    (error) => {
      if (error.response?.status === 401) {
        const userStore = useUserStore()
        userStore.clearToken()
        showFailToast('登录已过期，请重新登录')
        router.push(routerConfig.authRedirect)
      } else {
        showFailToast(error.message || 'Request Error')
      }

      return Promise.reject(error)
    },
  )
}
