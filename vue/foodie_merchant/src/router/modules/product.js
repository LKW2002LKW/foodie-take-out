import MainLayout from '@/layouts/MainLayout.vue'

const productRoutes = [
  {
    path: '/product',
    component: MainLayout,
    children: [
      {
        path: 'list',
        name: 'ProductList',
        component: () => import('@/pages/product/pages/ProductList.vue'),
        meta: { title: '商品列表' },
      },
      {
        path: 'category',
        name: 'ProductCategory',
        component: () => import('@/pages/product/pages/Category.vue'),
        meta: { title: '商品分类' },
      },
      {
        path: 'setmeal',
        name: 'SetmealList',
        component: () => import('@/pages/product/pages/SetmealList.vue'),
        meta: { title: '套餐管理' },
      },
      {
        path: 'inventory',
        name: 'ProductInventory',
        component: () => import('@/pages/product/pages/Inventory.vue'),
        meta: { title: '商品库存' },
      },
      {
        path: 'audit',
        name: 'ProductAudit',
        component: () => import('@/pages/product/pages/Audit.vue'),
        meta: { title: '商品审核' },
      },
    ],
  },
]

export default productRoutes
