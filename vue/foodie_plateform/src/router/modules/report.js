import MainLayout from '@/layouts/MainLayout.vue'

// 报表模块路由，集中挂载平台分析报表页面。
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
