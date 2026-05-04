import MainLayout from '@/layouts/MainLayout.vue'

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
