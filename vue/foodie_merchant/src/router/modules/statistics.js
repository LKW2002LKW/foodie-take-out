import MainLayout from '@/layouts/MainLayout.vue'

// 报表模块路由，集中销售报表与用户分析等统计页面。
const statisticsRoutes = [
  {
    path: '/statistics',
    component: MainLayout,
    children: [
      {
        path: 'sales',
        name: 'StatisticsSales',
        component: () => import('@/pages/statistics/pages/SalesReport.vue'),
        meta: { title: '销售报表' },
      },
      {
        path: 'user',
        name: 'StatisticsUser',
        component: () => import('@/pages/statistics/pages/UserAnalysis.vue'),
        meta: { title: '用户分析' },
      },
    ],
  },
]

export default statisticsRoutes
