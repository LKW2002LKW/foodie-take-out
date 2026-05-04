import MainLayout from '@/layouts/MainLayout.vue'

const reportRoutes = [
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: 'report',
        name: 'Report',
        component: () => import('@/pages/report/pages/ReportCenter.vue'),
        meta: { title: '数据报表', icon: 'TrendCharts' },
      },
    ],
  },
]

export default reportRoutes
