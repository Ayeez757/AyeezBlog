<template>
  <div class="sidebar">
    <!-- 使用 Element Plus 的菜单组件 -->
    <el-menu
      :default-active="$route.path"
      class="el-menu-vertical"
      @select="handleSelect"
    >
      <el-menu-item index="/">首页</el-menu-item>
      <el-menu-item index="/article">文章管理</el-menu-item>
      <el-menu-item index="/category">分类管理</el-menu-item>
      <el-menu-item index="/tag">标签管理</el-menu-item>
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
  width: 200px;
  height: 100vh;
  background-color: #f4f4f4;
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
}

.el-menu-vertical {
  border-right: none;
}
</style>