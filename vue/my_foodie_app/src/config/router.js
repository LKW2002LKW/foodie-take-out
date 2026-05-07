import { createWebHistory } from 'vue-router'

// 路由通用配置集中收口，便于守卫与路由实例共享。
export const routerConfig = {
  authRedirect: '/login',
  defaultTitle: '吃货联盟',
  history: createWebHistory(import.meta.env.BASE_URL),
  whiteList: ['/login', '/register'],
}
