<template>
  <div class="about-anime-page page-card">
    <div class="page-toolbar">
      <el-button native-type="button" @click="goCardsView">卡片视图排序</el-button>
      <el-button type="primary" native-type="button" @click="onRefreshClick">刷新</el-button>
      <el-button type="success" native-type="button" @click="openAddDialog">新增追番项</el-button>
      <el-button
        type="warning"
        native-type="button"
        :disabled="!sortDirty || savingOrder"
        :loading="savingOrder"
        @click="saveOrder"
      >
        保存排序
      </el-button>
      <span class="toolbar-hint">
        上移/下移只改本页顺序，不会请求接口；完成后请点「保存排序」写入数据库（单次请求 + 单条批量 SQL）
      </span>
    </div>

    <div class="table-wrap">
      <el-table v-loading="loading" :data="list" row-key="id" stripe style="width: 100%;">
        <el-table-column label="预览" width="100">
          <template #default="scope">
            <el-image
              v-if="scope.row.imageUrl"
              :src="scope.row.imageUrl"
              fit="cover"
              style="width: 56px; height: 78px; border-radius: 6px;"
              :preview-src-list="[scope.row.imageUrl]"
              preview-teleported
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="顺序" width="72">
          <template #default="scope">{{ scope.$index }}</template>
        </el-table-column>
        <el-table-column prop="imageUrl" label="图片 URL" min-width="220" show-overflow-tooltip />
        <el-table-column prop="title" label="标题" width="140" show-overflow-tooltip />
        <el-table-column prop="linkUrl" label="跳转链接" min-width="160" show-overflow-tooltip />
        <el-table-column label="排序" width="200">
          <template #default="scope">
            <el-button
              native-type="button"
              size="small"
              :disabled="scope.$index === 0"
              @click.stop.prevent="moveUp(scope.$index)"
            >
              上移
            </el-button>
            <el-button
              native-type="button"
              size="small"
              :disabled="scope.$index === list.length - 1"
              @click.stop.prevent="moveDown(scope.$index)"
            >
              下移
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <div class="row-actions">
              <el-button native-type="button" size="small" type="primary" @click.stop="openEditDialog(scope.row)">
                编辑
              </el-button>
              <el-button native-type="button" size="small" type="danger" @click.stop="removeRow(scope.row)">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog :title="isEdit ? '编辑追番项' : '新增追番项'" v-model="dialogVisible" width="560px">
      <el-form :model="form" label-width="96px" @submit.prevent>
        <el-form-item label="图片 URL" required>
          <el-input v-model="form.imageUrl" type="textarea" :rows="2" placeholder="完整图片地址，如七牛外链" />
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="可选，用于悬停提示" />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="form.linkUrl" placeholder="可选，有则点击新标签页打开" />
        </el-form-item>
        <el-form-item v-if="isEdit" label="展示顺序">
          <span class="form-readonly-hint">请在表格中使用上移/下移，然后点击工具栏「保存排序」写入数据库</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button native-type="button" @click="dialogVisible = false">取消</el-button>
        <el-button native-type="button" type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {
  addAboutAnime,
  deleteAboutAnime,
  getAboutAnimeList,
  reorderAboutAnime,
  updateAboutAnime
} from '@/api'

export default {
  name: 'AboutAnime',
  data() {
    return {
      loading: false,
      savingOrder: false,
      sortDirty: false,
      list: [],
      dialogVisible: false,
      isEdit: false,
      submitting: false,
      form: {
        id: null,
        imageUrl: '',
        title: '',
        linkUrl: ''
      }
    }
  },
  mounted() {
    this.fetchList()
  },
  methods: {
    goCardsView() {
      this.$router.push('/about-anime/cards')
    },
    async fetchList() {
      this.loading = true
      try {
        this.list = await getAboutAnimeList()
        this.sortDirty = false
      } catch (e) {
        console.error(e)
        this.$message.error('加载追番列表失败')
      } finally {
        this.loading = false
      }
    },
    onRefreshClick() {
      if (this.sortDirty) {
        this.$confirm('当前有未保存的排序调整，刷新将丢失，是否继续？', '提示', {
          type: 'warning'
        })
          .then(() => this.fetchList())
          .catch(() => {})
      } else {
        this.fetchList()
      }
    },
    async saveOrder() {
      if (!this.sortDirty || !this.list.length) return
      this.savingOrder = true
      try {
        const ids = this.list.map((x) => x.id)
        await reorderAboutAnime({ ids })
        this.sortDirty = false
        this.$message.success('排序已保存')
        // 本地顺序已与库一致，不再请求 list，避免整表重载像「每次操作都在刷新」
      } catch (e) {
        console.error(e)
        this.$message.error(e.message || '保存排序失败')
        await this.fetchList()
      } finally {
        this.savingOrder = false
      }
    },
    openAddDialog() {
      this.isEdit = false
      this.form = {
        id: null,
        imageUrl: '',
        title: '',
        linkUrl: ''
      }
      this.dialogVisible = true
    },
    openEditDialog(row) {
      this.isEdit = true
      this.form = {
        id: row.id,
        imageUrl: row.imageUrl || '',
        title: row.title || '',
        linkUrl: row.linkUrl || ''
      }
      this.dialogVisible = true
    },
    async submitForm() {
      if (!this.form.imageUrl || !String(this.form.imageUrl).trim()) {
        this.$message.warning('请填写图片 URL')
        return
      }
      this.submitting = true
      try {
        if (this.isEdit) {
          await updateAboutAnime({
            id: this.form.id,
            imageUrl: this.form.imageUrl.trim(),
            title: this.form.title || null,
            linkUrl: this.form.linkUrl || null
          })
          this.$message.success('更新成功')
        } else {
          await addAboutAnime({
            imageUrl: this.form.imageUrl.trim(),
            title: this.form.title || null,
            linkUrl: this.form.linkUrl || null
          })
          this.$message.success('新增成功')
        }
        this.dialogVisible = false
        await this.fetchList()
      } catch (e) {
        console.error(e)
        this.$message.error(this.isEdit ? '更新失败' : '新增失败')
      } finally {
        this.submitting = false
      }
    },
    removeRow(row) {
      this.$confirm(`确定删除该追番项吗？`, '提示', {
        type: 'warning'
      })
        .then(async () => {
          await deleteAboutAnime({ id: row.id })
          this.$message.success('删除成功')
          await this.fetchList()
        })
        .catch(() => {})
    },
    moveUp(index) {
      if (index <= 0) return
      const copy = [...this.list]
      const t = copy[index - 1]
      copy[index - 1] = copy[index]
      copy[index] = t
      this.list = copy
      this.sortDirty = true
    },
    moveDown(index) {
      if (index >= this.list.length - 1) return
      const copy = [...this.list]
      const t = copy[index + 1]
      copy[index + 1] = copy[index]
      copy[index] = t
      this.list = copy
      this.sortDirty = true
    }
  }
}
</script>

<style scoped>
.about-anime-page {
  padding: 16px;
}

.toolbar-hint {
  margin-left: 12px;
  font-size: 13px;
  color: #6b7280;
}

.form-readonly-hint {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.5;
}
</style>
