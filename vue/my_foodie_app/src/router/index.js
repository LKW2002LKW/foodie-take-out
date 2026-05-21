import { createRouter } from 'vue-router'
import { routerConfig } from '@/config/router'
import authRoutes from './modules/auth'
import merchantRoutes from './modules/merchant'
import addressRoutes from './modules/address'
import cartRoutes from './modules/cart'
import orderRoutes from './modules/order'
import reviewRoutes from './modules/review'
import profileRoutes from './modules/profile'
import noticeRoutes from './modules/notice'
import { registerBeforeEachGuard } from './guards/beforeEach'
import { registerAfterEachGuard } from './guards/afterEach'

// 主路由只负责聚合各业务模块的路由声明。
const routes = [
  ...authRoutes,
  ...merchantRoutes,
  ...addressRoutes,
  ...cartRoutes,
  ...orderRoutes,
  ...reviewRoutes,
  ...profileRoutes,
  ...noticeRoutes,
]

// 路由实例统一从配置层读取 history，避免散落硬编码。
const router = createRouter({
  history: routerConfig.history,
  routes,
})

registerBeforeEachGuard(router)
registerAfterEachGuard(router)

export default router
