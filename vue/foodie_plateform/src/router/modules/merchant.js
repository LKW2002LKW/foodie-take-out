import MainLayout from '@/layouts/MainLayout.vue'

// 商户管理模块路由，集中挂载商户审核与监管页面。
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
