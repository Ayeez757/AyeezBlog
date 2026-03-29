<template>
  <div v-loading="loading" class="add-article page-card">
    <header class="article-page-header">
      <div class="article-page-header__text">
        <h1 class="article-page-title">{{ isEdit ? '编辑文章' : '添加文章' }}</h1>
        <p class="article-page-desc">
          填写元数据与正文；左侧编辑 Markdown，右侧实时预览。修改表单会同步到 Front-Matter。
        </p>
      </div>
    </header>

    <div class="article-meta-grid">
      <section class="article-panel">
        <h2 class="article-panel__title">基本信息</h2>
        <el-form :model="form" label-position="top" class="article-form">
          <el-form-item label="文章标题">
            <el-input v-model="form.title" placeholder="请输入文章标题" clearable />
          </el-form-item>
          <div class="article-form__row">
            <el-form-item label="标签" class="article-form__grow">
              <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" clearable />
            </el-form-item>
            <el-form-item label="分类" class="article-form__grow">
              <el-input v-model="form.category" placeholder="请输入分类" clearable />
            </el-form-item>
          </div>
          <el-form-item label="描述">
            <el-input
              v-model="form.description"
              type="textarea"
              placeholder="列表或摘要中显示的简短描述"
              :rows="3"
              resize="none"
            />
          </el-form-item>
          <el-form-item label="封面链接">
            <el-input v-model="form.cover" placeholder="https://…" clearable />
          </el-form-item>
          <div class="cover-preview-wrap cover-preview-wrap--meta">
            <span class="cover-preview-label">封面预览</span>
            <div v-if="coverPreviewSrc" class="cover-preview-frame">
              <img
                v-show="!coverPreviewError"
                :src="coverPreviewSrc"
                alt="封面预览"
                class="cover-preview-img"
                @error="coverPreviewError = true"
                @load="coverPreviewError = false"
              />
              <p v-if="coverPreviewError" class="cover-preview-fallback">图片无法加载，请检查链接</p>
            </div>
            <div v-else class="cover-preview-empty">填写链接后在此显示缩略图</div>
          </div>
        </el-form>
      </section>

      <section class="article-panel article-panel--aside">
        <h2 class="article-panel__title">发布与展示</h2>
        <el-form :model="form" label-position="top" class="article-form">
          <el-form-item label="短链接（abbrlink）">
            <el-input v-model="form.abbrlink" placeholder="新建时可填写" :disabled="isEdit" clearable />
          </el-form-item>
          <div class="article-form__row article-form__row--dates">
            <el-form-item label="创建时间">
              <el-date-picker
                v-model="form.date"
                type="date"
                placeholder="日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="更新时间">
              <el-date-picker
                v-model="form.updated"
                type="date"
                placeholder="日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </div>
          <el-form-item label="列表角标">
            <div class="badge-switch-row">
              <div class="badge-switch-item">
                <span class="badge-switch-item__label">置顶</span>
                <el-switch v-model="form.pinned" />
              </div>
              <div class="badge-switch-item">
                <span class="badge-switch-item__label">推荐</span>
                <el-switch v-model="form.featured" />
              </div>
              <div class="badge-switch-item">
                <span class="badge-switch-item__label">正在编辑</span>
                <el-switch v-model="form.editing" />
              </div>
              <div class="badge-switch-item">
                <span class="badge-switch-item__label">水</span>
                <el-switch v-model="form.water" />
              </div>
            </div>
          </el-form-item>
        </el-form>
      </section>
    </div>

    <section class="article-panel article-panel--editor">
      <div class="editor-split">
        <div class="editor-pane editor-pane--source">
          <div class="editor-pane__head">
            <span class="editor-pane__label">Markdown 源码</span>
            <span class="editor-pane__hint">支持 Front-Matter</span>
          </div>
          <div class="editor-pane__body">
            <el-input
              v-model="markdownContent"
              type="textarea"
              placeholder="支持 Front-Matter；正文使用 Markdown 编写"
              :autosize="false"
              class="editor-textarea"
              @input="parseFrontMatter"
            />
          </div>
        </div>
        <div class="editor-pane editor-pane--preview">
          <div class="editor-pane__head">
            <span class="editor-pane__label">正文预览</span>
            <span class="editor-pane__hint">Markdown 渲染</span>
          </div>
          <div class="preview-pane-scroll">
            <div class="md-preview" v-html="renderedHtml"></div>
          </div>
        </div>
      </div>
    </section>

    <footer class="article-actions">
      <el-button size="large" @click="resetForm">重置</el-button>
      <el-button type="primary" size="large" @click="submitArticle">
        {{ isEdit ? '保存修改' : '提交文章' }}
      </el-button>
    </footer>
  </div>
</template>

<script>
import MarkdownIt from 'markdown-it';
import fm from 'front-matter';
import yaml from 'js-yaml';
import { addPost, getPostDetail, updatePost } from '../api/index';

export default {
  data() {
    return {
      loading: false,
      /** 从表单写回 Markdown 时跳过 parse 对表单的覆盖，避免循环 */
      suppressParseToForm: false,
      /** 正在从 Markdown 解析回填表单，禁止触发表单→正文同步 */
      syncingFromParse: false,
      /** 加载文章或程序写 Markdown 时不应触发表单→正文同步 */
      suppressFormWatch: false,
      _formSyncTimer: null,
      form: {
        title: '',
        tags: '',
        category: '',
        description: '',
        cover: '',
        abbrlink: '',
        date: '', // 创建时间
        updated: '', // 更新时间
        pinned: false,
        featured: false,
        editing: false,
        water: false
      },
      // 保存从 front-matter 解析出来的原始结构，提交时优先用它
      parsedFrontMatter: {
        tags: [],
        categories: []
      },
      markdownContent: '',
      renderedHtml: '',
      coverPreviewError: false
    };
  },
  computed: {
    isEdit() {
      return Boolean(this.$route.params && this.$route.params.id);
    },
    articleId() {
      return (this.$route.params && this.$route.params.id) || '';
    },
    coverPreviewSrc() {
      return (this.form.cover || '').trim();
    }
  },
  watch: {
    coverPreviewSrc() {
      this.coverPreviewError = false;
    },
    form: {
      deep: true,
      handler() {
        if (this.suppressFormWatch || this.loading || this.syncingFromParse) return;
        clearTimeout(this._formSyncTimer);
        this._formSyncTimer = setTimeout(() => {
          this.applyFormToMarkdown();
        }, 200);
      }
    }
  },
  beforeUnmount() {
    clearTimeout(this._formSyncTimer);
  },
  async created() {
    this.md = new MarkdownIt();
    if (this.isEdit) {
      await this.loadArticle();
    }
  },
  methods: {
    normalizeToArray(value) {
      if (value == null) return [];
      if (Array.isArray(value)) return value.map(v => String(v)).filter(Boolean);
      if (typeof value === 'string') {
        // 兼容用户手动在输入框里用逗号分隔
        return value.split(',').map(s => s.trim()).filter(Boolean);
      }
      return [String(value)];
    },
    formatToYmd(value) {
      if (!value) return '';
      const d = new Date(value);
      if (Number.isNaN(d.getTime())) return '';
      const yyyy = d.getFullYear();
      const mm = String(d.getMonth() + 1).padStart(2, '0');
      const dd = String(d.getDate()).padStart(2, '0');
      return `${yyyy}-${mm}-${dd}`;
    },
    async loadArticle() {
      this.loading = true;
      this.suppressFormWatch = true;
      try {
        const post = await getPostDetail({ id: this.articleId });
        if (!post) {
          this.$message.error('文章不存在或已被删除');
          this.$router.push('/article');
          return;
        }
        this.form.title = post.title || '';
        this.form.description = post.description || '';
        this.form.cover = post.cover || '';
        // 文章主键即 abbrlink；勿仅用 YAML，避免解析后覆盖为空
        this.form.abbrlink =
          post.id != null && String(post.id).trim() !== ''
            ? String(post.id).trim()
            : String(this.articleId || '');
        this.form.date = this.formatToYmd(post.createTime);
        this.form.updated = this.formatToYmd(post.updateTime);
        this.form.pinned = Boolean(post.pinned);
        this.form.featured = Boolean(post.featured);
        this.form.editing = Boolean(post.editing);
        this.form.water = Boolean(post.water);
        this.markdownContent = post.content || '';

        // 触发一次解析/渲染（如果无 Front-Matter 也不会影响）
        this.parseFrontMatter();
        if (!this.renderedHtml) {
          const parsed = fm(this.markdownContent || '');
          this.renderedHtml = this.md.render(parsed.body || '');
        }
      } catch (error) {
        console.error('获取文章详情失败:', error);
        this.$message.error('获取文章详情失败，请稍后再试');
      } finally {
        this.loading = false;
        this.$nextTick(() => {
          this.suppressFormWatch = false;
        });
      }
    },
    buildAttributesFromForm() {
      const tags = this.normalizeToArray(this.form.tags);
      const categories = this.normalizeToArray(this.form.category);
      return {
        title: this.form.title || '',
        tags,
        categories,
        description: this.form.description != null ? String(this.form.description) : '',
        cover: this.form.cover || '',
        abbrlink: this.form.abbrlink || '',
        date: this.form.date || '',
        updated: this.form.updated || '',
        pinned: !!this.form.pinned,
        featured: !!this.form.featured,
        editing: !!this.form.editing,
        water: !!this.form.water
      };
    },
    assembleMarkdownWithFrontMatter(attributes, body) {
      const yamlStr = yaml
        .dump(attributes, { lineWidth: -1, sortKeys: true, noRefs: true })
        .replace(/\n$/, '');
      return `---\n${yamlStr}\n---\n\n${body}`;
    },
    applyFormToMarkdown() {
      if (this.suppressFormWatch || this.loading || this.syncingFromParse) return;
      try {
        const parsed = fm(this.markdownContent || '');
        const body = parsed.body != null ? parsed.body : '';
        const prev = parsed.attributes && typeof parsed.attributes === 'object' ? { ...parsed.attributes } : {};
        const fromForm = this.buildAttributesFromForm();
        const merged = { ...prev, ...fromForm };
        if (merged.categories != null) delete merged.category;
        const next = this.assembleMarkdownWithFrontMatter(merged, body);
        if (next === this.markdownContent) return;

        this.suppressParseToForm = true;
        this.markdownContent = next;
        this.parsedFrontMatter.tags = this.normalizeToArray(this.form.tags);
        this.parsedFrontMatter.categories = this.normalizeToArray(this.form.category);

        const after = fm(this.markdownContent);
        this.renderedHtml = this.md.render(after.body || '');
        this.$nextTick(() => {
          this.suppressParseToForm = false;
        });
      } catch (error) {
        console.error('同步表单到 Front-Matter 失败:', error);
        this.suppressParseToForm = false;
      }
    },
    parseFrontMatter() {
      try {
        const { attributes, body } = fm(this.markdownContent);

        if (!this.suppressParseToForm) {
          this.syncingFromParse = true;
          this.form.title = attributes.title || '';
          const tagsArr = this.normalizeToArray(attributes.tags);
          const categoriesRaw =
            attributes.categories != null ? attributes.categories : attributes.category;
          const categoriesArr = this.normalizeToArray(categoriesRaw);

          this.parsedFrontMatter.tags = tagsArr;
          this.parsedFrontMatter.categories = categoriesArr;

          this.form.tags = tagsArr.join(',');
          this.form.category = categoriesArr.join(',');
          this.form.description =
            attributes.description != null ? String(attributes.description) : '';
          this.form.cover = attributes.cover || '';
          // 编辑页以路由/接口中的文章 id 为准，避免 front matter 未写 abbrlink 时被清空
          if (this.isEdit) {
            this.form.abbrlink = String(this.articleId || '');
          } else {
            const fromYaml = attributes.abbrlink;
            this.form.abbrlink =
              fromYaml != null && String(fromYaml).trim() !== ''
                ? String(fromYaml).trim()
                : '';
          }

          this.form.date = this.parseDate(attributes.date);
          this.form.updated = this.parseDate(attributes.updated);

          if (attributes.pinned !== undefined) this.form.pinned = Boolean(attributes.pinned);
          if (attributes.featured !== undefined) this.form.featured = Boolean(attributes.featured);
          if (attributes.editing !== undefined) this.form.editing = Boolean(attributes.editing);
          if (attributes.water !== undefined) this.form.water = Boolean(attributes.water);
          this.$nextTick(() => {
            this.syncingFromParse = false;
          });
        }

        this.renderedHtml = this.md.render(body);
      } catch (error) {
        console.error('Front-Matter 解析失败:', error);
        this.$message.error('Front-Matter 格式错误，请检查内容');
      }
    },
  //解析日期字段
  parseDate(dateString) {
    if (!dateString) return ''; // 如果为空，返回空字符串

    // 支持 YYYY-MM-DD 格式
    const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
    if (dateRegex.test(dateString)) {
      return dateString; // 直接返回原始字符串
    }

    // 其他情况尝试转换为标准格式
    const date = new Date(dateString);
    if (!isNaN(date.getTime())) {
      return date.toISOString().split('T')[0]; // 返回 YYYY-MM-DD 格式
    }

    return ''; // 不合法则返回空字符串
  },
    async submitArticle() {
      if (!this.form.title || !this.markdownContent.trim()) {
        this.$message.warning('请填写完整信息');
        return;
      }

      const postData = {
        id: this.isEdit ? this.articleId : (this.form.abbrlink || undefined),
        title: this.form.title,
        tags: (this.parsedFrontMatter.tags && this.parsedFrontMatter.tags.length > 0)
          ? this.parsedFrontMatter.tags
          : (this.form.tags ? this.form.tags.split(',').map(tag => tag.trim()).filter(Boolean) : []),
        // 后端当前接收字段名是 category（PostBody#setCategoryFromObject），这里继续发 category
        // 但来源优先用 front-matter 的 categories 数组（即你写的格式）
        category: (this.parsedFrontMatter.categories && this.parsedFrontMatter.categories.length > 0)
          ? this.parsedFrontMatter.categories
          : (this.form.category || null),
        description: this.form.description,
        cover: this.form.cover,
        // 后端支持多种日期格式，但不接受空字符串，这里改为在为空时传 null
        date: this.form.date || null,
        updated: this.form.updated || null,
        content: this.markdownContent,
        pinned: !!this.form.pinned,
        featured: !!this.form.featured,
        editing: !!this.form.editing,
        water: !!this.form.water
      };

      // 以 JSON 形式输出提交数据
      console.log(JSON.stringify(postData, null, 2));

      try {
        if (this.isEdit) {
          await updatePost(postData);
          this.$message.success('文章更新成功');
        } else {
          await addPost(postData);
          this.$message.success('文章提交成功');
        }
        this.$router.push('/article');
      } catch (error) {
        console.error(this.isEdit ? '更新文章失败:' : '添加文章失败:', error);
        this.$message.error(this.isEdit ? '更新文章失败，请稍后再试' : '添加文章失败，请稍后再试');
      }
    },
    resetForm() {
      this.suppressFormWatch = true;
      this.form = {
        title: '',
        tags: '',
        category: '',
        description: '',
        cover: '',
        abbrlink: '',
        date: '',
        updated: '',
        pinned: false,
        featured: false,
        editing: false,
        water: false
      };
      this.markdownContent = '';
      this.renderedHtml = '';
      this.coverPreviewError = false;
      this.parsedFrontMatter = { tags: [], categories: [] };
      this.$nextTick(() => {
        this.suppressFormWatch = false;
      });
    }
  }
};

</script>

<style scoped>
.add-article {
  --article-accent: #3b82f6;
  --article-border: #e5e7eb;
  --article-muted: #64748b;
  --article-surface: #f8fafc;
  /* 与右侧预览同高：整栏占满视口 */
  --editor-pane-h: calc(100vh - 32px);

  padding: 20px 22px 24px;
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
}

.article-page-header {
  margin-bottom: 20px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--article-border);
}

.article-page-title {
  margin: 0 0 8px;
  font-size: 1.5rem;
  font-weight: 600;
  letter-spacing: -0.02em;
  color: #0f172a;
}

.article-page-desc {
  margin: 0;
  font-size: 0.875rem;
  line-height: 1.55;
  color: var(--article-muted);
}

.article-meta-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(280px, 0.85fr);
  gap: 16px;
  margin-bottom: 16px;
  align-items: start;
}

.article-panel {
  background: var(--article-surface);
  border: 1px solid var(--article-border);
  border-radius: 12px;
  padding: 18px 20px;
}

.article-panel--editor {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: 0;
  overflow: visible;
}

.article-panel__title {
  margin: 0 0 14px;
  font-size: 0.9375rem;
  font-weight: 600;
  color: #334155;
}

.article-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #475569;
}

.article-form__row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 16px;
}

.article-form__grow {
  min-width: 0;
}

.article-form__row--dates {
  grid-template-columns: 1fr 1fr;
}

.badge-switch-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
}

.badge-switch-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 8px 12px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.badge-switch-item__label {
  font-size: 0.8125rem;
  color: #475569;
}

.editor-split {
  display: grid;
  grid-template-columns: 1fr 1fr;
  align-items: start;
  gap: 0;
  min-height: 260px;
}

.editor-pane {
  display: flex;
  flex-direction: column;
  min-height: 0;
  min-width: 0;
}

.editor-pane--source {
  border-right: 1px solid var(--article-border);
  height: var(--editor-pane-h);
  max-height: var(--editor-pane-h);
}

.editor-pane__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 16px;
  background: linear-gradient(180deg, #fff 0%, #f8fafc 100%);
  border-bottom: 1px solid var(--article-border);
}

.editor-pane__label {
  font-size: 0.8125rem;
  font-weight: 600;
  color: #334155;
}

.editor-pane__hint {
  font-size: 0.75rem;
  color: var(--article-muted);
}

.editor-pane--source .editor-pane__body {
  display: flex;
  flex-direction: column;
}

.editor-pane__body {
  flex: 1;
  min-height: 0;
  padding: 12px 14px 14px;
  overflow: auto;
  background: #fff;
}

.editor-textarea {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.editor-textarea :deep(.el-textarea) {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.editor-textarea :deep(textarea) {
  font-family: ui-monospace, "Cascadia Code", "SF Mono", Menlo, Consolas, monospace;
  font-size: 13px;
  line-height: 1.55;
  border: none;
  box-shadow: none;
  padding: 10px 12px;
  background: #f1f5f9;
  border-radius: 8px;
  min-height: 200px;
  height: 100% !important;
  resize: none;
}

.editor-textarea :deep(textarea:focus) {
  background: #fff;
  outline: 2px solid rgba(59, 130, 246, 0.25);
  outline-offset: 0;
}

/* 右侧预览：与左侧同高，占满视口，滚动浏览正文 */
.editor-pane--preview {
  position: sticky;
  top: 16px;
  align-self: start;
  width: 100%;
  min-height: 0;
  height: var(--editor-pane-h);
  max-height: var(--editor-pane-h);
}

.preview-pane-scroll {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 12px 14px 14px;
  background: #fafafa;
}

.cover-preview-label {
  display: block;
  font-size: 0.75rem;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 8px;
}

/* 首页 Home.vue：.post-cover 为 width100% × height180px（桌面）、object-fit:cover；
   三列卡片约 320px 宽 → 16:9。此处缩小尺寸但保持同一比例与裁切方式 */
.cover-preview-wrap--meta .cover-preview-frame {
  position: relative;
  width: 100%;
  max-width: 200px;
  aspect-ratio: 16 / 9;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  background: #f1f5f9;
}

.cover-preview-wrap--meta .cover-preview-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-preview-wrap--meta .cover-preview-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  max-width: 200px;
  aspect-ratio: 16 / 9;
  margin: 0;
  padding: 8px 10px;
  font-size: 0.75rem;
  line-height: 1.35;
  color: #94a3b8;
  border-radius: 10px;
  border: 1px dashed #cbd5e1;
  background: #fff;
  text-align: center;
  box-sizing: border-box;
}

.cover-preview-wrap--meta .cover-preview-fallback {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0;
  padding: 8px;
  font-size: 0.75rem;
  color: #b45309;
  text-align: center;
  background: rgba(255, 255, 255, 0.92);
}

.cover-preview-wrap--meta {
  margin-top: 6px;
  margin-bottom: 0;
}

.md-preview {
  font-size: 0.9375rem;
  line-height: 1.65;
  color: #1e293b;
  padding: 0 4px 8px;
}

.md-preview :deep(h1),
.md-preview :deep(h2),
.md-preview :deep(h3) {
  margin: 1.1em 0 0.5em;
  font-weight: 600;
  line-height: 1.35;
  color: #0f172a;
}

.md-preview :deep(h1) {
  font-size: 1.35rem;
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 0.35em;
}

.md-preview :deep(p) {
  margin: 0.65em 0;
}

.md-preview :deep(a) {
  color: var(--article-accent);
}

.md-preview :deep(code) {
  font-family: ui-monospace, Menlo, Consolas, monospace;
  font-size: 0.85em;
  background: #f1f5f9;
  padding: 0.15em 0.4em;
  border-radius: 4px;
}

.md-preview :deep(pre) {
  background: #1e293b;
  color: #e2e8f0;
  padding: 12px 14px;
  border-radius: 8px;
  overflow-x: auto;
  font-size: 0.8125rem;
}

.md-preview :deep(pre code) {
  background: none;
  padding: 0;
  color: inherit;
}

.md-preview :deep(blockquote) {
  margin: 0.75em 0;
  padding: 0.35em 0 0.35em 0.9em;
  border-left: 3px solid #cbd5e1;
  color: #475569;
  background: rgba(241, 245, 249, 0.6);
}

.md-preview :deep(img) {
  max-width: 100%;
  max-height: 220px;
  object-fit: contain;
  border-radius: 6px;
}

.md-preview :deep(ul),
.md-preview :deep(ol) {
  padding-left: 1.35em;
  margin: 0.5em 0;
}

.article-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  margin-top: 20px;
  padding-top: 18px;
  border-top: 1px solid var(--article-border);
}

@media (max-width: 1024px) {
  .article-meta-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .add-article {
    padding: 14px 12px 18px;
    --editor-pane-h: min(72vh, calc(100vh - 140px));
  }

  .article-form__row,
  .article-form__row--dates {
    grid-template-columns: 1fr;
  }

  .editor-split {
    grid-template-columns: 1fr;
    min-height: 0;
  }

  .editor-pane--source {
    border-right: none;
    border-bottom: 1px solid var(--article-border);
    height: var(--editor-pane-h);
    max-height: var(--editor-pane-h);
  }

  .editor-pane--preview {
    position: static;
    top: auto;
    height: var(--editor-pane-h);
    max-height: var(--editor-pane-h);
  }

  .cover-preview-wrap--meta .cover-preview-frame,
  .cover-preview-wrap--meta .cover-preview-empty {
    max-width: 168px;
  }

  .editor-pane--source .editor-pane__body,
  .preview-pane-scroll {
    max-height: none;
  }

  .article-actions {
    flex-direction: column-reverse;
    align-items: stretch;
  }

  .article-actions .el-button {
    width: 100%;
    margin: 0;
  }
}
</style>