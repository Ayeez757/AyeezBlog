<template>
  <div class="article-list page-card">
    <div class="page-toolbar">
      <el-input
        v-model="searchKeyword"
        placeholder="请输入文章标题关键词"
        class="toolbar-input"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="success" @click="goToAddArticle">添加文章</el-button>
    </div>

    <div class="table-wrap">
      <el-table :data="articles" stripe style="width: 100%;">
      <el-table-column prop="title" label="标题" min-width="260" />
      <el-table-column label="角标" min-width="200" align="left">
        <template #default="{ row }">
          <div class="article-badges">
            <el-tag v-if="isTruthy(row.pinned)" size="small" effect="dark" class="article-badge article-badge--pinned">
              置顶
            </el-tag>
            <el-tag v-if="isTruthy(row.featured)" size="small" effect="dark" class="article-badge article-badge--featured">
              推荐
            </el-tag>
            <el-tag v-if="isTruthy(row.editing)" size="small" effect="dark" class="article-badge article-badge--editing">
              正在编辑
            </el-tag>
            <el-tag v-if="isTruthy(row.water)" size="small" effect="dark" class="article-badge article-badge--water">
              水
            </el-tag>
            <span v-if="!hasAnyBadge(row)" class="article-badges-none">—</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="cover" label="封面" width="300">
        <template #default="scope">
          <img v-if="scope.row.cover" :src="normalizeCoverUrl(scope.row.cover)" alt="封面" class="cover-img" />
          <span v-else>无封面</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="150" />
      <el-table-column prop="updateTime" label="更新时间" width="150" />

      <!-- 操作列：编辑和删除按钮 -->
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <div class="row-actions">
            <el-button size="small" type="primary" @click="editArticle(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteArticle(scope.row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
      </el-table>
    </div>

    <!-- 分页组件 -->
    <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total"
      layout="prev, pager, next" @current-change="handlePageChange" class="page-pagination" />
  </div>
</template>

<script>
import { getPostList,deletePost } from '@/api'; // 引入接口

export default {
  data() {
    return {
      articles: [], // 文章列表
      total: 0, // 总数
      currentPage: 1, // 当前页码
      pageSize: 10, // 每页条数
      searchKeyword: '' // 搜索关键词
    };
  },
  mounted() {
    this.fetchArticles(); // 初始化加载数据
  },
  methods: {
    async fetchArticles() {
      try {
        const data = await getPostList({
          page: this.currentPage,
          pageSize: this.pageSize,
          title: this.searchKeyword?.trim() || undefined
        });

        this.articles = data.rows;
        this.total = data.total;
      } catch (error) {
        console.error('获取文章列表失败:', error);
        this.$message.error('获取文章列表失败，请稍后再试');
      }
    },
    handleSearch() {
      this.currentPage = 1;
      this.fetchArticles();
    },
    handlePageChange(page) {
      this.currentPage = page;
      this.fetchArticles(); // 切换页码重新加载数据
    },
    isTruthy(v) {
      return v === true || v === 1 || v === '1';
    },
    hasAnyBadge(row) {
      return (
        this.isTruthy(row?.pinned)
        || this.isTruthy(row?.featured)
        || this.isTruthy(row?.editing)
        || this.isTruthy(row?.water)
      );
    },
    normalizeCoverUrl(url) {
      const raw = (url || '').trim();
      if (!raw) return '';
      if (/^https?:\/\//i.test(raw)) return raw;
      if (/^\/\//.test(raw)) return `https:${raw}`;
      return `https://${raw}`;
    },
    editArticle(row) {
      this.$router.push(`/edit-article/${row.id}`)
    },
    deleteArticle(row) {
  this.$confirm(`确定删除文章 "${row.title}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deletePost({ id: row.id });
        this.$message.success('删除成功');
        this.fetchArticles();
      } catch (error) {
        console.error('删除失败:', error);
        this.$message.error('删除失败，请稍后再试');
      }
    })
    .catch(() => {
      this.$message.info('已取消删除');
    });
}, goToAddArticle() {
      this.$router.push('/add-article'); // 跳转到添加文章页面
    }

  }
};
</script>

<style scoped>
.article-list {
  padding: 16px;
}

.cover-img {
  width: 100px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
}

.article-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}

.article-badges-none {
  color: #c0c4cc;
  font-size: 13px;
}

.article-badge {
  border: none;
}

.article-badge--pinned {
  background: linear-gradient(135deg, #ef4444, #dc2626) !important;
}

.article-badge--featured {
  background: linear-gradient(135deg, #22c55e, #16a34a) !important;
}

.article-badge--editing {
  background: linear-gradient(135deg, #7dd3fc, #38bdf8) !important;
  color: #0c4a6e !important;
}

.article-badge--water {
  background: linear-gradient(135deg, #3b82f6, #2563eb) !important;
}
</style>