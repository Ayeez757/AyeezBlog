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

// 根据标题与正文生成文章简介（Spring AI + DeepSeek）
export const generateArticleDescription = (data) => {
  return request({
    url: '/admin/ai/article-description',
    method: 'post',
    data,
    timeout: 120000
  })
}

// 即梦文生图生成封面并上传七牛，返回 coverUrl（耗时常较长）
export const generateArticleCover = (data) => {
  return request({
    url: '/admin/ai/article-cover',
    method: 'post',
    data,
    timeout: 300000
  })
}

// 删除文章接口
export const deletePost = (params) => {
  return request.delete('/admin/post/delete', { params })
}

// =====================
// 说说（朋友圈）
// =====================
export const getTalkList = (params) => {
  return request.get('/admin/talk/list', { params })
}

export const addTalk = (data) => {
  return request.post('/admin/talk/add', data)
}

export const getTalkDetail = (params) => {
  return request.get('/admin/talk/get', { params })
}

export const updateTalk = (data) => {
  return request.put('/admin/talk/update', data)
}

export const deleteTalk = (params) => {
  return request.delete('/admin/talk/delete', { params })
}

// 说说页侧边栏配置
export const getTalkSidebar = () => {
  return request.get('/admin/talk/sidebar/get')
}

export const updateTalkSidebar = (data) => {
  return request.put('/admin/talk/sidebar/update', data)
}

// 七牛上传：获取 uploadToken（管理端直传七牛）
export const getQiniuUploadToken = (params) => {
  return request.get('/admin/upload/qiniu/token', { params })
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

// 友链分类管理
export const getLinkClassList = (params) => {
  return request.get('/admin/links/class/list', { params })
}

export const addLinkClass = (data) => {
  return request.post('/admin/links/class/add', data)
}

export const updateLinkClass = (data) => {
  return request.put('/admin/links/class/update', data)
}

export const deleteLinkClass = (params) => {
  return request.delete('/admin/links/class/delete', { params })
}

// 友链管理
export const getLinkList = (params) => {
  return request.get('/admin/links/link/list', { params })
}

export const addLink = (data) => {
  return request.post('/admin/links/link/add', data)
}

export const updateLink = (data) => {
  return request.put('/admin/links/link/update', data)
}

export const deleteLink = (params) => {
  return request.delete('/admin/links/link/delete', { params })
}

// 日志管理
export const getLogList = (params) => {
  return request.get('/admin/logs/list', { params })
}

export const addLogVersion = (data) => {
  return request.post('/admin/logs/add', data)
}

export const updateLogVersion = (data) => {
  return request.put('/admin/logs/update', data)
}

export const deleteLogVersion = (params) => {
  return request.delete('/admin/logs/delete', { params })
}

export const setCurrentLogVersion = (data) => {
  return request.post('/admin/logs/set-current', data)
}

// 管理端控制台：站点流量（PV/UV）仪表盘
export const getAdminDashboardStats = (params) => {
  return request.get('/admin/stats/dashboard', { params })
}

// 关于页追番列表
export const getAboutAnimeList = () => {
  return request.get('/admin/about/anime/list')
}

export const addAboutAnime = (data) => {
  return request.post('/admin/about/anime/add', data)
}

export const updateAboutAnime = (data) => {
  return request.put('/admin/about/anime/update', data)
}

export const deleteAboutAnime = (params) => {
  return request.delete('/admin/about/anime/delete', { params })
}

export const reorderAboutAnime = (data) => {
  return request.put('/admin/about/anime/reorder', data)
}

// 相册管理
export const getAlbumList = () => {
  return request.get('/admin/album/list')
}

export const addAlbum = (data) => {
  return request.post('/admin/album/add', data)
}

export const updateAlbum = (data) => {
  return request.put('/admin/album/update', data)
}

export const deleteAlbum = (params) => {
  return request.delete('/admin/album/delete', { params })
}

export const setDefaultCoverAlbum = (params) => {
  return request.post('/admin/album/default-cover/set', null, { params })
}

export const getAlbumPhotoList = (params) => {
  return request.get('/admin/album/photo/list', { params })
}

export const addAlbumPhoto = (data) => {
  return request.post('/admin/album/photo/add', data)
}

export const updateAlbumPhoto = (data) => {
  return request.put('/admin/album/photo/update', data)
}

export const deleteAlbumPhoto = (params) => {
  return request.delete('/admin/album/photo/delete', { params })
}