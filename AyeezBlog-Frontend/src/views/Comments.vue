<template>
  <div class="comments">
    <h1>欢迎留言</h1>
    <p>开发中......先建一个毛胚房，旧站留言页请看：<a href="https://blog.ayeez.cn/comments" target="_blank">旧站留言页</a></p>

    <div class="comment-card">
      <div id="tcomment"></div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Comments',
   mounted() {
    // 初始化 Twikoo
    window.twikoo.init({
      envId: 'https://twikoo.ayeez.cn',
      el: '#tcomment'       // 指定评论区容器的 ID
    });
  }
};
</script>

<style scoped>
.comments {
  position: absolute;
  top: 68px;
  padding: 20px;
  color: white;
  width: 100%; /* 占满屏幕宽度 */
  height: calc(100vh - 68px); /* 占满剩余屏幕高度 */
  display: flex; /* 启用 Flexbox */
  flex-direction: column; /* 子元素垂直排列 */
  align-items: center; /* 水平居中 */
  justify-content: flex-start; /* 内容从顶部开始 */
}

/* 黑色卡片样式 */
.comment-card {
  background-color: rgba(0, 0, 0, 0.8); /* 半透明白色背景 */
  border-radius: 10px; /* 圆角 */
  padding: 20px; /* 内边距 */
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3); /* 阴影效果 */
  width: 90%; /* 卡片宽度占父容器的 90% */
  max-width: 800px; /* 最大宽度限制 */
  margin-top: 20px; /* 与上方内容的距离 */
  border: 1px solid rgba(255, 255, 255, 0.5); /* 半透明白色边框 */
}

/* 调整 Twikoo 评论区容器样式 */
#tcomment {
  color: white; /* 文字颜色 */
}
</style>