import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())

  return {
    plugins: [vue()],
    server: {
      host: true,
      port: 5341,
      strictPort: true,
      proxy: {
        // 高德地图安全代理：在服务器端自动注入 Key，浏览器 Network 面板不可见
        '/amap-api': {
          target: 'https://restapi.amap.com',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/amap-api/, ''),
          configure: (proxy, options) => {
            proxy.on('proxyReq', (proxyReq, req, res) => {
              const url = new URL(proxyReq.path, 'https://restapi.amap.com')
              // 将 Key 注入到代理请求中
              url.searchParams.set('key', env.VITE_AMAP_KEY)
              proxyReq.path = url.pathname + url.search
            })
          }
        },
        '/user': {
          target: 'http://0.0.0.0:8083',
          changeOrigin: true
        }
      }
    }
  }
})
