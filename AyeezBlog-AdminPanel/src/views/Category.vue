<template>
  <div class="category-page page-card">
    <div class="page-toolbar">
      <el-input
        v-model="keyword"
        placeholder="请输入分类名称"
        clearable
        class="toolbar-input"
      />
      <el-button type="primary" @click="fetchList">搜索</el-button>
      <el-button type="success" @click="openAddDialog">新增分类</el-button>
    </div>

    <div class="table-wrap">
    <el-table :data="tableData" stripe style="width: 100%;">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="name" label="分类名称" min-width="180" />
      <el-table-column prop="description" label="描述" min-width="260" />
      <el-table-column prop="sort" label="排序" width="100" />
      <el-table-column label="文章数" width="120">
        <template #default="scope">
          <el-button type="primary" link @click="showPosts(scope.row)">
            {{ scope.row.postCount || 0 }}
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <div class="row-actions">
            <el-button size="small" type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="removeCategory(scope.row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    </div>

    <el-dialog :title="isEdit ? '编辑分类' : '新增分类'" v-model="dialogVisible" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="分类名称">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="请输入描述" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog :title="postDialogTitle" v-model="postDialogVisible" width="720px">
      <el-table :data="postList" stripe style="width: 100%;">
        <el-table-column prop="id" label="文章ID" width="140" />
        <el-table-column prop="title" label="标题" min-width="380" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="primary" link @click="goEditPost(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { addCategory, deleteCategory, getCategoryList, getCategoryPosts, updateCategory } from '@/api'

export default {
  data() {
    return {
      keyword: '',
      tableData: [],
      dialogVisible: false,
      postDialogVisible: false,
      postDialogTitle: '分类下文章',
      postList: [],
      isEdit: false,
      form: {
        id: null,
        name: '',
        description: '',
        sort: 0
      }
    }
  },
  mounted() {
    this.fetchList()
  },
  methods: {
    async fetchList() {
      try {
        const data = await getCategoryList({ keyword: this.keyword || undefined })
        this.tableData = data || []
      } catch (error) {
        console.error('获取分类列表失败:', error)
        this.$message.error('获取分类列表失败')
      }
    },
    openAddDialog() {
      this.isEdit = false
      this.form = { id: null, name: '', description: '', sort: 0 }
      this.dialogVisible = true
    },
    openEditDialog(row) {
      this.isEdit = true
      this.form = {
        id: row.id,
        name: row.name || '',
        description: row.description || '',
        sort: row.sort ?? 0
      }
      this.dialogVisible = true
    },
    async submitForm() {
      if (!this.form.name || !this.form.name.trim()) {
        this.$message.warning('请输入分类名称')
        return
      }
      const payload = {
        id: this.form.id,
        name: this.form.name.trim(),
        description: this.form.description,
        sort: this.form.sort
      }
      try {
        if (this.isEdit) {
          await updateCategory(payload)
          this.$message.success('分类更新成功')
        } else {
          await addCategory(payload)
          this.$message.success('分类新增成功')
        }
        this.dialogVisible = false
        this.fetchList()
      } catch (error) {
        console.error('保存分类失败:', error)
        this.$message.error('保存分类失败')
      }
    },
    removeCategory(row) {
      this.$confirm(`确定删除分类 "${row.name}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(async () => {
          await deleteCategory({ id: row.id })
          this.$message.success('删除成功')
          this.fetchList()
        })
        .catch(() => {})
    },
    async showPosts(row) {
      try {
        const data = await getCategoryPosts({ id: row.id })
        this.postList = data || []
        this.postDialogTitle = `分类「${row.name}」下的文章（${this.postList.length}）`
        this.postDialogVisible = true
      } catch (error) {
        console.error('获取分类文章失败:', error)
        this.$message.error('获取分类文章失败')
      }
    },
    goEditPost(row) {
      this.$router.push(`/edit-article/${row.id}`)
    }
  }
}
</script>

<style scoped>
.category-page {
  padding: 16px;
}
</style>