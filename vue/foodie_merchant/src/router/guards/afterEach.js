import { routerConfig } from '@/config/router'

// 后置守卫统一处理页面标题，避免每个页面单独设置。
export const registerAfterEachGuard = (router) => {
  router.afterEach((to) => {
    document.title = to.meta.title || routerConfig.defaultTitle
  })
}
