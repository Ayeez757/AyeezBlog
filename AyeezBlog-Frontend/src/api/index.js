// src/api/index.js
import axios from 'axios';

// 后端地址：
// - 生产/手机访问：默认同域（由 nginx/网关反代到后端），避免写死 localhost 导致移动端请求失败
// - 开发环境：可用 VITE_API_BASE_URL 覆盖，或使用 vite proxy（见 vite.config.js）
const BASE_URL = (import.meta && import.meta.env && import.meta.env.VITE_API_BASE_URL) ? import.meta.env.VITE_API_BASE_URL : '/';


// 创建 axios 实例
const apiClient = axios.create({
  baseURL: BASE_URL,
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json',
  },
});

// 封装通用请求方法
export const request = async (method, url, data = null) => {
  try {
    const response = await apiClient({
      method,
      url,
      data,
    });
    return response.data;
  } catch (error) {
    console.error('API 请求失败:', error);
    throw error;
  }
};

// 封装具体的 API 接口
export const fetchLogs = () => request('GET', '/logs');
export const createLog = (logData) => request('POST', '/logs', logData);
export const updateLog = (id, logData) => request('PUT', `/logs/${id}`, logData);
export const deleteLog = (id) => request('DELETE', `/logs/${id}`);

// 获取文章列表（支持分页）
export const fetchPosts = (
  page = 1,
  pageSize = 10,
  orderBy = 'update_time',
  orderType = 'desc',
) => {
  const params = new URLSearchParams({
    page: String(page),
    pageSize: String(pageSize),
    orderBy,
    orderType,
  });
  return request('GET', `/post/list?${params.toString()}`);
};

// 获取单篇文章的 API（调用后端接口）
export const fetchPostById = (id) => {
  return request('GET', `/post/get?id=${id}`); // 调用后端接口
};

// 获取站点统计（PV/UV）
export const fetchSiteStats = () => request('GET', '/post/stats');

// 获取友链分组列表
// 线上部分网关仅放行 /post/**，因此优先走兼容路径并回退旧路径
export const fetchLinks = async () => {
  const isValidLinksResponse = (res) =>
    !!res && typeof res === 'object' && res.code === 200 && Array.isArray(res.data);

  try {
    const res = await request('GET', '/post/links/list');
    if (isValidLinksResponse(res)) {
      return res;
    }
  } catch (error) {
    // 忽略并回退到旧路径
  }

  const fallbackRes = await request('GET', '/links/list');
  if (isValidLinksResponse(fallbackRes)) {
    return fallbackRes;
  }

  return fallbackRes;
};

// 上报一次访问
export const trackSiteVisit = (visitorKey, path = '/') => {
  const params = new URLSearchParams({ path });
  return apiClient.post(`/post/stats/track?${params.toString()}`, null, {
    headers: {
      'X-Visitor-Key': visitorKey,
    },
  }).then((res) => res.data);
};