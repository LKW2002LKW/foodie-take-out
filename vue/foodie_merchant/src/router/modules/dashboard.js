import MainLayout from '@/layouts/MainLayout.vue'

// 工作台模块路由，作为商家后台首页入口。
const dashboardRoutes = [
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/pages/dashboard/pages/Dashboard.vue'),
        meta: { title: '工作台' },
      },
    ],
  },
]

export default dashboardRoutes
