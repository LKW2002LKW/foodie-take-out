// 认证模块路由，仅承载登录与注册等空白布局页面。
const authRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/auth/pages/Login.vue'),
    meta: { layout: 'blank', title: '登录' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/pages/auth/pages/Register.vue'),
    meta: { layout: 'blank', title: '注册' },
  },
]

export default authRoutes
