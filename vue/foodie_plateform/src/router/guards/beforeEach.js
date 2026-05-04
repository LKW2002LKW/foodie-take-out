import { usePlatformStore } from '@/stores/modules/platformStore'
import { routerConfig } from '@/config/router'

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
