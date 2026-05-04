const authRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/auth/pages/Login.vue'),
    meta: { title: '管理员登录' },
  },
]

export default authRoutes
