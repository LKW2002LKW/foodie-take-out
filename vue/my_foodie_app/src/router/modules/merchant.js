const merchantRoutes = [
  {
    path: '/',
    redirect: '/merchant/list',
  },
  {
    path: '/merchant/list',
    name: 'MerchantList',
    component: () => import('@/pages/merchant/pages/MerchantList.vue'),
    meta: { requiresAuth: true, showTabbar: true, title: '商家列表' },
  },
  {
    path: '/merchant/:id',
    name: 'MerchantDetail',
    component: () => import('@/pages/merchant/pages/MerchantDetail.vue'),
    meta: { requiresAuth: true, title: '店铺详情' },
  },
]

export default merchantRoutes
