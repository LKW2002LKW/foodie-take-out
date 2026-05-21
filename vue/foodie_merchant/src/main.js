import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { appEnv } from '@/config/env'
import pinia from '@/config/pinia'
import './style.css'
import App from './App.vue'
import router from './router'

// 高德安全密钥需要在地图 SDK 初始化前写入全局配置。
window._AMapSecurityConfig = {
  securityJsCode: appEnv.amapSecurityJsCode,
}

const app = createApp(App)

// 全量注册图标，供商家端表单、菜单和统计页统一使用。
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 应用入口只负责装配全局状态、路由与 UI 框架。
app.use(pinia)
app.use(router)
app.use(ElementPlus)

app.mount('#app')
