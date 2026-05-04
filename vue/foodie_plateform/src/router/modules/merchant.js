import MainLayout from '@/layouts/MainLayout.vue'

const merchantRoutes = [
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: 'merchant',
        name: 'Merchant',
        component: () => import('@/pages/merchant/pages/MerchantList.vue'),
        meta: { title: '商户管理', icon: 'Shop' },
      },
    ],
  },
]

export default merchantRoutes
