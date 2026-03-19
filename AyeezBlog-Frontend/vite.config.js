import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    proxy: {
      // 本地开发把前端请求转发到后端，避免 CORS
      '/post': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/logs': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
})
