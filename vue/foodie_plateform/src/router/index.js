import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    hidden: true
  },
  {
    path: '/',
    component: () => import('../layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '数据统计', icon: 'Odometer' }
      },
      {
        path: 'merchant',
        name: 'Merchant',
        component: () => import('../views/merchant/index.vue'),
        meta: { title: '商户管理', icon: 'Shop' }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('../views/order/index.vue'),
        meta: { title: '订单监控', icon: 'List' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('../views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'finance',
        name: 'Finance',
        component: () => import('../views/finance/index.vue'),
        meta: { title: '财务管理', icon: 'Wallet' }
      },
      {
        path: 'review',
        name: 'Review',
        component: () => import('../views/review/index.vue'),
        meta: { title: '评价管理', icon: 'ChatDotRound' }
      },
      {
        path: 'report',
        name: 'Report',
        component: () => import('../views/report/index.vue'),
        meta: { title: '数据报表', icon: 'TrendCharts' }
      },
      {
        path: 'notice',
        name: 'Notice',
        component: () => import('../views/notice/index.vue'),
        meta: { title: '公告管理', icon: 'Bell' }
      },
      {
        path: 'config',
        name: 'Config',
        component: () => import('../views/config/index.vue'),
        meta: { title: '系统配置', icon: 'Setting' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('platform_token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
