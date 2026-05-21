import MainLayout from '@/layouts/MainLayout.vue'

// 用户管理模块路由，承接平台用户列表与分析页面。
const userRoutes = [
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: 'user',
        name: 'User',
        component: () => import('@/pages/user/pages/UserList.vue'),
        meta: { title: '用户管理', icon: 'User' },
      },
    ],
  },
]

export default userRoutes
