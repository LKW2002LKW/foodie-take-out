import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'

const routes = [
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
    path: '/',
    component: MainLayout,
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue')
      },
      // Product
      {
        path: 'product/list',
        name: 'ProductList',
        component: () => import('../views/product/ProductList.vue')
      },
      {
        path: 'product/category',
        name: 'ProductCategory',
        component: () => import('../views/product/Category.vue')
      },
      {
        path: 'product/setmeal',
        name: 'SetmealList',
        component: () => import('../views/setmeal/SetmealList.vue')
      },
      {
        path: 'product/inventory',
        name: 'ProductInventory',
        component: () => import('../views/product/Inventory.vue')
      },
      {
        path: 'product/audit',
        name: 'ProductAudit',
        component: () => import('../views/product/Audit.vue')
      },
      // Order
      {
        path: 'order/list',
        name: 'OrderList',
        component: () => import('../views/order/OrderList.vue')
      },
      {
        path: 'order/statistics',
        name: 'OrderStatistics',
        component: () => import('../views/order/Statistics.vue')
      },
      {
        path: 'order/aftersales',
        name: 'OrderAfterSales',
        component: () => import('../views/order/AfterSales.vue')
      },
      // Finance
      {
        path: 'finance/settlement',
        name: 'FinanceSettlement',
        component: () => import('../views/finance/Settlement.vue')
      },
      {
        path: 'finance/income',
        name: 'FinanceIncome',
        component: () => import('../views/finance/IncomeRecord.vue')
      },
      {
        path: 'finance/recharge',
        name: 'FinanceRecharge',
        component: () => import('../views/finance/Recharge.vue')
      },
      {
        path: 'finance/config',
        name: 'FinanceConfig',
        component: () => import('../views/finance/Config.vue')
      },
      // Merchant
      {
        path: 'merchant/profile',
        name: 'MerchantProfile',
        component: () => import('../views/merchant/Profile.vue')
      },
      {
        path: 'merchant/staff',
        name: 'MerchantStaff',
        component: () => import('../views/merchant/Staff.vue')
      },
      // Review
      {
        path: 'review/customer',
        name: 'ReviewCustomer',
        component: () => import('../views/review/CustomerReview.vue')
      },
      {
        path: 'review/reply',
        name: 'ReviewReply',
        component: () => import('../views/review/ReviewReply.vue')
      },
      // Statistics
      {
        path: 'statistics/sales',
        name: 'StatisticsSales',
        component: () => import('../views/statistics/SalesReport.vue')
      },
      {
        path: 'statistics/user',
        name: 'StatisticsUser',
        component: () => import('../views/statistics/UserAnalysis.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
