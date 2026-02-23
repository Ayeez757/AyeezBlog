// src/api/index.js
import axios from 'axios';

// 直接在这里配置后端接口地址
const BASE_URL = 'http://localhost:8080/'; // 手动修改这里的地址

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
export const fetchPosts = (page = 1, pageSize = 10) =>
  request('GET', `/post/list?page=${page}&pageSize=${pageSize}`);

// 获取单篇文章的 API（调用后端接口）
export const fetchPostById = (id) => {
  return request('GET', `/post/get?id=${id}`); // 调用后端接口
};