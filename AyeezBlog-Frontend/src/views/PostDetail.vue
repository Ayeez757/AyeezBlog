<template>
  <div class="post-container">
    <!-- 目录 -->
    <aside class="toc-sidebar" v-if="tocHtml" v-html="tocHtml"></aside>

    <!-- 文章内容 -->
    <main class="post-main">
      <div class="post-detail">
        <!-- 显示 Front-matter 中的标题 -->
        <h1>{{ frontMatter.title || post.title }}</h1>

        <!-- 显示标签和分类 -->
        <div class="post-meta">
          <p v-if="frontMatter.tags">标签：{{ frontMatter.tags.join(', ') }}</p>
          <p v-if="frontMatter.category">分类：{{ frontMatter.category.join(', ') }}</p>
          <p>更新于 {{ formatDate(frontMatter.updated || post.updateTime || '') }}</p>
        </div>

        <!-- 显示描述 -->
        <div v-if="frontMatter.description" class="post-description">
          <p>{{ frontMatter.description }}</p>
        </div>

        <!-- 分割线 -->
        <hr />
        <hr />

        <!-- 渲染正文内容 -->
        <div class="post-content" v-html="renderedMarkdown"></div>
      </div>
    </main>
  </div>
</template>

<script>
import { fetchPostById } from '@/api';
import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js';
import fm from 'front-matter'; // 引入 front-matter
import 'highlight.js/styles/github-dark.css';

export default {
  name: 'PostDetail',
  props: ['id'],
  data() {
    return {
      post: {},
      frontMatter: {},
      tocHtml: '' // 存储生成的目录 HTML
    };
  },
  computed: {
    renderedMarkdown() {
      const md = new MarkdownIt({
        highlight: function (str, lang) {
          if (lang && hljs.getLanguage(lang)) {
            try {
              return hljs.highlight(str, { language: lang }).value;
            } catch (__) {}
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
        const anchor = title.toLowerCase().replace(/\s+/g, '-');
        return `<h${level} id="${anchor}">`;
      };

      // 解析 Front-matter 并分离内容
      const { body, attributes } = fm(this.post.content || '');
      this.frontMatter = attributes;

      // 提取标题并生成目录
      const headings = this.extractHeadings(body);
      this.tocHtml = this.generateToc(headings);

      // 渲染 Markdown 内容
      return md.render(body);
    }
  },
  methods: {
// 提取标题
extractHeadings(markdown) {
  const headings = [];
  const lines = markdown.split('\n');
  let inCodeBlock = false; // 标记是否在代码块中

  lines.forEach(line => {
    // 检查是否进入或退出代码块
    if (line.trim().startsWith('```')) {
      inCodeBlock = !inCodeBlock;
      return;
    }

    // 如果在代码块中，则跳过标题提取
    if (inCodeBlock) {
      return;
    }

    // 匹配标题（仅限普通文本）
    const match = line.match(/^(#{1,3})\s+(.*)$/);
    if (match) {
      const level = match[1].length;
      const title = match[2];
      const anchor = title.toLowerCase().replace(/\s+/g, '-');
      headings.push({ level, title, anchor });
    }
  });

  return headings;
},
// 生成目录 HTML
generateToc(headings) {
  let html = '<ul>';
  const stack = [];

  headings.forEach(heading => {
    const { level, title, anchor } = heading;

    // 处理层级嵌套
    while (stack.length > 0 && stack[stack.length - 1].level >= level) {
      html += '</ul></li>';
      stack.pop();
    }

    // 为不同层级添加类名
    const levelClass = `toc-level-${level}`;
    html += `<li class="${levelClass}"><a href="#${anchor}">${title}</a>`;
    if (level < 3) {
      html += '<ul>';
      stack.push({ level });
    }
  });

  // 关闭剩余标签
  while (stack.length > 0) {
    html += '</ul></li>';
    stack.pop();
  }

  html += '</ul>';
  return html;
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
}

.toc-sidebar {
  width: 250px;
  background-color: #2d2d2d;
  padding: 15px;
  border-radius: 8px;
  font-size: 14px;
  color: #cccccc;
  overflow-y: auto;
  max-height: 80vh;
  position: sticky;
  top: 20px;
}

:deep(.toc-sidebar ul) {
  list-style: none;
  padding-left: 0;
}

:deep(.toc-sidebar li) {
  margin: 8px 0;
}

:deep(.toc-sidebar a) {
  color: #2e789d;
  text-decoration: none;
}

:deep(.toc-sidebar a:hover) {
  text-decoration: underline;
  color: #ffffff;
}

/* 为不同层级添加缩进 */
:deep(.toc-level-1) {
  padding-left: 0; /* 第一级无缩进 */
}

:deep(.toc-level-2) {
  padding-left: 20px; /* 第二级缩进 20px */
}

:deep(.toc-level-3) {
  padding-left: 40px; /* 第三级缩进 40px */
}

.post-main {
  flex: 1;
}

.post-detail {
  padding: 20px;
  background-color: #1e1e1ee0;
  color: #ffffff;
  border-radius: 8px;
  border: #ffffff 1px solid;
}

.post-content {
  line-height: 1.6;
  font-size: 16px;
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