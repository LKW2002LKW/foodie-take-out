import MainLayout from '@/layouts/MainLayout.vue'

// 订单模块路由，覆盖订单列表、统计与售后占位页。
const orderRoutes = [
  {
    path: '/order',
    component: MainLayout,
    children: [
      {
        path: 'list',
        name: 'OrderList',
        component: () => import('@/pages/order/pages/OrderList.vue'),
        meta: { title: '订单列表' },
      },
      {
        path: 'statistics',
        name: 'OrderStatistics',
        component: () => import('@/pages/order/pages/Statistics.vue'),
        meta: { title: '订单统计' },
      },
      {
        path: 'aftersales',
        name: 'OrderAfterSales',
        component: () => import('@/pages/order/pages/AfterSales.vue'),
        meta: { title: '售后服务' },
      },
    ],
  },
]

export default orderRoutes
