<template>
  <div class="privacy-page">
    <div class="privacy-inner">
      <section class="hero">
        <h1 class="hero-title">PRIVACY POLICY</h1>
        <p class="hero-subtitle">隐私政策 · 最后更新：2026 年 11 月</p>
      </section>

      <section class="section-card">
        <h2 class="section-title">一、适用范围</h2>
        <p class="muted">
          本政策适用于本站（blog.ayeez.cn，以下简称“本站”）。本站是阿叶 Ayeez 的个人博客，
          主要记录技术学习与生活随笔。访问或使用本站即表示你已阅读并理解本政策。
        </p>
      </section>

      <section class="section-card">
        <h2 class="section-title">二、本站收集的信息</h2>
        <ul class="info-list">
          <li>
            <strong>访问统计：</strong>本站会记录页面访问量（PV）与访客数（UV）。
            为区分独立访客，浏览器本地会生成一个随机标识存放在
            <code>localStorage</code> 中，该标识不含你的姓名、手机号等身份信息。
          </li>
          <li>
            <strong>评论信息：</strong>本站评论系统基于 Twikoo。发表评论时你主动填写的昵称、
            邮箱、网址与评论内容会被保存，用于展示评论与回复通知。邮箱不会公开展示。
          </li>
          <li>
            <strong>服务器日志：</strong>托管服务商可能记录访问 IP、时间、请求路径与
            User-Agent 等常规日志，用于安全防护与故障排查。
          </li>
        </ul>
      </section>

      <section class="section-card">
        <h2 class="section-title">三、Cookie 与本地存储</h2>
        <p class="muted">
          本站自身使用 <code>localStorage</code> 保存访客标识与少量界面偏好（如主题设置）。
          此外，本站接入了第三方服务（见下节），这些服务可能在你浏览器中写入 Cookie，
          用于统计与广告投放。你可以随时在浏览器设置中清除或禁用 Cookie 与本地存储，
          这不会影响浏览文章等基础功能。
        </p>
      </section>

      <section class="section-card">
        <h2 class="section-title">四、第三方服务</h2>
        <ul class="info-list">
          <li>
            <strong>Google Analytics 4 / Google Tag Manager：</strong>
            用于统计访问量、页面浏览与交互行为，帮助了解内容受欢迎程度。
            <a href="https://policies.google.com/privacy" target="_blank" rel="noopener noreferrer">
              Google 隐私权政策
            </a>
          </li>
          <li>
            <strong>Google AdSense：</strong>
            本站展示由 Google 提供的广告。Google 及其合作伙伴可能使用 Cookie
            基于你此前的访问记录投放广告（个性化广告）。
            你可以访问
            <a href="https://www.google.com/settings/ads" target="_blank" rel="noopener noreferrer">
              Google 广告设置
            </a>
            关闭个性化广告，或通过
            <a href="https://www.aboutads.info/choices/" target="_blank" rel="noopener noreferrer">
              aboutads.info
            </a>
            了解第三方广告商的 Cookie 退出方式。关闭个性化广告后你仍会看到广告，但相关性会降低。
          </li>
          <li>
            <strong>Twikoo 评论服务：</strong>用于承载本站评论功能，仅在评论区域加载。
          </li>
          <li>
            <strong>七牛云对象存储：</strong>用于托管本站图片、头像等静态资源。
          </li>
        </ul>
      </section>

      <section class="section-card">
        <h2 class="section-title">五、信息的使用与共享</h2>
        <p class="muted">
          本站收集的信息仅用于站点运营、内容改进与安全维护，不会出售、出租或交易你的个人信息。
          除上述第三方服务按其自身隐私政策处理数据外，仅在法律法规要求时才会披露。
        </p>
      </section>

      <section class="section-card">
        <h2 class="section-title">六、你的权利</h2>
        <p class="muted">
          你可以要求查询、更正或删除你留在本站的评论与相关信息。如需处理，
          请通过
          <router-link to="/about">关于页面</router-link>
          列出的联系方式与我联系。
        </p>
      </section>

      <section class="section-card">
        <h2 class="section-title">七、政策更新</h2>
        <p class="muted">
          本政策可能随功能调整而更新，更新后会在本页发布并修改顶部的“最后更新”时间。
          继续使用本站即视为接受更新后的政策。
        </p>
      </section>
    </div>
  </div>
</template>

<script setup>
// 纯静态页面，无逻辑
</script>

<style scoped>
.privacy-page {
  width: 100%;
  min-height: calc(100vh - 68px);
  padding: 20px;
  box-sizing: border-box;
  color: #fff;
  display: flex;
  justify-content: center;
  background: rgba(0, 0, 0, 0.7);
}

.privacy-inner {
  width: 100%;
  max-width: 900px;
}

.hero {
  padding: 22px 18px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: linear-gradient(135deg, rgba(108, 171, 106, 0.12), rgba(0, 0, 0, 0.76));
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.25);
}

.hero-title {
  margin: 0;
  font-family: 'Bebas Neue', Arial, sans-serif;
  font-size: 42px;
  line-height: 1.05;
  letter-spacing: 0.06em;
  background-image: linear-gradient(to right, #abe6a8, #00b828);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.hero-subtitle {
  margin: 10px 0 0;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.9);
}

.section-card {
  margin-top: 18px;
  padding: 20px 18px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(0, 0, 0, 0.68);
  box-shadow: 0 10px 26px rgba(0, 0, 0, 0.2);
}

.section-title {
  margin: 0 0 12px;
  font-family: 'Bebas Neue', Arial, sans-serif;
  font-size: 26px;
  letter-spacing: 0.09em;
  background-image: linear-gradient(to right, #abe6a8, #00b828);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.muted {
  margin: 0;
  line-height: 1.75;
  color: rgba(255, 255, 255, 0.82);
}

.muted a,
.section-card a {
  color: #7af58f;
  text-decoration: none;
}

.muted a:hover,
.section-card a:hover {
  text-decoration: underline;
}

.section-card code {
  padding: 1px 5px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.1);
  font-size: 0.9em;
}

.info-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 12px;
}

.info-list li {
  line-height: 1.75;
  color: rgba(255, 255, 255, 0.88);
  padding-left: 14px;
  position: relative;
}

.info-list li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0.7em;
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: rgba(0, 184, 40, 0.95);
  box-shadow: 0 0 12px rgba(0, 184, 40, 0.3);
}

@media (max-width: 768px) {
  .privacy-page {
    padding: 12px;
  }

  .hero-title {
    font-size: 32px;
  }

  .section-card {
    padding: 16px 14px;
  }
}
</style>
