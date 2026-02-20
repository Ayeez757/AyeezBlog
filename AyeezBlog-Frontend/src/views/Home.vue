<template>
  <div class="home">
    <!-- 左侧标语 -->
    <div class="welcome-banner">
      <div class="line1" ref="line1"></div>
      <div class="line2" ref="line2"></div>
    </div>

<div class="content">
  <p class="fade-in-text">这里是阿叶 Ayeez Blog 的博客。</p>
  <p class="fade-in-text">很高兴与你相遇！</p>
  <p class="fade-in-text">这里会分享技术与生活~</p>
</div>



  </div>
  <!-- 引导线和箭头容器 -->
  <div class="arrow-container">
    <!-- 引导线 -->
    <div class="guide-line"></div>
    <!-- 向下箭头 -->
    <div class="arrow-down"></div>
  </div>
</template>

<script>
export default {
  name: 'Home',
  mounted() {
    console.log('Component mounted'); // 调试日志
    this.animateText();
  },
  methods: {
    animateText() {
      const line1Text = 'WELCOME\u00A0TO';
      const line2Text = 'AYEEZ BLOG！';

      // 检查 DOM 元素是否存在
      if (!this.$refs.line1 || !this.$refs.line2) {
        console.error('DOM elements not found');
        return;
      }

      // 处理第一行文字（逐字动画）
      const line1HTML = this.wrapCharacters(line1Text);
      this.$refs.line1.innerHTML = line1HTML;

      // 处理第二行文字（整体渐变 + 弹性滑入动画）
      this.$refs.line2.textContent = line2Text;

      // 触发动画
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
  bottom: 30px;
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
</style>