<template>
  <div class="not-found-page">
    <div class="not-found-card">
      <div class="not-found-code">404</div>
      <h1 class="not-found-title">页面未找到</h1>
      <p class="not-found-desc">你访问的页面可能已移动、删除，或链接地址有误。</p>
      <div class="not-found-actions">
        <router-link to="/" class="not-found-btn not-found-btn--primary">返回首页</router-link>
        <router-link to="/archive" class="not-found-btn">浏览文章归档</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
// 纯静态 404 兜底页，由路由 meta 标记 noindex 且不加载广告脚本
</script>

<style scoped>
.not-found-page {
  width: 100%;
  min-height: calc(100vh - 68px);
  padding: 24px 16px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.7);
  color: #ffffff;
}

.not-found-card {
  width: 100%;
  max-width: 520px;
  padding: 36px 28px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(0, 0, 0, 0.72);
  text-align: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.35);
}

.not-found-code {
  font-family: 'Bebas Neue', Arial, sans-serif;
  font-size: 72px;
  line-height: 1;
  letter-spacing: 0.08em;
  background-image: linear-gradient(to right, #abe6a8, #00b828);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.not-found-title {
  margin: 12px 0 8px;
  font-size: 24px;
  color: #ffffff;
}

.not-found-desc {
  margin: 0 0 24px;
  font-size: 14px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.78);
}

.not-found-actions {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px;
}

.not-found-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10px 18px;
  border-radius: 999px;
  border: 1px solid rgba(122, 245, 143, 0.45);
  color: #dfffe5;
  text-decoration: none;
  font-size: 14px;
  transition: transform 0.2s ease, background-color 0.2s ease;
}

.not-found-btn--primary {
  background: rgba(0, 184, 40, 0.35);
}

.not-found-btn:hover {
  transform: translateY(-1px);
  background: rgba(0, 184, 40, 0.52);
}
</style>
