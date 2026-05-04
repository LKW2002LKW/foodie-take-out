import MainLayout from '@/layouts/MainLayout.vue'

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
