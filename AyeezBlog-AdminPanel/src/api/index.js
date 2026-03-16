import request from '@/utils/request'

// 获取文章列表接口
export const getPostList = (params) => {
  return request.get('/admin/post/list', { params })
}

// 添加文章接口
export const addPost = (data) => {
  return request.post('/admin/post/add', data)
}

// 获取文章详情（编辑回显）
export const getPostDetail = (params) => {
  return request.get('/admin/post/get', { params })
}

// 更新文章（编辑保存）
export const updatePost = (data) => {
  return request.put('/admin/post/update', data)
}

// 删除文章接口
export const deletePost = (params) => {
  return request.delete('/admin/post/delete', { params })
}

// 登录接口
export function loginApi(data) {
  return request.post('/admin/login', data)
}

// 登出接口
export function logoutApi() {
  return request.post('/admin/logout')
}