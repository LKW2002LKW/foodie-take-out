const cartRoutes = [
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/pages/cart/pages/Cart.vue'),
    meta: { requiresAuth: true, showTabbar: true, title: '购物车' },
  },
]

export default cartRoutes
