import { useMerchantStore } from '@/stores/modules/merchantStore'

// 前置守卫仅处理登录态与登录页回跳，不混入具体业务判断。
export const registerBeforeEachGuard = (router) => {
  router.beforeEach((to, from, next) => {
    const merchantStore = useMerchantStore()
    const requiresAuth = to.meta.requiresAuth !== false

    if (requiresAuth && !merchantStore.isLoggedIn) {
      next('/login')
      return
    }

    if ((to.path === '/login' || to.path === '/register') && merchantStore.isLoggedIn) {
      next('/')
      return
    }

    next()
  })
}
