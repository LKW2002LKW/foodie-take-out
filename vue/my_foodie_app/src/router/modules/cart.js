// 购物车模块路由，作为带底部导航的核心业务页面之一。
const cartRoutes = [
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/pages/cart/pages/Cart.vue'),
    meta: { requiresAuth: true, showTabbar: true, title: '购物车' },
  },
]

export default cartRoutes
