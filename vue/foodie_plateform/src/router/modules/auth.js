// 认证模块路由，仅承载登录等无需主布局的页面。
const authRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/auth/pages/Login.vue'),
    meta: { title: '管理员登录' },
  },
]

export default authRoutes
