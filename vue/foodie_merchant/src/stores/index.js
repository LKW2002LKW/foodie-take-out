import { createPinia } from 'pinia'

// 集中导出 Pinia 实例，避免业务文件重复创建状态容器。
const pinia = createPinia()

export default pinia
