import MainLayout from '@/layouts/MainLayout.vue'

// 评价监管模块路由，服务评价列表与审核相关场景。
const reviewRoutes = [
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: 'review',
        name: 'Review',
        component: () => import('@/pages/review/pages/ReviewList.vue'),
        meta: { title: '评价管理', icon: 'ChatDotRound' },
      },
    ],
  },
]

export default reviewRoutes
