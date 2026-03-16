<template>
  <div class="post-container">
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
            <div id="tcomment-post"></div>
          </div>
        </section>
      </div>
    </main>

    <!-- 目录（右侧，可折叠） -->
    <aside
      class="toc-sidebar"
      :class="{ 'toc-sidebar--mobile-open': isMobileTocOpen }"
      v-if="headings.length"
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

    <!-- 桌面端右下角悬浮球 -->
    <div class="float-buttons">
      <button class="float-btn" @click="scrollToComments">
        评
      </button>
      <button class="float-btn" @click="scrollToTop">
        顶
      </button>
      <button class="float-btn" @click="toggleMobileToc">
        目
      </button>
    </div>
  </div>
</template>

<script>
import { fetchPostById } from '@/api';
import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js';
import fm from 'front-matter';
import 'highlight.js/styles/github-dark.css';

export default {
  name: 'PostDetail',
  props: ['id'],
  data() {
    return {
      post: {},
      frontMatter: {},
      headings: [],     // { level, title, anchor }
      collapsedMap: {}, // { [anchor]: boolean }
      isMobileTocOpen: false
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
        return `<img src="${src}" alt="${alt}" style="max-width: 100%; height: auto; width: 800px; display: block; margin: 10px 0;" />`;
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
      });
    }
  },
  methods: {
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
  mounted() {
    // 为当前文章初始化 Twikoo 评论（按文章 ID 区分评论串）
    if (window && window.twikoo) {
      window.twikoo.init({
        envId: 'https://twikoo.ayeez.cn',
        el: '#tcomment-post',
        path: `/posts/${this.id}`
      });
    } else {
      console.warn('Twikoo 未加载，无法初始化文章评论');
    }
  }
};
</script>

<style scoped>
.post-container {
  display: flex;
  gap: 20px;
  max-width: 1200px;
  margin: 100px auto;
  padding: 20px;
  justify-content: center;
}

/* 文章主体固定一个合理宽度 */
.post-main {
  flex: 0 0 800px;
}

/* 目录在右侧，宽度较窄，并固定在页头下面 */
.toc-sidebar {
  flex: 0 0 260px;
  background-color: #2d2d2d;
  padding: 12px 10px;
  border-radius: 8px;
  font-size: 13px;
  color: #ccc;
  overflow-y: auto;
  max-height: 80vh;
  position: sticky;
  top: 80px; /* 根据你的导航栏高度微调 */
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
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.toc-link:hover {
  text-decoration: underline;
  color: #fff;
}

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
  width: 300px;
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

  /* 手机端：目录默认隐藏，点击右下角“目”按钮后，从右侧悬浮展开 */
  .toc-sidebar {
    display: none;
  }

  .toc-sidebar.toc-sidebar--mobile-open {
    position: fixed;
    right: 16px;
    bottom: 90px;
    width: 70vw;
    max-width: 320px;
    max-height: 60vh;
    margin-top: 0;
    z-index: 1100;
    display: block;
    border: 1px solid #ffffff;
  }

  .toc-sidebar.toc-sidebar--mobile-open .toc-list {
    max-height: 50vh;
    overflow-y: auto;
  }

  /* 覆盖 markdown 中图片的内联宽度，使其在手机上占满容器 */
  :deep(.post-content img) {
    width: 100% !important;
    max-width: 100%;
    height: auto;
  }
}
</style>