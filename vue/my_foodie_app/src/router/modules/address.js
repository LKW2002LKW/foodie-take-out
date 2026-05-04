const addressRoutes = [
  {
    path: '/address/list',
    name: 'AddressList',
    component: () => import('@/pages/address/pages/AddressList.vue'),
    meta: { requiresAuth: true, title: '地址管理' },
  },
  {
    path: '/address/edit',
    name: 'AddressEdit',
    component: () => import('@/pages/address/pages/AddressEdit.vue'),
    meta: { requiresAuth: true, title: '编辑地址' },
  },
]

export default addressRoutes
