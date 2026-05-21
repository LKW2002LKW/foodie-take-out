import { createPinia } from 'pinia'

// 集中导出 Pinia 实例，避免在业务代码里重复创建。
const pinia = createPinia()

export default pinia
