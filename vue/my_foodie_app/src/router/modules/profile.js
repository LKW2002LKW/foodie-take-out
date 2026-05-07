// 个人中心模块路由，收口资料、密码与绑定手机等页面。
const profileRoutes = [
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/pages/profile/pages/Profile.vue'),
    meta: { requiresAuth: true, showTabbar: true, title: '我的' },
  },
  {
    path: '/profile/info',
    name: 'ProfileEdit',
    component: () => import('@/pages/profile/pages/ProfileEdit.vue'),
    meta: { requiresAuth: true, title: '编辑资料' },
  },
  {
    path: '/profile/password',
    name: 'ChangePassword',
    component: () => import('@/pages/profile/pages/ChangePassword.vue'),
    meta: { requiresAuth: true, title: '修改密码' },
  },
  {
    path: '/profile/phone',
    name: 'BindPhone',
    component: () => import('@/pages/profile/pages/BindPhone.vue'),
    meta: { requiresAuth: true, title: '绑定手机' },
  },
]

export default profileRoutes
