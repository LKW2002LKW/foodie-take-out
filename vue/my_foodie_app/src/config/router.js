import { createWebHistory } from 'vue-router'

export const routerConfig = {
  authRedirect: '/login',
  defaultTitle: '吃货联盟',
  history: createWebHistory(import.meta.env.BASE_URL),
  whiteList: ['/login', '/register'],
}
