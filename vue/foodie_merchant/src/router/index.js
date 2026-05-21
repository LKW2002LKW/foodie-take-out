import { createRouter } from 'vue-router'
import { routerConfig } from '@/config/router'
import { registerAfterEachGuard } from '@/router/guards/afterEach'
import { registerBeforeEachGuard } from '@/router/guards/beforeEach'
import authRoutes from '@/router/modules/auth'
import dashboardRoutes from '@/router/modules/dashboard'
import financeRoutes from '@/router/modules/finance'
import merchantRoutes from '@/router/modules/merchant'
import orderRoutes from '@/router/modules/order'
import productRoutes from '@/router/modules/product'
import reviewRoutes from '@/router/modules/review'
import statisticsRoutes from '@/router/modules/statistics'

// 主路由只负责聚合各业务模块的路由声明。
const routes = [
  ...authRoutes,
  ...dashboardRoutes,
  ...productRoutes,
  ...orderRoutes,
  ...financeRoutes,
  ...merchantRoutes,
  ...reviewRoutes,
  ...statisticsRoutes,
]

// 路由实例统一从配置层获取 history，避免出现多处硬编码。
const router = createRouter({
  history: routerConfig.history,
  routes,
})

registerBeforeEachGuard(router)
registerAfterEachGuard(router)

export default router
