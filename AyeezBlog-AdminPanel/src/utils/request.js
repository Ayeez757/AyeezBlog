import axios from 'axios'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  // 默认放宽：文章保存、百炼生图等可能较慢；单项请求仍可在调用处覆盖
  timeout: 120000
})

// 请求拦截器 - 添加 token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    console.log('🔑 请求拦截器 - token:', token ? '存在' : '不存在')  // 🔍 调试日志
    console.log('🔑 请求拦截器 - URL:', config.url)
    
    if (token) {
      config.headers['token'] = `${token}`
    }
    return config
  },
  error => {
    console.error('❌ 请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一处理 401 和数据格式
request.interceptors.response.use(
  response => {
    console.log('📥 响应数据:', response.data)  // 🔍 调试日志
    const { code, message, data } = response.data
    if (code === 200) {
      return data
    } else {
      console.error('接口错误:', message)
      return Promise.reject(new Error(message))
    }
  },
  error => {
    console.error('❌ 响应错误:', error)
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export default request