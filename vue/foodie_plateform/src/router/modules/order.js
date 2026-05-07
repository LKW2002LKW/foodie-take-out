import MainLayout from '@/layouts/MainLayout.vue'

// 订单模块路由，承接订单监控和履约监管页面。
const orderRoutes = [
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/pages/order/pages/OrderMonitor.vue'),
        meta: { title: '订单监控', icon: 'List' },
      },
    ],
  },
]

export default orderRoutes
