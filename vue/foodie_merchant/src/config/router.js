import { createWebHistory } from 'vue-router'

// 路由通用配置集中管理，供路由实例和守卫复用。
export const routerConfig = {
  authRedirect: '/login',
  defaultTitle: '吃货联盟商家端',
  history: createWebHistory(),
}
