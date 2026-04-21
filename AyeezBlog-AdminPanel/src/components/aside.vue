<template>
  <div class="sidebar" :class="{ 'is-mobile': isMobile, 'is-visible': visible }">
    <div class="brand">AyeezBlog Admin</div>
    <el-menu
      :default-active="$route.path"
      class="el-menu-vertical"
      @select="handleSelect"
    >
      <el-menu-item index="/">首页</el-menu-item>
      <el-menu-item index="/article">文章管理</el-menu-item>
      <el-menu-item index="/category">分类管理</el-menu-item>
      <el-menu-item index="/tag">标签管理</el-menu-item>
      <el-menu-item index="/links">友链管理</el-menu-item>
      <el-menu-item index="/albums">相册管理</el-menu-item>
      <el-menu-item index="/about-anime">追番列表</el-menu-item>
      <el-menu-item index="/about-anime/cards">追番卡片视图</el-menu-item>
      <el-menu-item index="/logs">日志管理</el-menu-item>
      <el-menu-item index="__logout__">退出登录</el-menu-item>
    </el-menu>
  </div>
</template>

<script>
import { logoutApi } from '@/api/index';

export default {
  props: {
    isMobile: {
      type: Boolean,
      default: false
    },
    visible: {
      type: Boolean,
      default: true
    }
  },
  methods: {
    async handleSelect(index) {
      if (index === '__logout__') {
        try {
          await logoutApi();
        } catch (error) {
          // 即便接口失败也清理本地态，避免前端假登录
          console.warn('登出接口调用失败，已执行本地退出', error);
        } finally {
          localStorage.removeItem('token');
          this.$router.push('/login');
        }
        return;
      }
      // 路由跳转
      this.$router.push(index);
      if (this.isMobile) {
        this.$emit('close');
      }
    }
  }
};
</script>

<style>
.sidebar {
  width: 220px;
  height: 100vh;
  background-color: #ffffff;
  border-right: 1px solid #e5e7eb;
  position: sticky;
  top: 0;
}

.brand {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 18px;
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  border-bottom: 1px solid #eef2f7;
}

.el-menu-vertical {
  border-right: none;
  padding-top: 8px;
}

@media (max-width: 768px) {
  .sidebar.is-mobile {
    position: fixed;
    left: 0;
    z-index: 1000;
    transform: translateX(-100%);
    transition: transform 0.25s ease;
    box-shadow: 0 10px 30px rgba(15, 23, 42, 0.22);
  }

  .sidebar.is-mobile.is-visible {
    transform: translateX(0);
  }
}
</style>