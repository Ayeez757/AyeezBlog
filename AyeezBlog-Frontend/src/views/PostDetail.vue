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
      </div>
    </main>

    <!-- 目录（右侧，可折叠） -->
    <aside class="toc-sidebar" v-if="headings.length">
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
      collapsedMap: {}  // { [anchor]: boolean }
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
          return '';
        }
      });

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
    }
  },
  async created() {
    try {
      const response = await fetchPostById(this.id);
      this.post = response.data;
    } catch (error) {
      console.error('加载文章失败:', error);
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
  background-color: #2d2d2d;
  padding: 15px;
  border-radius: 5px;
  overflow-x: auto;
  margin: 15px 0;
}

:deep(.post-content code) {
  background-color: #3a3a3a;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
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
</style>