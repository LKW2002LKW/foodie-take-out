import MainLayout from '@/layouts/MainLayout.vue'

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
