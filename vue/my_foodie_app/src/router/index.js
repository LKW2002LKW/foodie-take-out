import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/modules/user'

const routes = [
  {
    path: '/',
    // name: 'Home',
    // component: () => import('../views/Home.vue'),
    redirect: '/merchant/list'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { requiresAuth: true, showTabbar: true }
  },
  {
    path: '/profile/info',
    name: 'ProfileEdit',
    component: () => import('../views/profile/ProfileEdit.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile/password',
    name: 'ChangePassword',
    component: () => import('../views/profile/ChangePassword.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile/phone',
    name: 'BindPhone',
    component: () => import('../views/profile/BindPhone.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/merchant/list',
    name: 'MerchantList',
    component: () => import('../views/MerchantList.vue'),
    meta: { requiresAuth: true, showTabbar: true }
  },
  {
    path: '/merchant/:id',
    name: 'MerchantDetail',
    component: () => import('../views/MerchantDetail.vue')
  },
  {
    path: '/address/list',
    name: 'AddressList',
    component: () => import('../views/AddressList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/address/edit',
    name: 'AddressEdit',
    component: () => import('../views/AddressEdit.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/notice/list',
    name: 'NoticeList',
    component: () => import('../views/notice/NoticeList.vue'),
    meta: { requiresAuth: true, showTabbar: false }
  },
  {
    path: '/notice/:id',
    name: 'NoticeDetail',
    component: () => import('../views/notice/NoticeDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/order/create',
    name: 'CreateOrder',
    component: () => import('../views/CreateOrder.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/order/list',
    name: 'OrderList',
    component: () => import('../views/OrderList.vue'),
    meta: { requiresAuth: true, showTabbar: true }
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: () => import('../views/Order.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router
