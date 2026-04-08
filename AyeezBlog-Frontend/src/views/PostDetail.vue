<template>
  <div class="post-container">
    <!-- 阅读模式：全屏、白底黑字、左右双滚动（文章/目录分离） -->
    <div
      v-if="isReadingMode"
      class="reader-overlay"
      role="dialog"
      aria-modal="true"
    >
      <button class="reader-exit" type="button" @click="exitReadingMode">退出阅读</button>
      <button
        v-if="headings.length"
        class="reader-toc-mobile-toggle"
        type="button"
        @click="toggleMobileReaderToc"
      >
        目录
      </button>

      <div class="reader-shell">
        <!-- 目录（左侧） -->
        <aside
          v-if="headings.length"
          class="reader-toc"
          :class="{ 'reader-toc--mobile-open': isMobileReaderTocOpen }"
          ref="readerToc"
        >
          <div class="reader-toc-title">目录</div>
          <div class="reader-toc-content" ref="readerTocContent">
            <ul class="reader-toc-list">
              <li
                v-for="h in headings"
                :key="h.anchor"
                class="reader-toc-item"
                :class="{ 'reader-toc-item--active': h.anchor === activeReaderAnchor }"
                :style="{ marginLeft: (Math.min(h.level, 6) - 1) * 14 + 'px' }"
                v-show="!isHiddenByParent(h)"
                @click="scrollToAnchorReader(h.anchor)"
              >
                <span
                  v-if="hasChildren(h)"
                  class="reader-toc-toggle"
                  @click.stop="toggleCollapse(h.anchor)"
                >
                  {{ isCollapsed(h.anchor) ? '▶' : '▼' }}
                </span>
                <span v-else class="reader-toc-toggle reader-toc-toggle-placeholder"></span>

                <span class="reader-toc-link">{{ h.title }}</span>
              </li>
            </ul>
          </div>
        </aside>

        <!-- 文章内容（右侧） -->
        <div class="reader-main">
          <div class="reader-content-scroller" ref="readerContentScroller">
            <article class="reader-article" ref="readerArticle">
              <h1 class="reader-title">{{ frontMatter.title || post.title }}</h1>

              <div class="reader-meta">
                <p v-if="frontMatter.tags">标签：{{ frontMatter.tags.join(', ') }}</p>
                <p v-if="frontMatter.category">分类：{{ frontMatter.category.join(', ') }}</p>
                <p>更新于 {{ formatDate(frontMatter.updated || post.updateTime || '') }}</p>
              </div>

              <div v-if="frontMatter.description" class="reader-description">
                <p>{{ frontMatter.description }}</p>
              </div>

              <hr class="reader-hr" />

              <div class="reader-post-content" v-html="renderedMarkdown"></div>
            </article>
          </div>
        </div>
      </div>
    </div>

    <!-- 文章内容（左侧） -->
    <main class="post-main">
      <div class="post-detail">
        <!-- 标题 -->
        <h1>{{ frontMatter.title || post.title }}</h1>

        <!-- 元信息 -->
        <div class="post-meta">
          <p v-if="frontMatter.tags">标签：{{ frontMatter.tags.join(', ') }}</p>
          <p v-if="frontMatter.category">分类：{{ frontMatter.category.join(', ') }}</p>
          <p>更新于 {{ formatDate(frontMatter.updated || post.updateTime || '') }}</p>
        </div>

        <!-- 描述 -->
        <div v-if="frontMatter.description" class="post-description">
          <p>{{ frontMatter.description }}</p>
        </div>

        <hr />
        <hr />

        <!-- 正文 -->
        <div class="post-content" v-html="renderedMarkdown"></div>

        <!-- 文章评论区域（Twikoo） -->
        <section id="comments" class="post-comments">
          <h2 class="post-comments-title">评论</h2>
          <div class="post-comment-card">
            <div id="tcomment-post" ref="twikooPost"></div>
          </div>
        </section>
      </div>
    </main>

    <div class="toc-sidebar-container">
    <!-- 目录（右侧，可折叠） -->
    <aside
      class="toc-sidebar"
      :class="{ 'toc-sidebar--mobile-open': isMobileTocOpen }"
      ref="tocSidebar"
      v-if="headings.length"
      tabindex="0"
      @blur="onMobileTocBlur"
    >
      <div class="toc-title">目录</div>
      <ul class="toc-list">
        <li
          v-for="h in headings"
          :key="h.anchor"
          class="toc-item"
          :style="{ marginLeft: (Math.min(h.level, 6) - 1) * 14 + 'px' }"
          v-show="!isHiddenByParent(h)"
          @click="scrollToAnchor(h.anchor)"
        >
          <span
            v-if="hasChildren(h)"
            class="toc-toggle"
            @click.stop="toggleCollapse(h.anchor)"
          >
            {{ isCollapsed(h.anchor) ? '▶' : '▼' }}
          </span>
          <span
            v-else
            class="toc-toggle toc-toggle-placeholder"
          ></span>
          <span class="toc-link">
            {{ h.title }}
          </span>
        </li>
      </ul>
    </aside>
  </div>

    <!-- 桌面端右下角悬浮球 -->
    <div class="float-buttons" v-if="!isReadingMode">
      <button class="float-btn" @click="scrollToComments">
        评
      </button>
      <button class="float-btn" @click="scrollToTop">
        顶
      </button>
      <button class="float-btn float-btn--toc" @click="toggleMobileToc">
        目
      </button>
      <button class="float-btn float-btn--reader" type="button" @click="toggleReadingMode">
        阅
      </button>
    </div>
  </div>
</template>

<script>
import { fetchPostById } from '@/api';
import { loadTwikoo, getTwikooEnvId } from '@/utils/twikoo';
import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js';
import fm from 'front-matter';
import 'highlight.js/styles/github-dark.css';
import Lenis from 'lenis';
import { initSmoothScroll, destroySmoothScroll } from '@/plugins/smoothScroll';

export default {
  name: 'PostDetail',
  props: ['id'],
  data() {
    return {
      post: {},
      frontMatter: {},
      headings: [],     // { level, title, anchor }
      collapsedMap: {}, // { [anchor]: boolean }
      isMobileTocOpen: false,
      isReadingMode: false,
      isMobileReaderTocOpen: false,
      activeReaderAnchor: null,
      _onResizeBound: null,
      __prevBodyOverflow: undefined,
      __prevHtmlOverflow: undefined,
      __readerLenisRafId: null,
      __readerLenisContent: null,
      __readerLenisToc: null
      ,
      __destroyedSmoothScrollByReader: false
    };
  },
  computed: {
    renderedMarkdown() {
      const md = new MarkdownIt({
        highlight: function (str, lang) {
          if (lang && hljs.getLanguage(lang)) {
            try {
              return hljs.highlight(str, { language: lang }).value;
            } catch (_) {}
          }
          return ''; // 不高亮时走默认转义
        }
      });

      // 自定义代码块渲染：增加头部信息 & 复制按钮
      const defaultFence =
        md.renderer.rules.fence ||
        function (tokens, idx, options, env, self) {
          return self.renderToken(tokens, idx, options);
        };

      md.renderer.rules.fence = (tokens, idx, options, env, self) => {
        const token = tokens[idx];
        const info = token.info ? token.info.trim() : '';
        const lang = info.split(/\s+/g)[0] || '';
        const rawCode = token.content;

        let highlighted = '';
        if (options.highlight) {
          highlighted = options.highlight(rawCode, lang) || '';
        }

        const finalCode =
          highlighted || md.utils.escapeHtml(rawCode || '');
        const langLabel = lang || 'Text';

        return `
<div class="code-block">
  <div class="code-block__header">
    <span class="code-block__lang">${langLabel}</span>
    <button class="code-block__copy" type="button">复制</button>
  </div>
  <pre class="code-block__body"><code class="hljs language-${langLabel.toLowerCase()}">${finalCode}</code></pre>
</div>`;
      };

      // 自定义图片渲染规则
      md.renderer.rules.image = function (tokens, idx) {
        const token = tokens[idx];
        const src = token.attrGet('src');
        const alt = token.content;
        return `<img src="${src}" alt="${alt}" style="max-width: 100%; height: auto; display: block; margin: 10px 0;" />`;
      };

      // 自定义标题渲染规则（添加锚点）
      md.renderer.rules.heading_open = (tokens, idx) => {
        const token = tokens[idx];
        const level = token.tag.slice(1);
        const nextToken = tokens[idx + 1];
        const title = nextToken.content;
        const anchor = title
          .toLowerCase()
          .replace(/\s+/g, '-')
          .replace(/[^\w\-一-龥]/g, '');
        return `<h${level} id="${anchor}">`;
      };

      // 解析 Front-matter 并分离正文
      const { body, attributes } = fm(this.post.content || '');
      this.frontMatter = attributes || {};

      // 提取标题列表
      this.extractHeadings(body);

      // 渲染正文
      return md.render(body);
    }
  },
  watch: {
    renderedMarkdown() {
      this.$nextTick(() => {
        this.enhanceCodeBlocks();
        this.updateTocPosition();
      });
    }
  },
  methods: {
    updateTocPosition() {
      // 阅读模式时不需要计算右侧目录位置
      if (this.isReadingMode) return;
      // 仅桌面端需要精确贴右；移动端 TOC 走抽屉 fixed right/bottom
      if (typeof window === 'undefined') return;
      const tocEl = this.$refs.tocSidebar;
      if (!tocEl) return;

      // 移动端：清掉桌面端写入的内联定位，让 media query 的 right/bottom 生效
      if (window.innerWidth <= 768) {
        tocEl.style.left = '';
        tocEl.style.right = '';
        return;
      }

      const postCard = this.$el.querySelector('.post-detail');
      if (!postCard) return;

      const gap = 20;
      const viewportPadding = 16;

      const postRect = postCard.getBoundingClientRect();
      const tocRect = tocEl.getBoundingClientRect();
      const tocWidth = tocRect.width || 260;

      // 理想位置：紧贴文章卡片右侧
      let left = postRect.right + gap;

      // 夹在视口内，避免越界
      const maxLeft = window.innerWidth - viewportPadding - tocWidth;
      if (left > maxLeft) left = maxLeft;
      if (left < viewportPadding) left = viewportPadding;

      tocEl.style.left = `${left}px`;
      tocEl.style.right = 'auto';
    },

    // 平滑滚动到锚点（整行点击）
    scrollToAnchor(anchor) {
      const el = document.getElementById(anchor);
      if (!el) return;
      const offset = 90; // 和 scroll-margin-top 对齐，可微调
      const rect = el.getBoundingClientRect();
      const top = window.pageYOffset + rect.top - offset;

      window.scrollTo({
        top,
        behavior: 'smooth'
      });

      // 手机端点击目录项后自动收起目录
      if (window.innerWidth <= 768) {
        this.isMobileTocOpen = false;
      }
    },

    // 提取标题：按 # 的真实数量作为层级（H1~H6），排除代码块中的 #
    extractHeadings(markdown) {
      const headings = [];
      const lines = markdown.split('\n');
      let inCodeBlock = false;

      lines.forEach(line => {
        const trimmed = line.trim();

        // 代码块内部不解析标题
        if (trimmed.startsWith('```')) {
          inCodeBlock = !inCodeBlock;
          return;
        }
        if (inCodeBlock) return;

        // 匹配行首 # 标题，1~6 个 #
        const m = trimmed.match(/^(#{1,6})\s+(.+)$/);
        if (!m) return;

        const level = m[1].length; // 1 ~ 6
        const title = m[2].trim();
        const anchor = title
          .toLowerCase()
          .replace(/\s+/g, '-')          // 空格 -> -
          .replace(/[^\w\-一-龥]/g, '');  // 去掉大部分标点

        headings.push({ level, title, anchor });
      });

      this.headings = headings;
    },

    // 折叠 / 展开某个标题
    toggleCollapse(anchor) {
      this.collapsedMap[anchor] = !this.collapsedMap[anchor];
    },

    isCollapsed(anchor) {
      return !!this.collapsedMap[anchor];
    },

    // 当前标题是否有子标题（决定是否显示三角）
    hasChildren(h) {
      const idx = this.headings.findIndex(x => x.anchor === h.anchor);
      if (idx === -1) return false;
      const myLevel = h.level;

      for (let i = idx + 1; i < this.headings.length; i++) {
        const lv = this.headings[i].level;
        if (lv <= myLevel) return false; // 遇到同级/更上级则结束
        if (lv > myLevel) return true;   // 遇到更深层即有子孙
      }
      return false;
    },

    // 如果上方某个祖先被折叠，则当前标题隐藏
    isHiddenByParent(h) {
      const idx = this.headings.findIndex(x => x.anchor === h.anchor);
      if (idx <= 0) return false;

      let curLevel = h.level;
      for (let i = idx - 1; i >= 0; i--) {
        const prev = this.headings[i];
        if (prev.level < curLevel) {
          if (this.collapsedMap[prev.anchor]) return true;
          curLevel = prev.level; // 继续往更高祖先找
        }
      }
      return false;
    },

    formatDate(dateString) {
      if (!dateString) return '未知时间';
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return '无效日期';
      return date.toLocaleDateString('zh-CN');
    },

    // 滚动到页面顶部
    scrollToTop() {
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      });
    },

    // 滚动到评论区域（假定评论容器 id 为 comments）
    scrollToComments() {
      const el = document.getElementById('comments');
      if (!el) return;
      const rect = el.getBoundingClientRect();
      const offset = 90; // 和标题滚动偏移保持一致
      const top = window.pageYOffset + rect.top - offset;

      window.scrollTo({
        top,
        behavior: 'smooth'
      });
    },

    // 切换手机端目录抽屉
    toggleMobileToc() {
      this.isMobileTocOpen = !this.isMobileTocOpen;

      // 打开后主动聚焦，保证失焦（blur）能触发关闭
      if (this.isMobileTocOpen) {
        this.$nextTick(() => {
          if (this.$refs.tocSidebar && typeof this.$refs.tocSidebar.focus === 'function') {
            this.$refs.tocSidebar.focus();
          }
        });
      }
    },

    // 手机端：目录失焦后自动隐藏
    onMobileTocBlur() {
      if (this.isReadingMode) return;
      if (typeof window === 'undefined') return;
      if (window.innerWidth > 768) return;

      this.isMobileTocOpen = false;
    },

    // 阅读模式下：手机目录抽屉开关
    toggleMobileReaderToc() {
      if (typeof window === 'undefined') return;
      if (window.innerWidth > 768) return; // 桌面端不需要手动开关
      this.isMobileReaderTocOpen = !this.isMobileReaderTocOpen;
    },

    // 进入/退出阅读模式（全屏覆盖层，禁用页面滚动）
    toggleReadingMode() {
      if (this.isReadingMode) this.exitReadingMode();
      else this.enterReadingMode();
    },

    enterReadingMode() {
      if (this.isReadingMode) return;
      this.__prevBodyOverflow = document.body.style.overflow;
      this.__prevHtmlOverflow = document.documentElement.style.overflow;
      document.body.style.overflow = 'hidden';
      document.documentElement.style.overflow = 'hidden';

      this.isReadingMode = true;
      this.isMobileReaderTocOpen = false;
      this.__destroyedSmoothScrollByReader = false;

      // 移动端：阅读模式只允许内容滚动，避免全站 Lenis 抢占触摸导致无法滑动
      if (typeof window !== 'undefined' && window.innerWidth <= 768) {
        destroySmoothScroll();
        this.__destroyedSmoothScrollByReader = true;
      }

      this.$nextTick(() => {
        // Lenis 会接管滚动位置，这里只做一次初始化即可
        // 阅读模式是 v-if 动态渲染，需要重新绑定代码块复制按钮事件
        this.enhanceCodeBlocks();
        this.initReaderSmoothScroll();
      });
    },

    exitReadingMode() {
      if (!this.isReadingMode) return;

      this.destroyReaderSmoothScroll();
      this.isReadingMode = false;
      this.isMobileReaderTocOpen = false;
      // 移动端：恢复全站 Lenis
      if (this.__destroyedSmoothScrollByReader) {
        initSmoothScroll();
        this.__destroyedSmoothScrollByReader = false;
      }

      // 恢复页面滚动
      document.body.style.overflow =
        this.__prevBodyOverflow !== undefined ? this.__prevBodyOverflow : '';
      document.documentElement.style.overflow =
        this.__prevHtmlOverflow !== undefined ? this.__prevHtmlOverflow : '';

      this.__prevBodyOverflow = undefined;
      this.__prevHtmlOverflow = undefined;
    },

    // 阅读模式：滚动到指定锚点（针对内容滚动容器，不用 window.pageYOffset）
    scrollToAnchorReader(anchor) {
      const el = document.getElementById(anchor);
      const scroller = this.$refs.readerContentScroller;
      if (!el || !scroller) return;

      const offset = 24; // 给标题/间距留白

      if (this.__readerLenisContent && typeof this.__readerLenisContent.scrollTo === 'function') {
        this.__readerLenisContent.scrollTo(el, { offset: -offset });
        if (typeof window !== 'undefined' && window.innerWidth <= 768) {
          this.isMobileReaderTocOpen = false;
        }
        return;
      }

      // fallback（如果 Lenis 被系统“减少动态效果”禁用）
      const scrollerRect = scroller.getBoundingClientRect();
      const elRect = el.getBoundingClientRect();
      const nextTop = scroller.scrollTop + (elRect.top - scrollerRect.top) - offset;
      scroller.scrollTo({ top: nextTop, behavior: 'smooth' });

      if (typeof window !== 'undefined' && window.innerWidth <= 768) {
        this.isMobileReaderTocOpen = false;
      }
    },

    initReaderSmoothScroll() {
      if (!this.isReadingMode) return;
      if (this.__readerLenisContent || this.__readerLenisToc) return;

      // 移动端：避免 Lenis 自定义滚动容器在部分机型上“滚不动”
      // 让阅读内容与目录走原生容器滚动（仍可保持目录跟随逻辑）
      if (window.innerWidth <= 768) return;

      const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
      if (prefersReducedMotion) return;

      const contentWrapper = this.$refs.readerContentScroller;
      const content = this.$refs.readerArticle;
      if (contentWrapper && content) {
        this.__readerLenisContent = new Lenis({
          wrapper: contentWrapper,
          content,
          duration: 1.2,
          easing: (t) => 1 - Math.pow(1 - t, 5),
          smoothWheel: true,
          wheelMultiplier: 1.4,
          touchMultiplier: 0.5
        });
      }

      const tocWrapper = this.$refs.readerToc;
      const tocContent = this.$refs.readerTocContent;
      if (tocWrapper && tocContent) {
        this.__readerLenisToc = new Lenis({
          wrapper: tocWrapper,
          content: tocContent,
          duration: 1.2,
          easing: (t) => 1 - Math.pow(1 - t, 5),
          smoothWheel: true,
          wheelMultiplier: 1.4,
          touchMultiplier: 0.5
        });
      }

      const raf = (time) => {
        if (this.__readerLenisContent) this.__readerLenisContent.raf(time);
        if (this.__readerLenisToc) this.__readerLenisToc.raf(time);
        this.__readerLenisRafId = requestAnimationFrame(raf);
      };
      this.__readerLenisRafId = requestAnimationFrame(raf);
    },

    destroyReaderSmoothScroll() {
      if (this.__readerLenisRafId) {
        cancelAnimationFrame(this.__readerLenisRafId);
        this.__readerLenisRafId = null;
      }

      if (this.__readerLenisContent && typeof this.__readerLenisContent.destroy === 'function') {
        this.__readerLenisContent.destroy();
      }
      if (this.__readerLenisToc && typeof this.__readerLenisToc.destroy === 'function') {
        this.__readerLenisToc.destroy();
      }

      this.__readerLenisContent = null;
      this.__readerLenisToc = null;
    },

    // 为代码块绑定复制事件
    enhanceCodeBlocks() {
      const blocks = this.$el.querySelectorAll('.code-block');
      blocks.forEach(block => {
        const btn = block.querySelector('.code-block__copy');
        if (!btn || btn.dataset.bound === 'true') return;

        btn.dataset.bound = 'true';
        btn.addEventListener('click', () => {
          const codeEl = block.querySelector('pre code');
          if (!codeEl) return;
          const text = codeEl.innerText;

          const setCopied = () => {
            const oldText = btn.innerText;
            btn.innerText = '已复制';
            btn.classList.add('code-block__copy--success');
            setTimeout(() => {
              btn.innerText = oldText;
              btn.classList.remove('code-block__copy--success');
            }, 2000);
          };

          if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(text).then(setCopied).catch(() => {});
          } else {
            const textarea = document.createElement('textarea');
            textarea.value = text;
            textarea.style.position = 'fixed';
            textarea.style.opacity = '0';
            document.body.appendChild(textarea);
            textarea.select();
            try {
              document.execCommand('copy');
              setCopied();
            } catch (e) {
              console.warn('复制失败', e);
            } finally {
              document.body.removeChild(textarea);
            }
          }
        });
      });
    }
  },
  async created() {
    try {
      const response = await fetchPostById(this.id);
      this.post = response.data;
    } catch (error) {
      console.error('加载文章失败:', error);
    }
  },
  async mounted() {
    await this.$nextTick();
    const el = this.$refs.twikooPost;
    if (!el) {
      console.warn('Twikoo 容器未找到');
      return;
    }
    try {
      const tw = await loadTwikoo();
      await Promise.resolve(
        tw.init({
          envId: getTwikooEnvId(),
          el,
          path: `/posts/${this.id}`
        })
      );
    } catch (e) {
      console.error('文章页 Twikoo 初始化失败', e);
    }

    // 初始化并监听窗口变化，保证 TOC 始终贴在文章卡片右侧
    this.$nextTick(() => {
      this.updateTocPosition();
    });
    this._onResizeBound = () => this.updateTocPosition();
    window.addEventListener('resize', this._onResizeBound, { passive: true });
  }
  ,
  beforeDestroy() {
    if (this._onResizeBound) {
      window.removeEventListener('resize', this._onResizeBound);
      this._onResizeBound = null;
    }

    this.destroyReaderSmoothScroll();

      // 兜底：如果在阅读模式中离开页面，尽量恢复全站 Lenis
      if (this.__destroyedSmoothScrollByReader) {
        initSmoothScroll();
        this.__destroyedSmoothScrollByReader = false;
      }

    // 兜底：离开页面时恢复滚动
    if (this.isReadingMode) {
      document.body.style.overflow =
        this.__prevBodyOverflow !== undefined ? this.__prevBodyOverflow : '';
      document.documentElement.style.overflow =
        this.__prevHtmlOverflow !== undefined ? this.__prevHtmlOverflow : '';
    }
  }
};
</script>

<style scoped>
.post-container {
  display: flex;
  gap: 20px;
  max-width: 1200px;
  margin: 40px auto;
  padding: 20px;
  justify-content: center;
  align-items: flex-start;
}

/* 文章主体固定一个合理宽度 */
.post-main {
  flex: 1 1 700px;
  max-width: 700px;
  min-width: 0;
}

.toc-sidebar-container {
  flex: 0 0 260px;
  position: relative;
  align-self: flex-start;
}

/* 目录在右侧，宽度较窄，并固定在页头下面 */
.toc-sidebar {
  width: min(260px, calc(100vw - 32px));
  background-color: #2d2d2d;
  padding: 12px 10px;
  border-radius: 8px;
  font-size: 13px;
  color: #ccc;
  height: fit-content;
  overflow-y: auto;
  max-height: 80vh;
  position: fixed;
  right: 16px; /* 默认回退：避免窄屏重叠 */
  left: auto;
}

.toc-title {
  font-weight: 600;
  font-size: 13px;
  margin-bottom: 8px;
}

.toc-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.toc-item {
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 2px 0;
  cursor: pointer; /* 整行可点击跳转 */
}

/* 鼠标悬停高亮整行目录 */
.toc-item:hover {
  background-color: rgba(255, 255, 255, 0.04);
  border-radius: 4px;
}

.toc-toggle {
  cursor: pointer;
  font-size: 9px;    /* 略大一点，更明显 */
  color: #bbb;
  user-select: none;
  width: 10px;
  text-align: center;
}

.toc-toggle-placeholder {
  visibility: hidden;
}

.toc-link {
  color: #2e789d;
  text-decoration: none;
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.toc-link:hover {
  text-decoration: underline;
  color: #fff;
}

/* 桌面端贴右逻辑由 JS 动态计算 left，CSS 仅保留回退定位 */

/* 文章卡片样式 */
.post-detail {
  padding: 20px;
  background-color: #1e1e1ee0;
  color: #ffffff;
  border-radius: 8px;
  border: #ffffff 1px solid;
}

/* 正文基础样式 */
.post-content {
  line-height: 1.6;
  font-size: 16px;
}

/* 点击目录时防止标题被页头遮挡 */
:deep(.post-content h1),
:deep(.post-content h2),
:deep(.post-content h3),
:deep(.post-content h4),
:deep(.post-content h5),
:deep(.post-content h6) {
  scroll-margin-top: 90px; /* 视实际导航高度微调 */
}

/* 表格样式 */
:deep(.post-content table) {
  width: 100%;
  border-collapse: collapse;
  margin: 20px 0;
  background-color: #2d2d2d;
  color: #ffffff;
}

:deep(.post-content th),
:deep(.post-content td) {
  border: 1px solid #444;
  padding: 10px;
  text-align: left;
}

:deep(.post-content th) {
  background-color: #3a3a3a;
  font-weight: bold;
}

/* 代码块样式 */
:deep(.post-content pre) {
  background-color: #1e1e1e;
  padding: 14px 16px;
  overflow-x: auto;
}

:deep(.post-content code) {
  background-color: rgba(0, 0, 0, 0.22);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'JetBrains Mono', 'Fira Code', Menlo, Consolas, 'Courier New',
    monospace;
  font-size: 13px;
}

/* 独立代码块容器（含标题 & 复制按钮） */
:deep(.code-block) {
  position: relative;
  margin: 18px 0;
  border-radius: 12px;
  border: 1px solid #ffffff8e;
  overflow: hidden;
  background: radial-gradient(circle at top left, #2b3a4a, #141414);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5);
}

:deep(.code-block__header) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 12px;
  font-size: 12px;
  background: linear-gradient(90deg, rgba(72, 89, 117, 0.9), rgba(20, 20, 20, 0.95));
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

:deep(.code-block__lang) {
  color: #e2e8f0;
  font-weight: 500;
  letter-spacing: 0.03em;
}

:deep(.code-block__body) {
  margin: 0;
  padding: 10px 14px 12px;
  background: transparent;
  font-size: 13px;
}

:deep(.code-block__body code) {
  background: transparent;
  font-family: 'JetBrains Mono', 'Fira Code', Menlo, Consolas, 'Courier New',
    monospace;
}

:deep(.code-block__copy) {
  border: none;
  outline: none;
  background: rgba(15, 23, 42, 0.7);
  color: #cbd5f5;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: background 0.15s ease, transform 0.1s ease, box-shadow 0.15s ease,
    color 0.15s ease;
}

:deep(.code-block__copy:hover) {
  background: rgba(37, 99, 235, 0.9);
  color: #e5edff;
  transform: translateY(-0.5px);
  box-shadow: 0 0 0 1px rgba(96, 165, 250, 0.6);
}

:deep(.code-block__copy:active) {
  transform: translateY(0.5px) scale(0.98);
  box-shadow: none;
}

:deep(.code-block__copy--success) {
  background: rgba(22, 163, 74, 0.9);
  color: #e5ffe6;
}

/* 图片样式 */
:deep(.post-content img) {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 10px 0;
}

.post-meta,
.post-description {
  margin-bottom: 20px;
  font-size: 14px;
  color: #cccccc;
}

.post-description p {
  font-style: italic;
  margin: 0;
}

/* 文章内嵌评论区域样式 */
.post-comments {
  margin-top: 40px;
}

.post-comments-title {
  font-size: 20px;
  margin-bottom: 12px;
}

.post-comment-card {
  background-color: rgba(0, 0, 0, 0.8);
  border-radius: 10px;
  padding: 16px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

#tcomment-post {
  color: #ffffff;
}

/* 桌面端右下角悬浮按钮容器 */
.float-buttons {
  position: fixed;
  right: 32px;
  bottom: 40px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 1000;
}

.float-btn {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  border: none;
  background: #2e789d;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.4);
  transition: transform 0.15s ease, box-shadow 0.15s ease, background 0.15s ease;
}

.float-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.55);
  background: #3691c0;
}

/* 手机适配：窄屏下改为上下布局 */
@media (max-width: 768px) {
  .post-container {
    flex-direction: column;
    margin: 80px auto 40px;
    padding: 10px;
  }

  .post-main {
    flex: 1 1 auto;
    width: 100%;
  }

  .post-detail {
    padding: 16px;
  }

  /* 手机端：目录默认隐藏，点击右下角“目”按钮后，从右侧悬浮展开，带动画 */
  .toc-sidebar {
    position: fixed;
    right: 16px;
    bottom: 70px;
    width: 70vw;
    max-width: 320px;
    max-height: 82vh;
    margin-top: 0;
    z-index: 1100;
    border: 1px solid #ffffff;
    opacity: 0;
    transform: translateY(10px);
    pointer-events: none;
    transition: opacity 0.2s ease, transform 0.2s ease;
  }

  .toc-sidebar-container {
    position: static;
    flex: 0 0 auto;
  }

  .toc-sidebar.toc-sidebar--mobile-open {
    opacity: 1;
    transform: translateY(0);
    pointer-events: auto;
  }

  .toc-sidebar .toc-list {
    max-height: 72vh;
    overflow-y: auto;
  }

  /* 覆盖 markdown 中图片的内联宽度，使其在手机上占满容器 */
  :deep(.post-content img) {
    width: 100% !important;
    max-width: 100%;
    height: auto;
  }
}

/* 桌面端隐藏“目录”悬浮球（仅移动端需要） */
@media (min-width: 769px) {
  .float-btn--toc {
    display: none;
  }
}

/* =========================
   阅读模式（全屏覆盖层）
   ========================= */
.reader-overlay {
  position: fixed;
  inset: 0;
  z-index: 3000;
  background: #ffffff;
  color: #111827;
}

.reader-exit {
  position: fixed;
  top: 14px;
  right: 14px;
  z-index: 3001;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  color: #111827;
  padding: 8px 12px;
  border-radius: 10px;
  cursor: pointer;
  box-shadow: 0 6px 18px rgba(17, 24, 39, 0.08);
}

.reader-exit:hover {
  background: #f9fafb;
}

.reader-toc-mobile-toggle {
  display: none; /* 桌面端默认不显示 */
  position: fixed;
  top: 14px;
  left: 14px;
  z-index: 3001;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  color: #111827;
  padding: 8px 12px;
  border-radius: 10px;
  cursor: pointer;
  box-shadow: 0 6px 18px rgba(17, 24, 39, 0.08);
}

.reader-toc-mobile-toggle:hover {
  background: #f9fafb;
}

.reader-shell {
  height: 100vh;
  display: flex;
  gap: 24px;
  padding: 64px 24px 24px;
  box-sizing: border-box;
}

.reader-toc {
  width: min(280px, 30vw);
  flex: 0 0 auto;
  background: #ffffff; /* 移动端目录不透明白底 */
  border-right: 1px solid #e5e7eb;
  padding-right: 16px;
  overflow-y: auto;
  scrollbar-color: #6b7280 #e5e7eb; /* thumb track (Firefox) */
  scrollbar-width: thin; /* Firefox */
}

.reader-toc::-webkit-scrollbar {
  width: 10px;
}

.reader-toc::-webkit-scrollbar-track {
  background: #e5e7eb; /* 浅灰轨道 */
  border-radius: 999px;
}

.reader-toc::-webkit-scrollbar-thumb {
  background: #6b7280; /* 深灰滑块 */
  border-radius: 999px;
  border: 2px solid #e5e7eb; /* 让滑块更“细”且更柔和 */
}

.reader-toc::-webkit-scrollbar-thumb:hover {
  background: #4b5563;
}

.reader-toc-title {
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 12px;
}

.reader-toc-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.reader-toc-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 8px;
  border-radius: 10px;
  cursor: pointer;
  user-select: none;
}

.reader-toc-item:hover {
  background: #f3f4f6;
}

.reader-toc-item--active {
  background: #e5e7eb; /* 灰色标记 */
}

.reader-toc-item--active:hover {
  background: #e5e7eb;
}

.reader-toc-toggle {
  width: 12px;
  text-align: center;
  font-size: 11px;
  color: #6b7280;
  flex: 0 0 auto;
}

.reader-toc-toggle-placeholder {
  visibility: hidden;
}

.reader-toc-link {
  color: #111827;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.reader-main {
  flex: 1 1 auto;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.reader-content-scroller {
  flex: 1 1 auto;
  overflow-y: auto;
  min-height: 0;
  touch-action: pan-y;
  -webkit-overflow-scrolling: touch;
  scrollbar-color: #6b7280 #e5e7eb; /* thumb track (Firefox) */
  scrollbar-width: thin; /* Firefox */
}

.reader-content-scroller::-webkit-scrollbar {
  width: 10px;
}

.reader-content-scroller::-webkit-scrollbar-track {
  background: #e5e7eb; /* 浅灰轨道 */
  border-radius: 999px;
}

.reader-content-scroller::-webkit-scrollbar-thumb {
  background: #6b7280; /* 深灰滑块 */
  border-radius: 999px;
  border: 2px solid #e5e7eb;
}

.reader-content-scroller::-webkit-scrollbar-thumb:hover {
  background: #4b5563;
}

.reader-article {
  max-width: 920px;
  margin: 0 auto;
  padding-right: 10px;
}

.reader-title {
  font-size: 30px;
  line-height: 1.25;
  margin: 0 0 10px;
  font-weight: 800;
}

.reader-meta,
.reader-description {
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 18px;
}

.reader-description p {
  margin: 0;
  font-style: italic;
  color: #4b5563;
}

.reader-hr {
  border: none;
  border-top: 1px solid #e5e7eb;
  margin: 18px 0;
}

/* Markdown 正文：白底黑字的“技术文档”风格 */
.reader-post-content {
  color: #111827;
  font-size: 16px;
  line-height: 1.85;
}

.reader-post-content :deep(p) {
  margin: 0 0 14px;
}

.reader-post-content :deep(hr) {
  border: none;
  border-top: 1px solid #e5e7eb;
  margin: 22px 0;
}

.reader-post-content :deep(h1),
.reader-post-content :deep(h2),
.reader-post-content :deep(h3),
.reader-post-content :deep(h4),
.reader-post-content :deep(h5),
.reader-post-content :deep(h6) {
  scroll-margin-top: 28px;
  line-height: 1.35;
  margin: 26px 0 12px;
  font-weight: 800;
}

.reader-post-content :deep(h1) {
  font-size: 26px;
}

.reader-post-content :deep(h2) {
  font-size: 22px;
}

.reader-post-content :deep(h3) {
  font-size: 18px;
}

.reader-post-content :deep(h4) {
  font-size: 16px;
}

.reader-post-content :deep(a) {
  color: #2563eb;
  text-decoration: none;
}

.reader-post-content :deep(a:hover) {
  text-decoration: underline;
}

.reader-post-content :deep(img) {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 14px 0;
}

.reader-post-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 18px 0;
  background: #ffffff;
  color: #111827;
}

.reader-post-content :deep(th),
.reader-post-content :deep(td) {
  border: 1px solid #e5e7eb;
  padding: 10px;
  text-align: left;
  vertical-align: top;
}

.reader-post-content :deep(th) {
  background: #f9fafb;
  font-weight: 800;
}

.reader-post-content :deep(pre) {
  background: #f8fafc;
  padding: 14px 16px;
  border-radius: 10px;
  overflow-x: auto;
}

.reader-overlay :deep(.code-block) {
  margin: 18px 0;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  background: #f8fafc;
  box-shadow: none;
}

.reader-overlay :deep(.code-block__header) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  font-size: 12px;
  background: #f1f5f9;
  border-bottom: 1px solid #e5e7eb;
}

.reader-overlay :deep(.code-block__lang) {
  color: #334155;
  font-weight: 800;
  letter-spacing: 0.02em;
}

.reader-overlay :deep(.code-block__body) {
  margin: 0;
  padding: 10px 14px 12px;
  background: transparent;
  font-size: 13px;
  border-radius: 0;
}

.reader-overlay :deep(.code-block__body code) {
  font-family: 'JetBrains Mono', 'Fira Code', Menlo, Consolas, 'Courier New', monospace;
  font-size: 13px;
}

.reader-overlay :deep(.code-block__copy) {
  border: none;
  outline: none;
  background: #e5e7eb;
  color: #111827;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: background 0.15s ease, transform 0.1s ease;
}

.reader-overlay :deep(.code-block__copy:hover) {
  background: #d1d5db;
}

.reader-overlay :deep(.code-block__copy:active) {
  transform: translateY(0.5px) scale(0.98);
}

.reader-overlay :deep(.code-block__copy--success) {
  background: #dcfce7;
  color: #166534;
}

.reader-overlay :deep(.hljs) {
  background: transparent !important;
  color: #111827 !important;
}

/* 行内 code：轻量灰底 */
.reader-overlay :deep(.reader-post-content code:not([class])) {
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 6px;
  font-family: 'JetBrains Mono', 'Fira Code', Menlo, Consolas, 'Courier New', monospace;
  font-size: 0.95em;
}

@media (max-width: 768px) {
  /* 阅读模式：全屏优先 */
  .reader-shell {
    padding: 56px 0 0;
    gap: 0;
    flex-direction: column;
  }

  .reader-main {
    width: 100%;
  }

  .reader-content-scroller {
    padding: 0 16px;
  }

  .reader-article {
    max-width: 100%;
    margin: 0;
    padding-right: 0;
  }

  .reader-title {
    font-size: 22px;
  }

  /* 手机目录：默认隐藏，可通过按钮展开/收起 */
  .reader-toc {
    position: fixed;
    top: 56px;
    left: 0;
    bottom: 0;
    width: 78vw;
    max-width: 320px;
    z-index: 3002;

    background: #ffffff;
    opacity: 1;
    padding: 12px 10px 12px 12px;
    padding-right: 12px;

    border-right: 1px solid #e5e7eb;
    border-radius: 0 12px 12px 0;
    box-shadow: 0 16px 40px rgba(17, 24, 39, 0.12);

    transform: translateX(-110%);
    transition: transform 0.2s ease;
    pointer-events: none;
  }

  .reader-toc--mobile-open {
    transform: translateX(0);
    pointer-events: auto;
  }

  .reader-toc-mobile-toggle {
    display: block;
  }
}
</style>