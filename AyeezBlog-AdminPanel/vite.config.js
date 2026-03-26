import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  // 部署在 Nginx 子路径 /admin/ 时使用；本地 dev 不受影响
  base: '/admin/',
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
   server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',  // 后端地址
        changeOrigin: true,  // 允许跨域
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      '/twikoo-proxy': {
        target: 'https://twikoo.ayeez.cn',
        changeOrigin: true,
        secure: true,
        rewrite: (path) => {
          const p = path.replace(/^\/twikoo-proxy/, '')
          return p || '/'
        }
      }
    }
  }
})
