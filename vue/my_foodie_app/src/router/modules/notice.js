// 公告模块路由，拆分公告列表与详情页，便于按需加载。
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
