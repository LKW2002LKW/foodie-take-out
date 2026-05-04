import MainLayout from '@/layouts/MainLayout.vue'

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
