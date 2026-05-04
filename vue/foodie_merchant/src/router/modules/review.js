import MainLayout from '@/layouts/MainLayout.vue'

const reviewRoutes = [
  {
    path: '/review',
    component: MainLayout,
    children: [
      {
        path: 'customer',
        name: 'ReviewCustomer',
        component: () => import('@/pages/review/pages/CustomerReview.vue'),
        meta: { title: '顾客评价' },
      },
      {
        path: 'reply',
        name: 'ReviewReply',
        component: () => import('@/pages/review/pages/ReviewReply.vue'),
        meta: { title: '评价回复' },
      },
    ],
  },
]

export default reviewRoutes
