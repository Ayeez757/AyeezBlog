<template>
  <div class="add-article">
    <h2>{{ isEdit ? '编辑文章' : '添加文章' }}</h2>

    <!-- 表单区域 -->
    <div class="form-section">
      <el-form :model="form" label-width="100px" style="max-width: 600px;">
        <el-form-item label="文章标题">
          <el-input v-model="form.title" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="请输入标签（多个标签用逗号分隔）" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" placeholder="请输入分类" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="请输入文章描述"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="封面">
          <el-input v-model="form.cover" placeholder="请输入封面图片链接" />
        </el-form-item>
        <el-form-item label="短链接">
          <el-input v-model="form.abbrlink" placeholder="请输入短链接（abbrlink）" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="创建时间">
  <el-date-picker
    v-model="form.date"
    type="date"
    placeholder="请选择创建时间"
    format="YYYY-MM-DD"
    value-format="YYYY-MM-DD"
  />
</el-form-item>
<el-form-item label="更新时间">
  <el-date-picker
    v-model="form.updated"
    type="date"
    placeholder="请选择更新时间"
    format="YYYY-MM-DD"
    value-format="YYYY-MM-DD"
  />
</el-form-item>
      </el-form>
    </div>

    <!-- Markdown 编辑器 -->
    <div class="editor-container">
      <div class="editor-left">
        <el-input
          v-model="markdownContent"
          type="textarea"
          placeholder="请输入 Markdown 内容..."
          :rows="30"
          @input="parseFrontMatter"
        />
      </div>
      <div class="editor-right" v-html="renderedHtml"></div>
    </div>

    <!-- 提交按钮 -->
    <div class="submit-section">
      <el-button type="primary" @click="submitArticle">{{ isEdit ? '保存修改' : '提交文章' }}</el-button>
      <el-button @click="resetForm">重置</el-button>
    </div>
  </div>
</template>

<script>
import MarkdownIt from 'markdown-it';
import fm from 'front-matter';
import { addPost, getPostDetail, updatePost } from '../api/index';

export default {
  
  data() {
    return {
      loading: false,
      form: {
        title: '',
        tags: '',
        category: '',
        description: '',
        cover: '',
        abbrlink: '',
        date: '', // 创建时间
        updated: '' // 更新时间
      },
      // 保存从 front-matter 解析出来的原始结构，提交时优先用它
      parsedFrontMatter: {
        tags: [],
        categories: []
      },
      markdownContent: '',
      renderedHtml: ''
    };
  },
  computed: {
    isEdit() {
      return Boolean(this.$route.params && this.$route.params.id);
    },
    articleId() {
      return (this.$route.params && this.$route.params.id) || '';
    }
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
        this.form.abbrlink = post.id || this.articleId;
        this.form.date = this.formatToYmd(post.createTime);
        this.form.updated = this.formatToYmd(post.updateTime);
        this.markdownContent = post.content || '';

        // 触发一次解析/渲染（如果无 Front-Matter 也不会影响）
        this.parseFrontMatter();
        if (!this.renderedHtml) {
          this.renderedHtml = this.md.render(this.markdownContent || '');
        }
      } catch (error) {
        console.error('获取文章详情失败:', error);
        this.$message.error('获取文章详情失败，请稍后再试');
      } finally {
        this.loading = false;
      }
    },
     parseFrontMatter() {
    try {
      const { attributes, body } = fm(this.markdownContent);

      // 解析基础字段
      this.form.title = attributes.title || '';
      const tagsArr = this.normalizeToArray(attributes.tags);
      // 注意：你的 front-matter 用的是 categories（不是 category）
      const categoriesRaw = attributes.categories != null ? attributes.categories : attributes.category;
      const categoriesArr = this.normalizeToArray(categoriesRaw);

      this.parsedFrontMatter.tags = tagsArr;
      this.parsedFrontMatter.categories = categoriesArr;

      this.form.tags = tagsArr.join(',');
      this.form.category = categoriesArr.join(',');
      this.form.description = (attributes.description != null) ? String(attributes.description) : '';
      this.form.cover = attributes.cover || '';
      this.form.abbrlink = attributes.abbrlink || '';

      // 解析 date 和 updated 字段，支持 YYYY-MM-DD 格式
      this.form.date = this.parseDate(attributes.date);
      this.form.updated = this.parseDate(attributes.updated);

      // 渲染 Markdown 内容
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
        content: this.markdownContent
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
      this.form = {
        title: '',
        tags: '',
        category: '',
        description: '',
        cover: '',
        abbrlink: '',
        date: '',
        updated: ''
      };
      this.markdownContent = '';
      this.renderedHtml = '';
    }
  }
};

</script>

<style scoped>
.add-article {
  padding: 20px;
  height: 90vh;
  display: flex;
  flex-direction: column;
  width: 80vw;
}

.form-section {
  margin-bottom: 20px;
}

.editor-container {
  display: flex;
  flex: 1;
  gap: 20px;
  margin-top: 20px;
  width: 100%;
  height: 100%;
}

.editor-left,
.editor-right {
  flex: 1;
  height: 100%;
  overflow: auto;
  border: 1px solid #ddd;
  padding: 10px;
  border-radius: 4px;
}

.editor-left textarea {
  width: 100%;
  height: 100%;
  resize: none;
}

.editor-right {
  background-color: #f9f9f9;
}

/* 调小渲染图片尺寸 */
.editor-right img {
  max-width: 300px;
  max-height: 200px;
  object-fit: contain;
}

.submit-section {
  margin-top: 20px;
  text-align: center;
}
</style>