import MainLayout from '@/layouts/MainLayout.vue'

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
