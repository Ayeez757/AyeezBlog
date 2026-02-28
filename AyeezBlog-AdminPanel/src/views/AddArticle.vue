<template>
  <div class="add-article">
    <h2>添加文章</h2>

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
          <el-input v-model="form.abbrlink" placeholder="请输入短链接（abbrlink）" />
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
      <el-button type="primary" @click="submitArticle">提交文章</el-button>
      <el-button @click="resetForm">重置</el-button>
    </div>
  </div>
</template>

<script>
import MarkdownIt from 'markdown-it';
import fm from 'front-matter';

export default {
  
  data() {
    return {
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
      markdownContent: '',
      renderedHtml: ''
    };
  },
  created() {
    this.md = new MarkdownIt();
  },
  methods: {
     parseFrontMatter() {
    try {
      const { attributes, body } = fm(this.markdownContent);

      // 解析基础字段
      this.form.title = attributes.title || '';
      this.form.tags = attributes.tags ? attributes.tags.join(',') : '';
      this.form.category = attributes.categories ? attributes.categories.join(',') : '';
      this.form.description = attributes.description || '';
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
    submitArticle() {
      if (!this.form.title || !this.markdownContent.trim()) {
        this.$message.warning('请填写完整信息');
        return;
      }

      const postData = {
        title: this.form.title,
        tags: this.form.tags.split(',').map(tag => tag.trim()),
        category: this.form.category,
        description: this.form.description,
        cover: this.form.cover,
        abbrlink: this.form.abbrlink,
        date: this.form.date,
        updated: this.form.updated,
        content: this.markdownContent
      };

      // 以 JSON 形式输出提交数据
      console.log(JSON.stringify(postData, null, 2));

      this.$message.success('文章提交成功');
      this.$router.push('/article');
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