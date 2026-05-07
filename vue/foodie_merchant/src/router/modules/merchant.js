import MainLayout from '@/layouts/MainLayout.vue'

// 商户模块路由，聚合资料维护与员工管理页面。
const merchantRoutes = [
  {
    path: '/merchant',
    component: MainLayout,
    children: [
      {
        path: 'profile',
        name: 'MerchantProfile',
        component: () => import('@/pages/merchant/pages/Profile.vue'),
        meta: { title: '商户资料' },
      },
      {
        path: 'staff',
        name: 'MerchantStaff',
        component: () => import('@/pages/merchant/pages/Staff.vue'),
        meta: { title: '商户员工' },
      },
    ],
  },
]

export default merchantRoutes
