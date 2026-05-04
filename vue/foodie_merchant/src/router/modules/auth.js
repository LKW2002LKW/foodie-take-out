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
