import axios from 'axios';

// 创建 axios 实例
const request = axios.create({
  baseURL: 'http://111.228.53.6:8080', // 后端基础地址
  timeout: 5000 // 请求超时时间
});

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 可以在这里添加 token 等通用请求头
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { code, message, data } = response.data;
    if (code === 200) {
      return data; // 返回成功数据
    } else {
      // 处理业务错误
      console.error('接口错误:', message);
      return Promise.reject(new Error(message));
    }
  },
  (error) => {
    console.error('网络错误:', error);
    return Promise.reject(error);
  }
);

// 接口定义
export const getPostList = (params) => {
  return request.get('/post/list', { params });
};


// 添加文章接口
export const addPost = (data) => {
  return request.post('/post/add', data);
};

