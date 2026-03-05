import request from '@/utils/request'

// 获取文章列表接口
export const getPostList = (params) => {
  return request.get('/admin/post/list', { params })
}

// 添加文章接口
export const addPost = (data) => {
  return request.post('/post/add', data)
}

// 登录接口
export function loginApi(data) {
  return request.post('/admin/login', data)
}

// 登出接口
export function logoutApi() {
  return request.post('/admin/logout')
}