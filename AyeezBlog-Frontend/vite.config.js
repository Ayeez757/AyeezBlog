import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 本地访问 Twikoo 云函数：浏览器直连会 CORS，走同源代理转发
const twikooProxy = {
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
      },
      ...twikooProxy
    }
  },
  preview: {
    proxy: {
      ...twikooProxy
    }
  }
})
