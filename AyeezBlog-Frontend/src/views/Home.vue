<template>
  <div class="home">

    <!-- 左上角搓的拐角线 -->
    <div class="left-top-line"></div>
    <div class="left-top-line2"></div>
    <div class="left-top-line3"></div>
    <div class="left-top-line4"></div>
    <div class="left-top-line5"></div>




    <!-- 左侧标语 -->
    <div class="welcome-banner">
      <div class="line1" ref="line1"></div>
      <div class="line2" ref="line2"></div>

    </div>

    <div class="content">
      <p class="fade-in-text">这里是阿叶 Ayeez Blog 的博客。</p>
      <p class="fade-in-text">很高兴与你相遇！</p>
      <p class="fade-in-text">这里会分享技术与生活~</p>
      <div class="left-bottom-line6"></div>
    </div>

    <div class="blank" style="height: 60px;"></div>




  </div>
  <!-- 引导线和箭头容器 -->
  <div class="arrow-container">
    <!-- 引导线 -->
    <div class="guide-line"></div>
    <!-- 向下箭头 -->
    <div class="arrow-down"></div>
  </div>

  <!-- 新增的横向卡片 -->
  <div class="card-container">
    <div class="card">
      <img id="home-card-avatar" src="https://blog.ayeez.cn/imgs/photo.jpg" alt="头像">
      <div class="card-content">
        <text style="font-size: 20px;font-weight: 1000;padding: 5px;">公告！</text>
        <text>这是新博客，仍然在开发中~</text>
        <text>旧站：https://blog.ayeez.cn （仍在使用中）</text>
        <text>qq闲聊交流群：421300955</text>
        <!-- 圆形图标链接 -->
        <div class="social-icons">
          <a href="https://github.com/ayeez757/" target="_blank" class="icon github">
            <i class="fab fa-github"></i>
          </a>
          <a href="https://space.bilibili.com/499974079" target="_blank" class="icon bilibili">
            <i class="fab fa-bilibili"></i> <!-- 自定义图标或用其他替代 -->
          </a>
          <a href="https://v.douyin.com/GGeliQdHOQ0/" target="_blank" class="icon douyin">
            <i class="fab fa-tiktok"></i> <!-- 抖音暂无官方图标，可用 TikTok 替代 -->
          </a>
          <a href="mailto:3406608593@qq.com" class="icon email">
            <i class="fas fa-envelope"></i>
          </a>
          <a href="https://qiniu.ayeez.cn/20260221221801914.png" target="_blank" class="icon qq">
            <i class="fab fa-qq"></i>
          </a>
        </div>
      </div>
    </div>
    
<!-- 文章卡片展示区域 -->
    <div class="posts-container">
      <div v-for="post in posts" :key="post.id" class="post-card">
        <img :src="post.cover || defaultCover" :alt="post.title" class="post-cover" />
        <div class="post-info">
          <h3 class="post-title">{{ post.title }}</h3>
          <p class="post-date">更新于 {{ formatDate(post.updateTime) }}</p>
          <p class="post-description">{{ truncateContent(post.content) }}</p>
        </div>
      </div>
    </div>

    <!-- 分页控件 -->
    <div class="pagination">
      <button @click="prevPage" :disabled="currentPage === 1">上一页</button>
      <span>{{ currentPage }} / {{ totalPages }}</span>
      <button @click="nextPage" :disabled="currentPage === totalPages">下一页</button>
    </div>

  </div>


</template>

<script>
import { fetchPosts } from '@/api'; // 引入 API 方法

export default {
  name: 'Home',
  data() {
    return {
      posts: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      defaultCover: 'https://blog.ayeez.cn/imgs/bg/bg.jpg'
    };
  },
  computed: {
    totalPages() {
      return Math.ceil(this.total / this.pageSize);
    }
  },
  async mounted() {
    await this.loadPosts(); // 加载文章数据
    this.animateText();
  },
  methods: {
    async loadPosts() {
      try {
        const response = await fetchPosts(this.currentPage, this.pageSize);
        this.posts = response.data.rows; // 文章列表
        this.total = response.data.total; // 总条数
      } catch (error) {
        console.error('加载文章失败:', error);
      }
    },
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--;
        this.loadPosts();
      }
    },
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++;
        this.loadPosts();
      }
    },
    formatDate(dateString) {
      const date = new Date(dateString);
      return date.toLocaleDateString('zh-CN');
    },
    truncateContent(content) {
      if (!content) return '暂无描述';
      return content.length > 100 ? content.substring(0, 100) + '...' : content;
    },
    animateText() {
      const line1Text = 'WELCOME\u00A0TO';
      const line2Text = 'AYEEZ BLOG！';

      if (!this.$refs.line1 || !this.$refs.line2) {
        console.error('DOM elements not found');
        return;
      }

      const line1HTML = this.wrapCharacters(line1Text);
      this.$refs.line1.innerHTML = line1HTML;

      this.$refs.line2.textContent = line2Text;

      this.triggerAnimation(this.$refs.line1);
      this.triggerAnimation(this.$refs.line2);
    },
    wrapCharacters(text) {
      return text
        .split('')
        .map(char => `<span class="char">${char}</span>`)
        .join('');
    },
    triggerAnimation(container) {
      const chars = container.querySelectorAll('.char');
      chars.forEach((char, index) => {
        char.style.animationDelay = `${index * 0.1}s`;
      });
    }
  }
};
</script>


<style>
/* 引入 Bebas Neue 字体 */
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&display=swap');

.home {
  position: absolute;
  top: 68px;
  padding: 20px;
  color: white;
  display: flex;
  flex-direction: column;
}

/* 左侧标语样式 */
.welcome-banner {
  font-family: 'Bebas Neue', sans-serif;
  text-align: left;
  color: #fff;
  padding: 100px;
  border-radius: 10px;
}

.line1,
.line2 {
  font-weight: normal;
  text-transform: uppercase;
  letter-spacing: 6px;
  line-height: 1.1;
}

.line1 {
  font-size: 6.5vw;
}

.line2 {
  font-size: 8vw;
}

/* 第一排文字样式（白色，逐字动画） */
.line1 .char {
  display: inline-block;
  opacity: 0;
  color: white;
  animation: fadeIn 0.5s forwards;
}

/* 第二排文字样式（整体渐变 + 弹性滑入） */
.line2 {
  background-image: linear-gradient(to right, #abe6a8, #00b828);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  display: inline-block;
  overflow: hidden;
  opacity: 0;
  transform: translateX(100%);
  /* 初始位置在右侧 */
  animation: slideInLeft 1.2s cubic-bezier(0.68, -0.55, 0.265, 1.55) forwards;
  /* 弹性滑入 */
}

/* 弹性滑入动画定义 */
@keyframes slideInLeft {
  to {
    opacity: 1;
    transform: translateX(0);
    /* 最终位置 */
  }
}

/* 逐字浮现动画 */
@keyframes fadeIn {
  to {
    opacity: 1;
  }
}

/* 内容区域样式 */
.content {
  flex: 1;
  font-size: 18px;
  line-height: 1.6;
  text-align: left;
  color: #b6b6b6;
  padding: 0 100px;
  border-radius: 10px;
}

/* 引导线和箭头的容器 */
.arrow-container {
  position: absolute;
  bottom: 60px;
  /* 距离底部的距离 */
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0px;
  /* 引导线与箭头之间的间距 */
  animation: floatAndBlink 2s infinite ease-in-out;

}

/* 引导线样式 */
.guide-line {
  width: 2px;
  height: 40px;
  background-color: rgba(31, 147, 15, 0.747);
  border-radius: 999px;
  box-shadow: #00b828 0px 0px 5px;
}

/* 向下箭头样式 */
.arrow-down {
  width: 30px;
  height: 30px;
  background-color: rgb(55, 255, 28);
  mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z'/%3E%3C/svg%3E") no-repeat center;
  -webkit-mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z'/%3E%3C/svg%3E") no-repeat center;
}

/* 浮动和闪烁动画 */
@keyframes floatAndBlink {

  0%,
  100% {
    transform: translateX(-50%) translateY(0);
    opacity: 1;
  }

  50% {
    transform: translateX(-50%) translateY(-10px);
    opacity: 0.5;
  }
}

/* 内容区域文字浮现动画 */
.fade-in-text {
  opacity: 0;
  animation: fadeInContent 1s forwards;
}

/* 分别设置每段文字的延迟时间 */
.content p:nth-child(1) {
  animation-delay: 0.5s;
}

.content p:nth-child(2) {
  animation-delay: 1s;
}

.content p:nth-child(3) {
  animation-delay: 1.5s;
}

/* 文字浮现动画定义 */
@keyframes fadeInContent {
  to {
    opacity: 1;
  }
}

.left-top-line {
  position: absolute;
  top: 80px;
  left: 70px;
  width: 130px;
  height: 10px;
  background-color: rgb(111, 155, 119);
}

.left-top-line2 {
  position: absolute;
  top: 90px;
  left: 70px;
  width: 10px;
  height: 10px;
  background-color: rgb(111, 155, 119);
}

.left-top-line3 {
  position: absolute;
  top: 110px;
  left: 70px;
  width: 10px;
  height: 5px;
  background-color: rgb(111, 155, 119);
}

.left-top-line4 {
  position: absolute;
  top: 80px;
  left: 210px;
  width: 20px;
  height: 10px;
  background-color: rgb(111, 155, 119);
}

.left-top-line5 {
  position: absolute;
  top: 80px;
  left: 250px;
  width: 5px;
  height: 10px;
  background-color: rgb(111, 155, 119);
}

.left-bottom-line6 {
  position: absolute;
  left: 70px;
  width: 350px;
  height: 7px;
  background-color: rgb(80, 105, 84);
}

/* 卡片容器样式 */
.card-container {
  flex-direction: column;
  align-items: center;
  display: flex;
  justify-content: center;
  /* 水平居中 */
  margin-top: 95vh;
  /*露出来一点在第一面*/
}

.card {
  display: flex;
  gap: 20px;
  position: relative;
  /* 为伪元素定位做准备 */
  width: 60%;
  max-width: 900px;
  background: linear-gradient(135deg,
      rgba(130, 183, 128, 0.3),
      rgba(33, 184, 66, 0.6));
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 10px;
  /* padding: 20px; */
  /* margin:20px; */
  text-align: center;
  color: white;
  box-shadow:
    inset 0 0 10px rgba(255, 255, 255, 0.2),
    0 4px 15px rgba(0, 0, 0, 0.3);
}

.card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg,
      rgba(255, 255, 255, 0.1),
      transparent);
  border-radius: 10px;
  pointer-events: none;
}

#home-card-avatar {
  width: 170px;
  height: 170px;
  border: 3px solid rgba(255, 255, 255, 0.5);
  margin:20px;
}

.card-content {
  display: flex;
  flex-direction: column;
  justify-content: start;
  text-align: left;
}

.social-icons {
  display: flex;
  gap: 15px;
  margin-top: 15px;
}

.icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #919d8bcb;
  color: white;
  text-decoration: none;
  transition: all 0.3s ease;
}

.icon:hover {
  transform: scale(1.1);
  background-color: #555;
}

/* 特定平台的颜色 */
.github:hover {
  background-color: #181818;
}

.bilibili:hover {
  background-color: #FB7299;
}

.douyin:hover {
  background-color: #181818;
}

.email:hover {
  background-color: #b2b206;
}

.qq:hover {
  background-color: #12b7f5;
}

.posts-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* 每行三个卡片 */
  gap: 20px; /* 卡片之间的间距 */
  padding: 20px 0px;
  width: 60%;
    max-width: 900px;
  box-sizing: border-box; /* 包含 padding 和 border 在内计算宽度 */
}

.post-card {
    background: linear-gradient(135deg,
      rgba(108, 171, 106, 0.3),
      rgba(42, 184, 73, 0.6));
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s ease;
}

.post-card:hover {
  transform: translateY(-5px);
}

.post-cover {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.post-info {
  padding: 15px;
}

.post-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
  color: white;
}

.post-date {
  font-size: 14px;
  color: #ccc;
  margin-bottom: 10px;
}

.post-description {
  font-size: 14px;
  color: #aaa;
  line-height: 1.5;
}

/* 分页控件样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 30px;
}

.pagination button {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 5px;
  color: white;
  cursor: pointer;
  /* transition: background 0.3s ease; */
}

.pagination button:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.4);
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>