<template>
  <div class="sidebar">
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
    </el-menu>
  </div>
</template>

<script>
export default {
  methods: {
    handleSelect(index) {
      // 路由跳转
      this.$router.push(index);
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
</style>