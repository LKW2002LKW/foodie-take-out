// 订单模块路由，覆盖确认下单、订单列表与订单详情。
const orderRoutes = [
  {
    path: '/order/create',
    name: 'CreateOrder',
    component: () => import('@/pages/order/pages/CreateOrder.vue'),
    meta: { requiresAuth: true, title: '确认订单' },
  },
  {
    path: '/order/list',
    name: 'OrderList',
    component: () => import('@/pages/order/pages/OrderList.vue'),
    meta: { requiresAuth: true, showTabbar: true, title: '订单列表' },
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: () => import('@/pages/order/pages/Order.vue'),
    meta: { requiresAuth: true, title: '订单详情' },
  },
]

export default orderRoutes
