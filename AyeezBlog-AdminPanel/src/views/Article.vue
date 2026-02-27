<template>
  <div class="article-list">
    <!-- 搜索框和添加按钮 -->
    <div class="search-box">
      <el-input v-model="searchKeyword" placeholder="请输入文章标题关键词" style="width: 300px; margin-right: 10px;" clearable />
      <el-button type="primary" @click="fetchArticles">搜索</el-button>
      <el-button type="success" @click="goToAddArticle" style="margin-left: 10px;">添加文章</el-button>
    </div>

    <!-- 文章列表 -->
    <el-table :data="articles" stripe style="width: 100%; margin-top: 20px;">
      <el-table-column prop="title" label="标题" width="500" />
      <el-table-column prop="cover" label="封面" width="300">
        <template #default="scope">
          <img v-if="scope.row.cover" :src="scope.row.cover" alt="封面"
            style="width: 100px; height: 60px; object-fit: cover;" />
          <span v-else>无封面</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="150" />
      <el-table-column prop="updateTime" label="更新时间" width="150" />

      <!-- 操作列：编辑和删除按钮 -->
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" type="primary" @click="editArticle(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteArticle(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total"
      layout="prev, pager, next" @current-change="handlePageChange" style="margin-top: 20px; text-align: center;" />
  </div>
</template>

<script>
import { getPostList } from '@/api'; // 引入接口

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
          keyword: this.searchKeyword
        });

        this.articles = data.rows;
        this.total = data.total;
      } catch (error) {
        console.error('获取文章列表失败:', error);
        this.$message.error('获取文章列表失败，请稍后再试');
      }
    },
    handlePageChange(page) {
      this.currentPage = page;
      this.fetchArticles(); // 切换页码重新加载数据
    },
    editArticle(row) {
      // 编辑文章逻辑
      console.log('编辑文章:', row);
      this.$message.info(`正在编辑文章：${row.title}`);
      // 可以跳转到编辑页面或弹出对话框
    },
    deleteArticle(row) {
      // 删除文章逻辑
      this.$confirm(`确定删除文章 "${row.title}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(async () => {
          try {
            // 调用删除接口（假设存在）
            // await deletePost(row.id);
            this.$message.success('删除成功');
            this.fetchArticles(); // 重新加载数据
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
  padding: 20px;
}

.search-box {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}
</style>