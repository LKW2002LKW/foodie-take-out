import { createWebHistory } from 'vue-router'

export const routerConfig = {
  authRedirect: '/login',
  defaultTitle: '吃货联盟商家端',
  history: createWebHistory(),
}
