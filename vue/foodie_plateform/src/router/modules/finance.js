import MainLayout from '@/layouts/MainLayout.vue'

// 财务模块路由，承接平台资金中心相关页面。
const financeRoutes = [
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: 'finance',
        name: 'Finance',
        component: () => import('@/pages/finance/pages/FinanceCenter.vue'),
        meta: { title: '财务管理', icon: 'Wallet' },
      },
    ],
  },
]

export default financeRoutes
