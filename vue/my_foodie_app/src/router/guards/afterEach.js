import { routerConfig } from '@/config/router'

// 后置守卫统一处理页面标题，保持页面组件简洁。
export const registerAfterEachGuard = (router) => {
  router.afterEach((to) => {
    document.title = to.meta.title || routerConfig.defaultTitle
  })
}
