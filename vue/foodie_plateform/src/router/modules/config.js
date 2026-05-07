import MainLayout from '@/layouts/MainLayout.vue'

// 系统配置模块路由，集中挂载平台配置相关页面。
const configRoutes = [
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: 'config',
        name: 'Config',
        component: () => import('@/pages/config/pages/SystemConfig.vue'),
        meta: { title: '系统配置', icon: 'Setting' },
      },
    ],
  },
]

export default configRoutes
