<template>
  <div v-loading="loading" class="talk-sidebar page-card">
    <header class="page-head">
      <h1 class="page-title">说说侧边栏配置</h1>
      <div class="head-actions">
        <el-button @click="reload">刷新</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </div>
    </header>

    <section class="panel">
      <div class="form-grid">
        <div class="field">
          <div class="label">我的状态</div>
          <el-input v-model="form.status" placeholder="例如：在线 / 忙碌 / 离线" clearable />
        </div>
        <div class="field">
          <div class="label">心情</div>
          <el-input v-model="form.mood" placeholder="例如：平静 / 开心 / 低落" clearable />
        </div>
        <div class="field">
          <div class="label">在做</div>
          <el-input v-model="form.doing" placeholder="例如：写代码 / 看书" clearable />
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="label">待办（每行一条）</div>
      <el-input
        v-model="todosText"
        type="textarea"
        :rows="6"
        resize="none"
        placeholder="例如：\n把今天的想法记在说说\n整理一篇博客草稿\n保持运动/早睡"
      />
    </section>

    <section class="panel">
      <div class="label">碎碎念（每行一条）</div>
      <el-input
        v-model="notesText"
        type="textarea"
        :rows="6"
        resize="none"
        placeholder="例如：\n随手记录：想到什么就发什么\n欢迎来评论区打个招呼"
      />
    </section>

    <div v-if="error" class="error">{{ error }}</div>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'
import { getTalkSidebar, updateTalkSidebar } from '@/api'

export default {
  name: 'TalkSidebar',
  data() {
    return {
      loading: false,
      saving: false,
      error: '',
      form: {
        status: '',
        mood: '',
        doing: ''
      },
      todosText: '',
      notesText: ''
    }
  },
  mounted() {
    this.reload()
  },
  methods: {
    toLines(text) {
      return String(text || '')
        .split('\n')
        .map((x) => String(x || '').trim())
        .filter(Boolean)
    },
    parseJsonArrayToLinesText(jsonStr) {
      if (!jsonStr) return ''
      try {
        const arr = JSON.parse(jsonStr)
        if (!Array.isArray(arr)) return ''
        return arr.map((x) => String(x || '').trim()).filter(Boolean).join('\n')
      } catch (_) {
        // 如果历史数据不是 JSON，就按纯文本兜底
        return String(jsonStr || '').trim()
      }
    },
    async reload() {
      this.loading = true
      this.error = ''
      try {
        const resp = await getTalkSidebar()
        const d = resp?.data || {}
        this.form.status = d.status || ''
        this.form.mood = d.mood || ''
        this.form.doing = d.doing || ''
        this.todosText = this.parseJsonArrayToLinesText(d.todos)
        this.notesText = this.parseJsonArrayToLinesText(d.notes)
      } catch (e) {
        const msg = e?.message ? String(e.message) : String(e)
        this.error = msg
      } finally {
        this.loading = false
      }
    },
    async save() {
      this.saving = true
      this.error = ''
      try {
        const payload = {
          status: String(this.form.status || '').trim(),
          mood: String(this.form.mood || '').trim(),
          doing: String(this.form.doing || '').trim(),
          todos: JSON.stringify(this.toLines(this.todosText)),
          notes: JSON.stringify(this.toLines(this.notesText))
        }
        await updateTalkSidebar(payload)
        ElMessage.success('保存成功')
        await this.reload()
      } catch (e) {
        const msg = e?.message ? String(e.message) : String(e)
        this.error = msg
        ElMessage.error('保存失败')
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style scoped>
.talk-sidebar {
  min-height: 260px;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  color: #111827;
}

.head-actions {
  display: flex;
  gap: 10px;
}

.panel {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 14px;
  margin-bottom: 12px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.field .label,
.label {
  font-size: 12px;
  font-weight: 700;
  color: #374151;
  margin-bottom: 8px;
}

.error {
  margin-top: 8px;
  color: #b91c1c;
  font-size: 13px;
}

@media (max-width: 980px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>

