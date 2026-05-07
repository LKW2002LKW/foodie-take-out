import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import pinia from '@/config/pinia'
import { appEnv } from '@/config/env'
import { registerVant } from '@/config/vant'
import 'vant/lib/index.css'
import './assets/inconfont/iconfont.js'
import './assets/inconfont/iconfont.css'
import '@/assets/styles/global.css'

// 高德安全密钥需要在 SDK 加载前挂到全局对象。
window._AMapSecurityConfig = {
  securityJsCode: appEnv.amapSecurityJsCode,
}

// 以 375 设计稿为基准设置根字体，兼顾移动端最小与最大宽度。
const setRootFontSize = () => {
  const viewportWidth = Math.max(320, Math.min(window.innerWidth, 540))
  document.documentElement.style.fontSize = `${viewportWidth / 37.5}px`
}

setRootFontSize()
window.addEventListener('resize', setRootFontSize)
window.addEventListener('pageshow', setRootFontSize)

const app = createApp(App)

// 统一注册状态、路由与 UI 组件，保证启动入口职责单一。
app.use(pinia)
app.use(router)
registerVant(app)

app.mount('#app')
