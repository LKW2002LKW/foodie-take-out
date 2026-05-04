import { routerConfig } from '@/config/router'

export const registerAfterEachGuard = (router) => {
  router.afterEach((to) => {
    document.title = to.meta.title || routerConfig.defaultTitle
  })
}
