import { routerConfig } from '@/config/router'
import { useUserStore } from '@/stores/modules/userStore'

export const registerBeforeEachGuard = (router) => {
  router.beforeEach((to, from, next) => {
    const userStore = useUserStore()

    if (to.meta.requiresAuth && !userStore.token) {
      next(routerConfig.authRedirect)
      return
    }

    next()
  })
}
