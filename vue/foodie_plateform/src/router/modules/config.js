import MainLayout from '@/layouts/MainLayout.vue'

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
