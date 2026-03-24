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

    <div class="tree-wrap">
      <div class="tree-header">
        <h3>分类目录视图</h3>
        <el-button size="small" @click="refreshTree">刷新目录</el-button>
      </div>
      <el-empty v-if="!categoryTree.length" description="暂无可展示的分类目录" />
      <el-tree
        v-else
        :data="categoryTree"
        node-key="key"
        default-expand-all
        :expand-on-click-node="false"
      >
        <template #default="{ data }">
          <div class="tree-node">
            <div class="tree-node-left">
              <el-icon v-if="data.type === 'category'" class="tree-node-icon"><FolderOpened /></el-icon>
              <el-icon v-else class="tree-node-icon"><Document /></el-icon>
              <span>{{ data.label }}</span>
            </div>
            <div class="tree-node-actions" v-if="data.type === 'category'">
              <el-button type="primary" link @click.stop="openEditDialog(data.raw)">编辑分类</el-button>
              <el-button type="primary" link @click.stop="showPosts(data.raw)">文章列表</el-button>
            </div>
            <div class="tree-node-actions" v-else>
              <el-button type="primary" link @click.stop="goEditPost(data.raw)">编辑文章</el-button>
            </div>
          </div>
        </template>
      </el-tree>
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
import { Document, FolderOpened } from '@element-plus/icons-vue'

export default {
  components: {
    FolderOpened,
    Document
  },
  data() {
    return {
      keyword: '',
      tableData: [],
      categoryTree: [],
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
        await this.buildTreeData()
      } catch (error) {
        console.error('获取分类列表失败:', error)
        this.$message.error('获取分类列表失败')
      }
    },
    async refreshTree() {
      await this.buildTreeData()
      this.$message.success('目录已刷新')
    },
    async buildTreeData() {
      if (!this.tableData.length) {
        this.categoryTree = []
        return
      }
      const postMap = {}
      const postTasks = this.tableData.map(async (item) => {
        try {
          const posts = await getCategoryPosts({ id: item.id })
          postMap[item.id] = posts || []
        } catch (error) {
          postMap[item.id] = []
          console.error(`获取分类 ${item.id} 的文章失败:`, error)
        }
      })
      await Promise.all(postTasks)
      this.categoryTree = this.composeCategoryTree(this.tableData, postMap)
    },
    composeCategoryTree(categories, postMap) {
      const nodeMap = new Map()
      categories.forEach((item) => {
        const posts = postMap[item.id] || []
        nodeMap.set(item.id, {
          key: `category-${item.id}`,
          type: 'category',
          label: `${item.name} (${posts.length})`,
          raw: item,
          children: posts.map((post) => ({
            key: `post-${item.id}-${post.id}`,
            type: 'post',
            label: post.title || `文章 ${post.id}`,
            raw: post
          }))
        })
      })
      const roots = []
      categories.forEach((item) => {
        const node = nodeMap.get(item.id)
        const parentId = item.parentId
        if (parentId && nodeMap.has(parentId)) {
          nodeMap.get(parentId).children.unshift(node)
        } else {
          roots.push(node)
        }
      })
      return roots
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

.tree-wrap {
  margin-top: 18px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  background: #fff;
}

.tree-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.tree-header h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
}

.tree-node {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.tree-node-left {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.tree-node-icon {
  color: #909399;
}

.tree-node-actions {
  white-space: nowrap;
}
</style>