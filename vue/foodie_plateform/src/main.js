import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import pinia from './config/pinia'
import router from './router'
import './style.css'
import App from './App.vue'

const app = createApp(App)

// 全量注册图标，供平台端表单、菜单和统计组件统一复用。
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 平台端入口只负责装配 UI 框架、状态容器与路由。
app.use(ElementPlus)
app.use(pinia)
app.use(router)
app.mount('#app')
