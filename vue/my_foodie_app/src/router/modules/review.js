// 评价模块路由，区分发布评价与我的评价两个业务入口。
const reviewRoutes = [
  {
    path: '/review/create',
    name: 'ReviewCreate',
    component: () => import('@/pages/review/pages/ReviewCreate.vue'),
    meta: { requiresAuth: true, title: '发布评价' },
  },
  {
    path: '/review/my',
    name: 'MyReview',
    component: () => import('@/pages/review/pages/MyReview.vue'),
    meta: { requiresAuth: true, title: '我的评价' },
  },
]

export default reviewRoutes
