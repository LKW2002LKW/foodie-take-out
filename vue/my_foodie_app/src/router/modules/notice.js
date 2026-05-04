const noticeRoutes = [
  {
    path: '/notice/list',
    name: 'NoticeList',
    component: () => import('@/pages/notice/pages/NoticeList.vue'),
    meta: { requiresAuth: true, title: '公告列表' },
  },
  {
    path: '/notice/:id',
    name: 'NoticeDetail',
    component: () => import('@/pages/notice/pages/NoticeDetail.vue'),
    meta: { requiresAuth: true, title: '公告详情' },
  },
]

export default noticeRoutes
