import MainLayout from '@/layouts/MainLayout.vue'

// 财务模块路由，集中挂载结算、收入与充值相关页面。
const financeRoutes = [
  {
    path: '/finance',
    component: MainLayout,
    children: [
      {
        path: 'settlement',
        name: 'FinanceSettlement',
        component: () => import('@/pages/finance/pages/Settlement.vue'),
        meta: { title: '结算记录' },
      },
      {
        path: 'income',
        name: 'FinanceIncome',
        component: () => import('@/pages/finance/pages/IncomeRecord.vue'),
        meta: { title: '收入明细' },
      },
      {
        path: 'recharge',
        name: 'FinanceRecharge',
        component: () => import('@/pages/finance/pages/Recharge.vue'),
        meta: { title: '充值记录' },
      },
      {
        path: 'config',
        name: 'FinanceConfig',
        component: () => import('@/pages/finance/pages/Config.vue'),
        meta: { title: '结算配置' },
      },
    ],
  },
]

export default financeRoutes
