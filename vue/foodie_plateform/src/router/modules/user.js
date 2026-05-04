import MainLayout from '@/layouts/MainLayout.vue'

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
