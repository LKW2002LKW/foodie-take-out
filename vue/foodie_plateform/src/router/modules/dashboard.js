import MainLayout from '@/layouts/MainLayout.vue'

// 工作台模块路由，作为平台端首页入口。
const dashboardRoutes = [
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/pages/dashboard/pages/Dashboard.vue'),
        meta: { title: '数据统计', icon: 'Odometer' },
      },
    ],
  },
]

export default dashboardRoutes
