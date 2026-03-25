<template>
  <div class="logs-manage-page page-card">
    <div class="page-toolbar">
      <el-input
        v-model="keyword"
        placeholder="请输入版本号（如 v1.3.0）"
        clearable
        class="toolbar-input"
      />
      <el-button type="primary" @click="fetchList">搜索</el-button>
      <el-button type="success" @click="openAddDialog">新增版本</el-button>
      <el-button type="info" @click="goToContentView">查看日志内容</el-button>
    </div>

    <div class="current-info">
      <span class="current-label">当前生效版本：</span>
      <el-tag v-if="currentVersion" type="success">
        {{ currentVersion.version }} ({{ currentVersion.date }})
      </el-tag>
      <el-tag v-else type="info">暂无</el-tag>
    </div>

    <div class="table-wrap">
      <el-table :data="tableData" stripe style="width: 100%;">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="date" label="日期" width="140" />
        <el-table-column prop="version" label="版本号" min-width="180" />

        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.current" type="success">当前</el-tag>
            <el-tag v-else type="info">-</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="280">
          <template #default="scope">
            <div class="row-actions">
              <el-button
                size="small"
                type="primary"
                v-if="!scope.row.current"
                @click="setCurrent(scope.row)"
              >
                设为当前
              </el-button>
              <el-button size="small" type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="removeLogVersion(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog :title="isEdit ? '编辑日志版本' : '新增日志版本'" v-model="dialogVisible" width="760px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="版本号">
          <el-input v-model="form.version" placeholder="例如 v1.3.0" />
        </el-form-item>

        <el-form-item label="版本日期">
          <el-date-picker
            v-model="form.date"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="变更内容">
          <el-input
            v-model="form.changesText"
            placeholder="一行一个变更内容"
            type="textarea"
            :rows="6"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {
  addLogVersion,
  deleteLogVersion,
  getLogList,
  setCurrentLogVersion,
  updateLogVersion
} from '@/api'

export default {
  data() {
    return {
      keyword: '',
      tableData: [],
      currentVersion: null,

      dialogVisible: false,
      isEdit: false,
      form: {
        id: null,
        version: '',
        date: '',
        changesText: ''
      }
    }
  },
  mounted() {
    this.fetchList()
  },
  methods: {
    async fetchList() {
      try {
        const data = await getLogList({ keyword: this.keyword || undefined })
        this.tableData = Array.isArray(data) ? data : []
        this.currentVersion = this.tableData.find((i) => i.current) || null
      } catch (error) {
        console.error('获取日志列表失败:', error)
        this.$message.error('获取日志列表失败')
      }
    },
    openAddDialog() {
      this.isEdit = false
      this.form = { id: null, version: '', date: '', changesText: '' }
      this.dialogVisible = true
    },
    openEditDialog(row) {
      this.isEdit = true
      this.form = {
        id: row.id,
        version: row.version || '',
        date: row.date || '',
        changesText: Array.isArray(row.changes) ? row.changes.join('\n') : ''
      }
      this.dialogVisible = true
    },
    normalizeChanges() {
      return (this.form.changesText || '')
        .split('\n')
        .map((s) => s.trim())
        .filter((s) => s)
    },
    async submitForm() {
      if (!this.form.version || !this.form.version.trim()) {
        this.$message.warning('请输入版本号')
        return
      }
      if (!this.form.date) {
        this.$message.warning('请选择版本日期')
        return
      }
      const changes = this.normalizeChanges()
      if (!changes.length) {
        this.$message.warning('请至少填写一个变更内容')
        return
      }

      const payload = {
        id: this.form.id,
        version: this.form.version.trim(),
        date: this.form.date,
        changes
      }

      try {
        if (this.isEdit) {
          await updateLogVersion(payload)
          this.$message.success('日志版本更新成功')
        } else {
          await addLogVersion(payload)
          this.$message.success('日志版本新增成功')
        }
        this.dialogVisible = false
        await this.fetchList()
      } catch (error) {
        console.error('保存日志版本失败:', error)
        this.$message.error('保存日志版本失败')
      }
    },
    async setCurrent(row) {
      try {
        await setCurrentLogVersion({ id: row.id })
        this.$message.success('已切换当前版本')
        await this.fetchList()
      } catch (error) {
        console.error('切换当前版本失败:', error)
        this.$message.error('切换当前版本失败')
      }
    },
    removeLogVersion(row) {
      this.$confirm(`确定删除版本 "${row.version}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(async () => {
          await deleteLogVersion({ id: row.id })
          this.$message.success('删除成功')
          await this.fetchList()
        })
        .catch(() => {})
    },
    goToContentView() {
      this.$router.push('/logs/content')
    }
  }
}
</script>

<style scoped>
.logs-manage-page {
  padding: 16px;
}

.current-info {
  margin: 12px 0;
  padding: 10px 12px;
  background: #ffffff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.current-label {
  font-weight: 600;
  margin-right: 8px;
}

.row-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.table-wrap {
  margin-top: 12px;
}
</style>

