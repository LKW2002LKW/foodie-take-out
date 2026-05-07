import { routerConfig } from '@/config/router'
import { useUserStore } from '@/stores/modules/userStore'

// 前置守卫只处理登录态拦截，不在这里混入页面级业务逻辑。
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
