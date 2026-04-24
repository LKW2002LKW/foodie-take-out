import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    },
    server: {
      port: 5342,
      strictPort: true,
      proxy: {
        '/amap-api': {
          target: 'https://restapi.amap.com',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/amap-api/, ''),
          configure: (proxy) => {
            proxy.on('proxyReq', (proxyReq) => {
              const url = new URL(proxyReq.path, 'https://restapi.amap.com')
              url.searchParams.set('key', env.VITE_AMAP_KEY)
              proxyReq.path = url.pathname + url.search
            })
          }
        }
      }
    }
  }
})
