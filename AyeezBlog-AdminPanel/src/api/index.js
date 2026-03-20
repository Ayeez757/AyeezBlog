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

// 分类管理
export const getCategoryList = (params) => {
  return request.get('/admin/category/list', { params })
}

export const addCategory = (data) => {
  return request.post('/admin/category/add', data)
}

export const updateCategory = (data) => {
  return request.put('/admin/category/update', data)
}

export const deleteCategory = (params) => {
  return request.delete('/admin/category/delete', { params })
}

export const getCategoryPosts = (params) => {
  return request.get('/admin/category/posts', { params })
}

// 标签管理
export const getTagList = (params) => {
  return request.get('/admin/tag/list', { params })
}

export const addTag = (data) => {
  return request.post('/admin/tag/add', data)
}

export const updateTag = (data) => {
  return request.put('/admin/tag/update', data)
}

export const deleteTag = (params) => {
  return request.delete('/admin/tag/delete', { params })
}

export const getTagPosts = (params) => {
  return request.get('/admin/tag/posts', { params })
}