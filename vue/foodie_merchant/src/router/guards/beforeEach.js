import { useMerchantStore } from '@/stores/modules/merchantStore'

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
