import { usePlatformStore } from '@/stores/modules/platformStore'
import { routerConfig } from '@/config/router'

// 前置守卫只处理登录态拦截与登录页回跳，不掺杂具体业务判断。
export const createBeforeEachGuard = () => (to) => {
  const platformStore = usePlatformStore()

  if (to.path === routerConfig.loginPath) {
    if (platformStore.isLoggedIn) {
      return routerConfig.homePath
    }
    return true
  }

  if (!platformStore.isLoggedIn) {
    return routerConfig.authRedirect
  }

  return true
}
