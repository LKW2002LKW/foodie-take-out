import MainLayout from '@/layouts/MainLayout.vue'

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
