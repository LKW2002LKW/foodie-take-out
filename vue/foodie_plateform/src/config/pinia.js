import { createPinia } from 'pinia'

// 集中导出 Pinia 实例，保证入口与测试场景使用同一状态容器。
const pinia = createPinia()

export default pinia
