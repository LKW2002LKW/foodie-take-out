import { createRouter, createWebHistory } from 'vue-router'
import { createAfterEachGuard } from '@/router/guards/afterEach'
import { createBeforeEachGuard } from '@/router/guards/beforeEach'
import authRoutes from '@/router/modules/auth'
import configRoutes from '@/router/modules/config'
import dashboardRoutes from '@/router/modules/dashboard'
import financeRoutes from '@/router/modules/finance'
import merchantRoutes from '@/router/modules/merchant'
import noticeRoutes from '@/router/modules/notice'
import orderRoutes from '@/router/modules/order'
import reportRoutes from '@/router/modules/report'
import reviewRoutes from '@/router/modules/review'
import userRoutes from '@/router/modules/user'

const routes = [
  ...authRoutes,
  ...dashboardRoutes,
  ...merchantRoutes,
  ...orderRoutes,
  ...userRoutes,
  ...financeRoutes,
  ...reviewRoutes,
  ...reportRoutes,
  ...noticeRoutes,
  ...configRoutes,
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(createBeforeEachGuard())
router.afterEach(createAfterEachGuard())

export default router
