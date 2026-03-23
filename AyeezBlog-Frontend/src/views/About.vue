<template>
  <div class="about-page">
    <div class="about-inner">
      <!-- 顶部：头像 + 标题 -->
      <section class="hero">
        <div class="hero-top">
          <img :src="avatarUrl" alt="头像" class="hero-avatar" />
          <div class="hero-titles">
            <h1 class="hero-title" ref="animatedTitle"></h1>
            <h2 class="hero-subtitle" ref="animatedSubtitle"></h2>
          </div>
        </div>

        <div class="tags-container" aria-label="标签列表">
          <span
            v-for="t in tagsWithStyle"
            :key="t.text"
            class="about-tag"
            :style="{ background: t.bg }"
          >
            {{ t.text }}
          </span>
        </div>
      </section>

      <!-- 个人简介 -->
      <section class="section-card">
        <h2 class="section-title">个人简介</h2>
        <ul class="info-list">
          <li><strong>很高兴认识你</strong></li>
          <li>25级软件工程在读大学生（大一）</li>
          <li>热爱计算机，喜欢开发</li>
          <li>喜欢瞎折腾，拥有众多兴趣爱好</li>
          <li>
            例如：
            <span class="accent-text">
              画画，弹钢琴，做饭，乒乓球，中长跑，排球，我的世界（游戏），中式建筑，生电（我的世界），魔方，骑行，桌球，国际象棋，
              摄影，手工木作，股市赌狗，编织，喝茶，写博客等
            </span>
          </li>
          <li>
            <strong>技术栈:</strong>
            <span class="accent-text">
              Java、HTML、CSS、Git、Astro、Uniapp、MySQL、Springboot、Maven、Ajax、JDBC、mybatis、log4j、slf4j、nginx
            </span>
          </li>
          <li>
            <strong>涉及:</strong>
            <span class="accent-text">
              c、c++、python、javascript、vue、logback、MongoDB、Ajax
            </span>
          </li>
          <li><strong>IP:</strong> 广东</li>
        </ul>

        <!-- 完全不完全统计：直接放进个人简介卡片内部 -->
        <div class="stats-inline">
          <h3 class="section-title section-title--stats">完全不完全统计</h3>
          <!-- <p class="muted muted--stats">喜欢看数据的人……这里也随便放一点。</p> -->
          <div class="stats-images stats-images--stats">
            <img
              :src="wakatimeBadge"
              alt="WakaTime Badge"
              class="stats-img stats-img--stats stats-img--stats-first"
              loading="lazy"
              @error="hideImage"
            />
            <img :src="wakatimeCompact" alt="WakaTime Compact" class="stats-img stats-img--stats" loading="lazy" @error="hideImage" />
          </div>
        </div>
      </section>

      <!-- 社交账号 -->
      <section class="section-card">
        <h2 class="section-title">我的一些社交账号</h2>
        <div class="social-grid">
          <a href="https://ayeez.cn" target="_blank" rel="noopener" class="social-button">
            <i class="fa fa-blog"></i><span>个人主页</span>
          </a>
          <a href="https://github.com/Ayeez757" target="_blank" rel="noopener" class="social-button">
            <i class="fab fa-github"></i><span>GitHub</span>
          </a>
          <a href="https://space.bilibili.com/499974079" target="_blank" rel="noopener" class="social-button">
            <i class="fas fa-tv"></i><span>哔哩哔哩</span>
          </a>
          <a href="https://www.zhihu.com/people/71-12-85-62-28" target="_blank" rel="noopener" class="social-button">
            <i class="fab fa-zhihu"></i><span>知乎</span>
          </a>
          <a
            href="https://www.douyin.com/user/MS4wLjABAAAArHlMaFbHsJL__6xl2PdkHefaBuKlXbyhZZ8wPGwfb6s"
            target="_blank"
            rel="noopener"
            class="social-button"
          >
            <i class="fab fa-tiktok"></i><span>抖音</span>
          </a>
          <a href="https://www.xiaohongshu.com/user/profile/63c16665000000002702b0d1" target="_blank" rel="noopener" class="social-button">
            <i class="fas fa-book"></i><span>小红书</span>
          </a>
          <a href="https://blog.csdn.net/Aye_Jun?type=blog" target="_blank" rel="noopener" class="social-button">
            <i class="fas fa-code"></i><span>CSDN</span>
          </a>
          <a href="mailto:ayeez757@gmail.com" class="social-button">
            <i class="fas fa-envelope"></i><span>邮箱</span>
          </a>
        </div>
      </section>

      <!-- 技术栈 -->
      <section class="section-card">
        <h2 class="section-title">我的技术栈</h2>
        <div class="tech-marquee" aria-label="技术栈图标滚动">
          <div class="tech-marquee-row tech-marquee-row--left">
            <img
              v-for="item in techIcons"
              :key="item.src"
              :src="item.src"
              :alt="item.name"
              :title="item.name"
              class="tech-icon"
              loading="lazy"
              @error="techIconError"
            />
            <img
              v-for="item in techIcons"
              :key="'dup-' + item.src"
              :src="item.src"
              :alt="item.name"
              :title="item.name"
              class="tech-icon"
              loading="lazy"
              @error="techIconError"
            />
          </div>
          <div class="tech-marquee-row tech-marquee-row--right">
            <img
              v-for="item in techIcons"
              :key="item.src + '-r'"
              :src="item.src"
              :alt="item.name"
              :title="item.name"
              class="tech-icon"
              loading="lazy"
              @error="techIconError"
            />
            <img
              v-for="item in techIcons"
              :key="'dup-r-' + item.src"
              :src="item.src"
              :alt="item.name"
              :title="item.name"
              class="tech-icon"
              loading="lazy"
              @error="techIconError"
            />
          </div>
        </div>
      </section>

      <!-- 追番 -->
      <section class="section-card">
        <div class="anime-title-row">
          <h2 class="section-title">追番列表</h2>
          <button
            type="button"
            class="anime-collapse-btn"
            @click="toggleAnimeCollapse"
            :aria-expanded="!animeCollapsed"
          >
            {{ animeCollapsed ? '展开全部' : '折叠' }}
          </button>
        </div>
        <div class="anime-grid" ref="animeGrid" aria-label="追番卡片列表">
          <a
            v-for="(src, i) in animeDisplayedImages"
            :key="src"
            href="#"
            class="anime-card"
            @click.prevent
            :title="`番剧 ${i + 1}`"
          >
            <img :src="src" :alt="`番剧 ${i + 1}`" class="anime-card-img" loading="lazy" @error="hideImage" />
          </a>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
export default {
  name: 'About',
  data() {
    return {
      avatarUrl: 'https://qiniu.ayeez.cn/avatar.jpg',

      titleText: '我叫阿叶Ayeez',
      subtitleText: '很高兴认识你Hi~ o(*￣▽￣*)ブ',

      tags: [
        '#大一软件工程学生',
        '#热衷于开发',
        '#学习能力MAX',
        '#喜欢瞎折腾',
        '#喜欢体验各种事物',
        '#画画、钢琴、运动、技术控',
        '#Minecraft'
      ],
      tagsWithStyle: [],
      gradientPool: [
        'linear-gradient(135deg, #B05F6D, #D18A94)',
        'linear-gradient(135deg, #B86D6D, #D19A9A)',
        'linear-gradient(135deg, #A66363, #C48C8C)',
        'linear-gradient(135deg, #8A6E9C, #A88EB8)',
        'linear-gradient(135deg, #947AA3, #B29AC4)',
        'linear-gradient(135deg, #7D7094, #9B8EBA)',
        'linear-gradient(135deg, #6B8CAD, #8AA9C9)',
        'linear-gradient(135deg, #7193A6, #91B3C9)',
        'linear-gradient(135deg, #69859C, #87A3BD)',
        'linear-gradient(135deg, #7DA48C, #9DC2AA)',
        'linear-gradient(135deg, #86A486, #A4C2A4)',
        'linear-gradient(135deg, #8FA68A, #ADC4A7)',
        'linear-gradient(135deg, #C49B71, #D9B995)',
        'linear-gradient(135deg, #B8A27D, #D1C09D)',
        'linear-gradient(135deg, #BFA07A, #D9C2A0)',
        'linear-gradient(135deg, #B48A9D, #D1A8BA)',
        'linear-gradient(135deg, #A88C9C, #C6AAB9)',
        'linear-gradient(135deg, #B095A8, #CDB3C5)',
        'linear-gradient(135deg, #7DA7A7, #9DC2C2)',
        'linear-gradient(135deg, #859D9D, #A3BDBD)',
        'linear-gradient(135deg, #7D9C9C, #9BB9B9)',
        'linear-gradient(135deg, #8A9494, #A8B2B2)',
        'linear-gradient(135deg, #9C948A, #BAB2A8)',
        'linear-gradient(135deg, #8A9C94, #A8BAAE)',
        'linear-gradient(135deg, #948A9C, #B2A8BA)',
        'linear-gradient(135deg, #9C9C8A, #BABAA8)'
      ],

      // 技术栈图标：本地 assets/imgs/icon
      techIconFiles: [
        'java-original.svg',
        'python-original.svg',
        'html5-original.svg',
        'css3-original.svg',
        'javascript-original.svg',
        'cplusplus-original.svg',
        'vuejs-original.svg',
        'git-original.svg',
        'intellij-original.svg',
        'vscode-original.svg',
        'pycharm-original.svg',
        'astro-original.svg',
        'mysql-original.svg',
        'maven-original.svg',
        'spring-original.svg',
        'uniapp.png'
      ],

      animeBase: 'https://qiniu.ayeez.cn/',
      animeImages: [],
      animeCollapsed: true,
      animeRowsToShow: 2,
      animeVisibleCount: 0,
      animeCardMinWidth: 130,
      animeGridGap: 6,
    };
  },
  computed: {
    techIcons() {
      return (this.techIconFiles || [])
        .map((file) => ({
          src: this.getTechIconSrc(file),
          name: this.getTechIconName(file)
        }))
        .filter((it) => it.src);
    },
    techIconFallback() {
      // 兜底占位图：避免个别图标找不到时整排缺图
      const svg = '<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 64 64"><rect x="6" y="6" width="52" height="52" rx="14" fill="rgba(0,184,40,0.12)" stroke="rgba(0,184,40,0.28)" /><text x="50%" y="56%" dominant-baseline="middle" text-anchor="middle" font-family="Arial, sans-serif" font-size="28" fill="#7af58f">?</text></svg>';
      return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`;
    },
    wakatimeBadge() {
      return 'https://wakatime.com/badge/user/923b7fe9-128b-4a46-b1d9-d4494ac957da.svg';
    },
    wakatimeStats() {
      return 'https://github-readme-stats-fast.vercel.app/api/wakatime?username=Ayeez757';
    },
    wakatimeCompact() {
      return 'https://github-readme-stats-fast.vercel.app/api/wakatime?username=Ayeez757&cache_seconds=1800&layout=compact';
    },
    animeDisplayedImages() {
      if (!this.animeCollapsed) return this.animeImages;
      const count = this.animeVisibleCount || this.animeRowsToShow;
      return (this.animeImages || []).slice(0, count);
    }
  },
  mounted() {
    // 标签随机渐变：只在首次进入页面时生成，避免轮播/滚动引起重算闪烁
    this.tagsWithStyle = (this.tags || []).map((text) => ({
      text,
      bg: this.gradientPool[Math.floor(Math.random() * this.gradientPool.length)]
    }));

    this.initAnimatedTitles();
    this.initAnimeImages();
    // 图片列表初始化后，计算当前网格可容纳列数，用于折叠显示“两行”
    this.$nextTick(() => {
      this.updateAnimeVisibleCount();
    });
    window.addEventListener('resize', this.onAnimeResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.onAnimeResize);
  },
  methods: {
    getTechIconSrc(file) {
      // 统一用 Vite 的静态资源打包方式，避免运行时相对路径在路由下失效
      switch (file) {
        case 'java-original.svg':
          return new URL('../assets/imgs/icon/java-original.svg', import.meta.url).href;
        case 'python-original.svg':
          return new URL('../assets/imgs/icon/python-original.svg', import.meta.url).href;
        case 'html5-original.svg':
          return new URL('../assets/imgs/icon/html5-original.svg', import.meta.url).href;
        case 'css3-original.svg':
          return new URL('../assets/imgs/icon/css3-original.svg', import.meta.url).href;
        case 'javascript-original.svg':
          return new URL('../assets/imgs/icon/javascript-original.svg', import.meta.url).href;
        case 'cplusplus-original.svg':
          return new URL('../assets/imgs/icon/cplusplus-original.svg', import.meta.url).href;
        case 'vuejs-original.svg':
          return new URL('../assets/imgs/icon/vuejs-original.svg', import.meta.url).href;
        case 'git-original.svg':
          return new URL('../assets/imgs/icon/git-original.svg', import.meta.url).href;
        case 'intellij-original.svg':
          return new URL('../assets/imgs/icon/intellij-original.svg', import.meta.url).href;
        case 'vscode-original.svg':
          return new URL('../assets/imgs/icon/vscode-original.svg', import.meta.url).href;
        case 'pycharm-original.svg':
          return new URL('../assets/imgs/icon/pycharm-original.svg', import.meta.url).href;
        case 'astro-original.svg':
          return new URL('../assets/imgs/icon/astro-original.svg', import.meta.url).href;
        case 'mysql-original.svg':
          return new URL('../assets/imgs/icon/mysql-original.svg', import.meta.url).href;
        case 'maven-original.svg':
          return new URL('../assets/imgs/icon/maven-original.svg', import.meta.url).href;
        case 'spring-original.svg':
          return new URL('../assets/imgs/icon/spring-original.svg', import.meta.url).href;
        case 'uniapp.png':
          return new URL('../assets/imgs/icon/uniapp.png', import.meta.url).href;
        default:
          return '';
      }
    },
    getTechIconName(file) {
      // 用文件名映射到更“像人”的技术栈名称（用于 title/alt）
      switch (file) {
        case 'java-original.svg':
          return 'Java';
        case 'python-original.svg':
          return 'Python';
        case 'html5-original.svg':
          return 'HTML5';
        case 'css3-original.svg':
          return 'CSS3';
        case 'javascript-original.svg':
          return 'JavaScript';
        case 'cplusplus-original.svg':
          return 'C++';
        case 'vuejs-original.svg':
          return 'Vue';
        case 'git-original.svg':
          return 'Git';
        case 'intellij-original.svg':
          return 'IntelliJ IDEA';
        case 'vscode-original.svg':
          return 'VS Code';
        case 'pycharm-original.svg':
          return 'PyCharm';
        case 'astro-original.svg':
          return 'Astro';
        case 'mysql-original.svg':
          return 'MySQL';
        case 'maven-original.svg':
          return 'Maven';
        case 'spring-original.svg':
          return 'Spring';
        case 'uniapp.png':
          return 'UniApp';
        default:
          return file;
      }
    },
    hideImage(e) {
      const target = e && e.target ? e.target : null;
      if (target) target.style.display = 'none';
    },

    techIconError(e) {
      const target = e && e.target ? e.target : null;
      if (!target) return;
      const fallback = this.techIconFallback;
      if (target.src !== fallback) target.src = fallback;
      target.style.display = 'block';
    },

    initAnimatedTitles() {
      const titleEl = this.$refs.animatedTitle;
      const subtitleEl = this.$refs.animatedSubtitle;
      if (!titleEl || !subtitleEl) return;

      const buildChars = (el, text, className) => {
        el.innerHTML = '';
        const safe = String(text || '');
        for (let i = 0; i < safe.length; i += 1) {
          const span = document.createElement('span');
          span.className = className;
          span.textContent = safe[i] === ' ' ? '\u00A0' : safe[i];
          span.style.animationDelay = `${i * 0.08}s`;
          el.appendChild(span);
        }
      };

      buildChars(titleEl, this.titleText, 'title-char');
      buildChars(subtitleEl, this.subtitleText, 'subtitle-char');
    },
    
    initAnimeImages() {
      // 动漫追番图：统一走七牛资源（p1.jpg ~ p43.jpg）
      const files = [
        'p45.jpg',
        'p1.jpg',
        'p2.jpg',
        'p22.jpg',
        'p17.jpg',
        'p24.jpg',
        'p27.jpg',
        'p3.png',
        'p34.jpg',
        'p4.jpeg',
        'p5.jpg',
        'p37.jpg',
        'p18.jpg',
        'p8.jpg',
        'p9.png',
        'p10.jpg',
        'p19.jpg',
        'p11.jpeg',
        'p7.jpg',
        'p46.jpg',
        'p12.jpg',
        'p13.jpg',
        'p14.jpg',
        'p15.jpg',
        'p16.jpg',
        'p6.jpg',
        'p20.jpg',
        'p21.jpg',
        'p23.jpg',
        'p25.jpg',
        'p26.jpg',
        'p28.jpg',
        'p29.jpg',
        'p30.jpg',
        'p31.jpg',
        'p32.jpg',
        'p33.jpg',
        'p35.jpg',
        'p36.jpg',
        'p38.jpg',
        'p39.jpg',
        'p40.jpg',
        'p41.jpg',
        'p42.jpg',
        'p43.jpg',
        'p44.jpg',
      ];
      this.animeImages = files.map((f) => `${this.animeBase}${f}`);
      // 初始化完成后也更新一次，避免首次渲染折叠行数不准
      this.$nextTick(() => {
        this.updateAnimeVisibleCount();
      });
    },
    onAnimeResize() {
      // 轻量 debounce，避免频繁计算
      clearTimeout(this._animeResizeTimer);
      this._animeResizeTimer = setTimeout(() => {
        this.updateAnimeVisibleCount();
      }, 150);
    },
    updateAnimeVisibleCount() {
      if (!this.animeImages || this.animeImages.length === 0) return;

      const gridEl = this.$refs.animeGrid;
      let colCount = 0;

      if (gridEl && typeof window !== 'undefined') {
        const style = window.getComputedStyle(gridEl);
        const colsStr = style && style.gridTemplateColumns;

        // gridTemplateColumns 通常会被解析为实际的轨道宽度列表：例如 "140px 140px 140px"
        if (colsStr && colsStr.includes('px')) {
          const parts = colsStr.split(' ').filter(Boolean);
          colCount = parts.length;
        }

        // 兜底：按 minmax 估算列数
        if (!colCount) {
          const w = gridEl.clientWidth || 0;
          const min = this.animeCardMinWidth || 130;
          const gap = this.animeGridGap || 6;
          colCount = Math.max(1, Math.floor((w + gap) / (min + gap)));
        }
      }

      colCount = Math.max(1, colCount || 1);
      const nextCount = colCount * (this.animeRowsToShow || 2);
      this.animeVisibleCount = Math.min(nextCount, this.animeImages.length);
    },
    toggleAnimeCollapse() {
      this.animeCollapsed = !this.animeCollapsed;
      // 展开/折叠后，grid 的实际列数基本不变，但这里仍可保证状态一致
      this.$nextTick(() => {
        this.updateAnimeVisibleCount();
      });
    },
  }
};
</script>

<style scoped>
.about-page {
  width: 100%;
  min-height: calc(100vh - 68px);
  padding: 20px;
  box-sizing: border-box;
  color: #fff;
  display: flex;
  justify-content: center;
  background: rgba(0, 0, 0, 0.7);
}

.about-inner {
  width: 100%;
  max-width: 1100px;
}

.hero {
  padding: 22px 18px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: linear-gradient(135deg, rgba(108, 171, 106, 0.12), rgba(0, 0, 0, 0.76));
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.25);
}

.hero-top {
  display: flex;
  align-items: center;
  gap: 16px;
}

.hero-avatar {
  width: 110px;
  height: 110px;
  border-radius: 18px;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.55);
  box-shadow: 0 0 0 6px rgba(0, 184, 40, 0.12);
}

.hero-titles {
  flex: 1;
  min-width: 0;
}

.hero-title {
  margin: 0;
  font-family: 'Bebas Neue', Arial, sans-serif;
  font-size: 46px;
  line-height: 1.05;
  letter-spacing: 0.06em;
  background-image: linear-gradient(to right, #abe6a8, #00b828);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.hero-subtitle {
  margin: 10px 0 0;
  font-size: 16px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
}

.title-char,
.subtitle-char {
  display: inline-block;
  opacity: 0;
  animation: titleCharIn 0.65s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
}

.subtitle-char {
  animation-name: subtitleCharIn;
}

@keyframes titleCharIn {
  from {
    opacity: 0;
    transform: translateY(16px) scale(0.45);
    filter: blur(1px);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
    filter: blur(0);
  }
}

@keyframes subtitleCharIn {
  from {
    opacity: 0;
    transform: translateY(10px) scale(0.6);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.tags-container {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.about-tag {
  display: inline-flex;
  align-items: center;
  padding: 7px 12px;
  border-radius: 999px;
  font-size: 13px;
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: inset 0 0 12px rgba(255, 255, 255, 0.08);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.25);
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
  font-size: 34px;
  letter-spacing: 0.09em;
  background-image: linear-gradient(to right, #abe6a8, #00b828);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.muted {
  margin: 0 0 14px;
  color: rgba(255, 255, 255, 0.72);
}

.stats-card {
  margin-top: 14px;
  padding: 16px 14px;
}

.stats-inline {
  margin-top: 14px;
}

.section-title--stats {
  margin-bottom: 8px;
  font-size: 22px;
}

.muted--stats {
  margin-bottom: 10px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.stats-images.stats-images--stats {
  display: grid;
  grid-template-columns: max-content;
  row-gap: 8px;
  column-gap: 12px;
  align-items: start;
  justify-items: start;
  justify-content: flex-start;
  width: 100%;
}

.stats-img--stats {
  /* “很小”：缩到你主页信息卡片那种密度 */
  max-width: 320px;
  width: auto;
  height: auto;
  object-fit: contain;
  display: block;
}

.stats-img--stats-first {
  max-width: 180px;
}

.info-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 10px;
}

.info-list li {
  line-height: 1.65;
  color: rgba(255, 255, 255, 0.92);
  padding-left: 14px;
  position: relative;
}

.info-list li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0.6em;
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: rgba(0, 184, 40, 0.95);
  box-shadow: 0 0 12px rgba(0, 184, 40, 0.3);
}

.accent-text {
  color: rgba(122, 245, 143, 0.95);
  font-weight: 600;
}

.social-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.social-button {
  text-decoration: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 14px 12px;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(255, 255, 255, 0.04);
  box-shadow: 0 10px 22px rgba(0, 0, 0, 0.18);
  color: #fff;
  transition: transform 0.15s ease, box-shadow 0.15s ease, border 0.15s ease, background 0.15s ease;
}

.social-button i {
  font-size: 18px;
  color: rgba(122, 245, 143, 0.95);
}

.social-button span {
  font-weight: 700;
  letter-spacing: 0.02em;
}

.social-button:hover {
  transform: translateY(-2px);
  border: 1px solid rgba(0, 184, 40, 0.85);
  background: rgba(0, 184, 40, 0.08);
  box-shadow: 0 16px 34px rgba(0, 184, 40, 0.12);
}

.stats-images {
  display: flex;
  /* flex-direction: column; */
  
  gap: 14px;
}

.stats-img {
  /* width: 100%; */
  margin: 0 auto;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: rgba(255, 255, 255, 0.03);
}

.tech-marquee {
  --tech-marquee-bg: rgba(0, 0, 0, 0.25);
  --tech-marquee-bg: rgba(255, 255, 255, 0.07);
  --tech-marquee-border: rgba(255, 255, 255, 0.22);
  --tech-icon-bg: rgba(0, 184, 40, 0.22);
  --tech-icon-border: rgba(0, 184, 40, 0.5);
  --tech-icon-shadow: rgba(0, 184, 40, 0.24);

  border-radius: 10px;
  border: 1px solid var(--tech-marquee-border);
  background: var(--tech-marquee-bg);
  overflow: hidden;
  padding: 22px 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.tech-marquee-row {
  width: max-content;
  display: flex;
  gap: 22px;
  padding: 0 18px;
  align-items: center;
  animation: techMarquee 55s linear infinite;
}

@keyframes techMarquee {
  from {
    transform: translateX(0);
  }
  to {
    transform: translateX(-50%);
  }
}

.tech-marquee-row--right {
  animation: techMarqueeReverse 55s linear infinite;
}

@keyframes techMarqueeReverse {
  from {
    transform: translateX(-50%);
  }
  to {
    transform: translateX(0);
  }
}

.anime-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.anime-title-row .section-title {
  margin: 0;
}

.anime-collapse-btn {
  flex: 0 0 auto;
  background: rgba(0, 184, 40, 0.14);
  border: 1px solid rgba(0, 184, 40, 0.35);
  color: rgba(255, 255, 255, 0.95);
  padding: 8px 12px;
  border-radius: 12px;
  cursor: pointer;
  font-size: 13px;
  line-height: 1;
}

.anime-collapse-btn:hover {
  border-color: rgba(0, 184, 40, 0.6);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.18);
}

.anime-collapse-btn:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(0, 184, 40, 0.25);
}

.tech-icon {
  width: 106px;
  height: 106px;
  display: block;
  box-sizing: border-box;
  padding: 14px;
  border-radius: 16px;
  background: var(--tech-icon-bg);
  border: 1px solid var(--tech-icon-border);
  object-fit: contain;
  opacity: 0.95;
  filter: drop-shadow(0 8px 20px var(--tech-icon-shadow));
}

.anime-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
  gap: 6px;
  padding: 0;
}

.anime-card {
  position: relative;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(0, 0, 0, 0.35);
  overflow: hidden;
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.18);
  transition: transform 0.15s ease, border 0.15s ease, box-shadow 0.15s ease;
  text-decoration: none;
}

.anime-card::before {
  content: '';
  position: absolute;
  left: -20%;
  top: -30%;
  width: 140%;
  height: 55px;
  background: linear-gradient(to bottom, transparent, rgba(0, 184, 40, 0.55), transparent);
  transform: translateY(-60px);
  opacity: 0;
  transition: opacity 0.15s ease, transform 0.25s ease;
  pointer-events: none;
}

.anime-card:hover {
  transform: translateY(-3px);
  border-color: rgba(0, 184, 40, 0.85);
  box-shadow: 0 18px 40px rgba(0, 184, 40, 0.12);
}

.anime-card:hover::before {
  opacity: 1;
  transform: translateY(160px);
}

.anime-card-img {
  width: 100%;
  /* 强制展示框比例：29:32；用 cover 填满避免留白 */
  aspect-ratio: 125 / 176;
  height: auto;
  object-fit: cover;
  display: block;
  filter: grayscale(30%);
  transition: filter 0.2s ease, transform 0.2s ease;
  background: transparent;
}

.anime-card:hover .anime-card-img {
  filter: grayscale(0%);
  transform: scale(1.02);
}

@media (max-width: 980px) {
  .hero-title {
    font-size: 42px;
  }
}

@media (max-width: 520px) {
  .about-page {
    padding: 14px;
  }

  .hero-top {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .hero-avatar {
    width: 96px;
    height: 96px;
  }

  .hero-title {
    font-size: 34px;
  }

  .hero-subtitle {
    font-size: 15px;
  }

  .social-grid {
    grid-template-columns: 1fr 1fr;
  }

  .section-card {
    padding: 18px 14px;
  }

  .anime-title-row {
    margin-bottom: 10px;
  }
}
</style>