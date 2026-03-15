<template>
  <div class="links">
    <div class="links-inner">
      <div class="links-header">
        <div class="links-tagline">WELCOME&nbsp;TO</div>
        <div class="links-title-gradient">AYEEZ&nbsp;LINKS</div>
      </div>
      <p class="links-subtitle">这里是阿叶 Ayeez 的友链小角落，欢迎互相串门。</p>

      <!-- 友链列表 -->
      <div class="links-grid">
        <div
          v-for="(site, index) in friends"
          :key="index"
          class="link-card"
        >
          <div class="link-card-header">
            <img
              class="link-avatar"
              :src="site.avatar"
              :alt="site.name"
            />
            <div class="link-meta">
              <h2 class="link-name">{{ site.name }}</h2>
              <p class="link-descr">{{ site.descr }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 友链说明 & 格式 -->
      <div class="links-rules">
        <h2 class="rules-title">想交换友链吗？</h2>
        <p>
          想交换友链的可以在下方
          <span class="highlight">友链评论区</span>
          底下留言哦！
        </p>
        <p class="rules-subtitle">请先确保以下几点：</p>
        <ul>
          <li>
            你已经将
            <span class="highlight">阿叶Ayeez 的博客</span>
            添加至贵站友链
          </li>
          <li>
            你的网站内容
            <span class="highlight">符合中国大陆法律法规</span>
          </li>
          <li>
            你的网站可以在
            <span class="highlight">1 分钟内加载完成首屏</span>
          </li>
          <li>
            在你的网站里面
            <span class="highlight">加上我的友链</span>
          </li>
        </ul>

        <h3 class="rules-subtitle-strong">我的友链信息：</h3>
        <pre class="links-code">
- name: 阿叶Ayeez的小站
  link: https://blog.Ayeez.cn
  avatar: https://blog.ayeez.cn/imgs/photo.jpg
  descr: 记录学习历程，记录美好生活
  siteshot:
        </pre>

        <h3 class="rules-subtitle-strong">你可以按这个格式留言：</h3>
        <pre class="links-code">
- name: //[网站标题]
  link: //[网站网址]
  avatar: //[头像链接]
  descr: //[一句话简介]
  siteshot:
        </pre>

        <p class="links-tip">
          你的友链会在我看到信息的下次更新加上哦~ 如果发现我遗漏了可以戳戳我~
        </p>
      </div>

      <!-- 友链评论区 -->
      <div class="comment-card">
        <h2 class="comment-title">友链评论区</h2>
        <div id="tlinkcomment"></div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Links',
  data() {
    return {
      friends: [
        {
          name: '阿叶Ayeez的小站',
          link: 'https://blog.Ayeez.cn',
          avatar: 'https://blog.ayeez.cn/imgs/photo.jpg',
          descr: '记录学习历程，记录美好生活'
        }
        // 这里会在你下次更新时加上新的友链
      ]
    };
  },
  mounted() {
    if (window.twikoo) {
      window.twikoo.init({
        envId: 'https://twikoo.ayeez.cn',
        el: '#tlinkcomment'
      });
    }
  }
};
</script>

<style scoped>
.links {
  padding: 20px;
  width: 100%;
  box-sizing: border-box;
  color: white;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
}

.links-inner {
  width: 100%;
  max-width: 1300px;
}

.links-header {
  font-family: 'Bebas Neue', Arial, sans-serif;
  margin-bottom: 8px;
}

.links-tagline {
  font-size: 20px;
  letter-spacing: 4px;
  color: #cccccc;
}

.links-title-gradient {
  font-size: 46px;
  letter-spacing: 6px;
  background-image: linear-gradient(to right, #abe6a8, #00b828);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.links-subtitle {
  margin-bottom: 24px;
  color: #cccccc;
}

.links-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}

.link-card {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 16px;
  border-radius: 10px;
  background: linear-gradient(135deg,
      rgba(108, 171, 106, 0.3),
      rgba(42, 184, 73, 0.6));
  border: 2px solid #ffffff00;
  box-shadow:
    inset 0 0 10px rgba(255, 255, 255, 0.15),
    0 4px 15px rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  overflow: hidden;
  transition: transform 0.3s ease, border 0.3s ease, box-shadow 0.3s ease;
}

.link-card::before {
  content: '';
  position: absolute;
  top: -100%;
  left: 0;
  width: 100%;
  height: 5px;
  background: linear-gradient(to bottom, transparent, #00b828, transparent);
  z-index: 1;
  opacity: 0;
}

.link-card:hover {
  transform: translateY(-5px);
  border: 2px solid #00b828;
  box-shadow: 0 6px 15px rgba(0, 184, 40, 0.4);
}

.link-card:hover::before {
  opacity: 1;
  animation: scanLine 1s ease-in-out;
}

.link-card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.link-avatar {
  width: 54px;
  height: 54px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.6);
}

.link-meta {
  overflow: hidden;
}

.link-name {
  font-size: 1.1rem;
  margin: 0 0 4px 0;
}

.link-descr {
  margin: 0;
  font-size: 0.9rem;
  color: #cccccc;
  white-space: normal;
  text-overflow: clip;
  overflow: visible;
}

.links-rules {
  margin-top: 24px;
  margin-bottom: 24px;
  padding: 16px 18px;
  background-color: rgba(0, 0, 0, 0.8);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  font-size: 0.95rem;
}

.rules-title {
  margin-top: 0;
  margin-bottom: 6px;
  font-size: 1.2rem;
  color: #ffffff;
}

.rules-subtitle {
  margin: 4px 0;
  color: #dddddd;
}

.rules-subtitle-strong {
  margin: 10px 0 4px;
  font-size: 1.05rem;
  color: #ffffff;
}

.links-rules ul {
  padding-left: 1.2em;
  margin: 8px 0 16px;
}

.links-rules li {
  margin: 4px 0;
}

.highlight {
  color: #7af58f;
  font-weight: 600;
}

@keyframes scanLine {
  0% {
    top: -10px;
  }

  100% {
    top: 100%;
  }
}

.links-code {
  background-color: #111;
  border-radius: 6px;
  padding: 10px 12px;
  font-family: 'Courier New', monospace;
  font-size: 0.85rem;
  white-space: pre;
  overflow-x: auto;
  border: 1px solid rgba(255, 255, 255, 0.3);
  margin-bottom: 12px;
}

.links-tip {
  margin-top: 4px;
  color: #cccccc;
}

.comment-card {
  background-color: rgba(0, 0, 0, 0.8);
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.5);
  margin-top: 24px;
}

.comment-title {
  margin-top: 0;
  margin-bottom: 10px;
}

#tlinkcomment {
  color: white;
}
</style>