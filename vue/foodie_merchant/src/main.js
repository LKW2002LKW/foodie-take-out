import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { appEnv } from '@/config/env'
import pinia from '@/config/pinia'
import './style.css'
import App from './App.vue'
import router from './router'

window._AMapSecurityConfig = {
  securityJsCode: appEnv.amapSecurityJsCode,
}

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)

app.mount('#app')
