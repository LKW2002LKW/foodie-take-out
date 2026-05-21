import MainLayout from '@/layouts/MainLayout.vue'

// 公告模块路由，负责公告列表与后续公告管理页扩展。
const noticeRoutes = [
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: 'notice',
        name: 'Notice',
        component: () => import('@/pages/notice/pages/NoticeList.vue'),
        meta: { title: '公告管理', icon: 'Bell' },
      },
    ],
  },
]

export default noticeRoutes
