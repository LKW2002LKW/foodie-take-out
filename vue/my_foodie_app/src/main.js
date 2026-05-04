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

window._AMapSecurityConfig = {
  securityJsCode: appEnv.amapSecurityJsCode,
}

const setRootFontSize = () => {
  const viewportWidth = Math.max(320, Math.min(window.innerWidth, 540))
  document.documentElement.style.fontSize = `${viewportWidth / 37.5}px`
}

setRootFontSize()
window.addEventListener('resize', setRootFontSize)
window.addEventListener('pageshow', setRootFontSize)

const app = createApp(App)

app.use(pinia)
app.use(router)
registerVant(app)

app.mount('#app')
