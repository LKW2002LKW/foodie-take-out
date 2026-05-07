// 认证模块路由，仅承载登录与注册等无需主框架的页面。
const authRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/auth/pages/Login.vue'),
    meta: {
      requiresAuth: false,
      title: '商户登录',
    },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/pages/auth/pages/Register.vue'),
    meta: {
      requiresAuth: false,
      title: '商户注册',
    },
  },
]

export default authRoutes
