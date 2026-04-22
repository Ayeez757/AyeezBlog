<template>
  <div class="talk-list page-card">
    <div class="page-toolbar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索说说内容关键词"
        class="toolbar-input"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-select v-model="publishedFilter" placeholder="发布状态" class="toolbar-select" clearable>
        <el-option label="已发布" :value="1" />
        <el-option label="草稿/下线" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="success" @click="goToAdd">发布说说</el-button>
    </div>

    <div class="table-wrap">
      <el-table :data="rows" stripe style="width: 100%;">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column label="内容" min-width="380">
          <template #default="{ row }">
            <div class="talk-content" :title="row.content || ''">{{ row.content || '' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="图片" width="90" align="center">
          <template #default="{ row }">
            <span>{{ imageCount(row.images) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.published === 1 ? 'success' : 'info'">
              {{ row.published === 1 ? '已发布' : '草稿/下线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column prop="updatedAt" label="更新时间" width="170" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button size="small" type="primary" @click="editRow(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteRow(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      layout="prev, pager, next"
      @current-change="handlePageChange"
      class="page-pagination"
    />
  </div>
</template>

<script>
import { getTalkList, deleteTalk } from '@/api'

export default {
  name: 'Talk',
  data() {
    return {
      rows: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      searchKeyword: '',
      publishedFilter: undefined
    }
  },
  mounted() {
    this.fetchRows()
  },
  methods: {
    imageCount(images) {
      if (!images) return 0
      try {
        const arr = JSON.parse(images)
        return Array.isArray(arr) ? arr.length : 0
      } catch (_) {
        return 0
      }
    },
    async fetchRows() {
      try {
        const data = await getTalkList({
          page: this.currentPage,
          pageSize: this.pageSize,
          keyword: this.searchKeyword?.trim() || undefined,
          published: this.publishedFilter
        })
        this.rows = data?.rows || []
        this.total = data?.total || 0
      } catch (e) {
        console.error('获取说说列表失败:', e)
        this.$message.error('获取说说列表失败，请稍后再试')
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchRows()
    },
    handlePageChange(page) {
      this.currentPage = page
      this.fetchRows()
    },
    goToAdd() {
      this.$router.push('/add-talk')
    },
    editRow(row) {
      this.$router.push(`/edit-talk/${row.id}`)
    },
    deleteRow(row) {
      this.$confirm(`确定删除这条说说吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(async () => {
          await deleteTalk({ id: row.id })
          this.$message.success('删除成功')
          this.fetchRows()
        })
        .catch(() => {
          this.$message.info('已取消删除')
        })
    }
  }
}
</script>

<style scoped>
.talk-list {
  padding: 16px;
}

.page-toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.toolbar-input {
  width: min(420px, 100%);
}

.toolbar-select {
  width: 160px;
}

.talk-content {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  white-space: pre-wrap;
  word-break: break-word;
  color: #111827;
}
</style>

