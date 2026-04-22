<template>
  <div class="talks">
    <div class="talks-inner">
      <div class="talks-header">
        <div class="talks-tagline">this is</div>
        <div class="talks-title-gradient">moment</div>
        <p class="talks-subtitle">一些碎碎念，谁拉这了</p>
      </div>

      <div class="talks-layout">
        <main class="talks-main">
          <div v-if="loading" class="state">加载中…</div>
          <div v-else-if="error" class="state state--error">加载失败：{{ error }}</div>
          <div v-else-if="!items.length" class="state">还没有说说。</div>

          <div v-else class="talk-list">
            <article v-for="t in items" :key="t.id" class="talk-card" @click="goDetail(t.id)">
              <div class="moment">
                <img class="avatar" :src="authorAvatar" alt="" />
                <div class="moment-main">
                  <div class="moment-head">
                    <div class="nick">{{ authorName }}</div>
                    <div class="meta">
                      <span class="time">{{ formatTime(t.createdAt) }}</span>
                      <span class="id">#{{ t.id }}</span>
                    </div>
                  </div>

                  <div class="content">{{ t.content || '' }}</div>

                  <div
                    v-if="parseImages(t.images).length"
                    class="images"
                    :class="imageLayoutClass(parseImages(t.images).length)"
                  >
                    <img
                      v-for="(url, idx) in parseImages(t.images).slice(0, 9)"
                      :key="url + idx"
                      :src="url"
                      alt=""
                      class="img"
                      loading="lazy"
                      @click.stop="openPreview(url)"
                    />
                  </div>
                </div>
              </div>
            </article>
          </div>
        </main>

        <aside class="talks-side">
          <div class="side-mobile-head">
            <button class="side-toggle-btn" type="button" @click="toggleSidebarCompact">
              {{ sidebarCompact ? '展开状态栏（待办）' : '缩略状态栏（待办）' }}
            </button>
          </div>

          <template v-if="!sidebarCompact">
          <section class="side-card">
            <div class="side-title">我的状态</div>
            <div class="status-row">
              <span class="dot" :class="statusDotClass"></span>
              <span class="status-text">{{ myStatus }}</span>
            </div>
            <div class="side-kv">
              <div class="kv">
                <div class="k">心情</div>
                <div class="v">{{ myMood }}</div>
              </div>
              <div class="kv">
                <div class="k">在做</div>
                <div class="v">{{ myDoing }}</div>
              </div>
            </div>
          </section>

          <section class="side-card">
            <div class="side-title">待办（{{ todos.length }}）</div>
            <div v-if="!todos.length" class="side-muted">暂无待办</div>
            <ul v-else class="todo-list">
              <li v-for="(t, i) in todos" :key="i" class="todo-item">
                <span class="todo-text">{{ t }}</span>
              </li>
            </ul>
          </section>

          <section class="side-card">
            <div class="side-title">碎碎念</div>
            <ul class="side-list">
              <li v-for="(x, i) in sideNotes" :key="i">{{ x }}</li>
            </ul>
          </section>
          </template>
        </aside>
      </div>

      <div v-if="previewOpen" class="img-preview" @click.self="closePreview" role="dialog" aria-modal="true">
        <button class="preview-close" type="button" @click="closePreview" aria-label="关闭">×</button>
        <img class="preview-img" :src="previewUrl" alt="" />
      </div>

      <div v-if="!loading && total > pageSize" class="pager">
        <button class="pager-btn" :disabled="page <= 1" @click="prevPage">上一页</button>
        <span class="pager-text">{{ page }} / {{ totalPages }}</span>
        <button class="pager-btn" :disabled="page >= totalPages" @click="nextPage">下一页</button>
      </div>
    </div>
  </div>
</template>

<script>
import { fetchTalks, fetchTalkSidebar } from '@/api'

export default {
  name: 'Talks',
  data() {
    return {
      authorName: '阿叶Ayeez',
      authorAvatar: 'https://qiniu.ayeez.cn/avatar.jpg',
      myStatus: '',
      myMood: '',
      myDoing: '',
      sideNotes: [],
      todos: [],
      loading: false,
      error: '',
      items: [],
      total: 0,
      page: 1,
      pageSize: 10,
      previewOpen: false,
      previewUrl: '',
      sidebarLoading: false,
      isMobile: false,
      sidebarCompact: false
    }
  },
  computed: {
    totalPages() {
      return Math.max(1, Math.ceil((this.total || 0) / this.pageSize))
    },
    statusDotClass() {
      const s = String(this.myStatus || '').toLowerCase()
      if (s.includes('离线') || s.includes('offline')) return 'dot--offline'
      if (s.includes('忙') || s.includes('busy')) return 'dot--busy'
      return 'dot--online'
    }
  },
  mounted() {
    this.load()
    this.loadSidebar()
    this.handleResize()
    window.addEventListener('keydown', this.onKeydown, { passive: true })
    window.addEventListener('resize', this.handleResize, { passive: true })
  },
  beforeDestroy() {
    window.removeEventListener('keydown', this.onKeydown)
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    handleResize() {
      const mobile = window.innerWidth <= 980
      const changed = mobile !== this.isMobile
      this.isMobile = mobile
      // 移动端默认缩略，桌面端默认展开
      if (changed) {
        this.sidebarCompact = mobile
      }
    },
    toggleSidebarCompact() {
      if (!this.isMobile) return
      this.sidebarCompact = !this.sidebarCompact
    },
    parseJsonArray(jsonStr) {
      if (!jsonStr) return []
      try {
        const arr = JSON.parse(jsonStr)
        if (!Array.isArray(arr)) return []
        return arr.map((x) => String(x || '').trim()).filter(Boolean)
      } catch (_) {
        return []
      }
    },
    parseImages(images) {
      if (!images) return []
      try {
        const arr = JSON.parse(images)
        if (!Array.isArray(arr)) return []
        return arr.map((x) => String(x || '').trim()).filter(Boolean)
      } catch (_) {
        return []
      }
    },
    imageLayoutClass(count) {
      if (!count) return ''
      if (count === 1) return 'images--single'
      if (count <= 4) return 'images--grid-2'
      return 'images--grid-3'
    },
    formatTime(v) {
      if (!v) return ''
      const d = new Date(v)
      if (Number.isNaN(d.getTime())) return String(v)
      // 朋友圈风格：更紧凑一些
      return d.toLocaleString('zh-CN', { hour12: false })
    },
    async load() {
      this.loading = true
      this.error = ''
      try {
        const resp = await fetchTalks(this.page, this.pageSize, 'created_at', 'desc')
        const rows = resp?.data?.rows || resp?.rows || []
        const total = resp?.data?.total ?? resp?.total ?? 0
        this.items = Array.isArray(rows) ? rows : []
        this.total = Number(total) || 0
      } catch (e) {
        const msg = e?.message ? String(e.message) : String(e)
        this.error = msg
      } finally {
        this.loading = false
      }
    },
    applySidebarFallback() {
      if (!this.myStatus) this.myStatus = '在线'
      if (!this.myMood) this.myMood = '平静'
      if (!this.myDoing) this.myDoing = '写代码 / 写文章'
      if (!Array.isArray(this.sideNotes) || !this.sideNotes.length) {
        this.sideNotes = ['随手记录：想到什么就发什么', '今天也要保持好心情', '欢迎来评论区打个招呼']
      }
      if (!Array.isArray(this.todos) || !this.todos.length) {
        this.todos = ['把今天的想法记在说说', '整理一篇博客草稿', '保持运动/早睡']
      }
    },
    async loadSidebar() {
      this.sidebarLoading = true
      try {
        const resp = await fetchTalkSidebar()
        const d = resp?.data || {}
        this.myStatus = String(d.status || '').trim()
        this.myMood = String(d.mood || '').trim()
        this.myDoing = String(d.doing || '').trim()
        this.todos = this.parseJsonArray(d.todos)
        this.sideNotes = this.parseJsonArray(d.notes)
        this.applySidebarFallback()
      } catch (_) {
        this.applySidebarFallback()
      } finally {
        this.sidebarLoading = false
      }
    },
    onKeydown(e) {
      if (!this.previewOpen) return
      if (e && (e.key === 'Escape' || e.key === 'Esc')) {
        this.closePreview()
      }
    },
    openPreview(url) {
      const u = String(url || '').trim()
      if (!u) return
      this.previewUrl = u
      this.previewOpen = true
      // 禁止背景滚动
      document.body.style.overflow = 'hidden'
    },
    closePreview() {
      this.previewOpen = false
      this.previewUrl = ''
      document.body.style.overflow = ''
    },
    goDetail(id) {
      this.$router.push(`/talks/${id}`)
    },
    async prevPage() {
      if (this.page <= 1) return
      this.page -= 1
      await this.load()
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },
    async nextPage() {
      if (this.page >= this.totalPages) return
      this.page += 1
      await this.load()
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
  }
}
</script>

<style scoped>
.talks {
  width: 100%;
  box-sizing: border-box;
  color: white;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  min-height: calc(100vh - 68px);
  padding: 20px;
}

.talks-inner {
  width: 100%;
  max-width: 1120px;
}

.talks-header {
  font-family: 'Bebas Neue', Arial, sans-serif;
  margin-bottom: 14px;
}

.talks-tagline {
  font-size: 20px;
  letter-spacing: 4px;
  color: #cccccc;
}

.talks-title-gradient {
  font-size: 46px;
  letter-spacing: 6px;
  background-image: linear-gradient(to right, #abe6a8, #00b828);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.talks-subtitle {
  margin: 10px 0 0;
  color: #cccccc;
  font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial, sans-serif;
  letter-spacing: 0;
  font-size: 14px;
}

.state {
  margin-top: 18px;
  color: rgba(255, 255, 255, 0.82);
}

.state--error {
  color: #ffb4b4;
}

.talks-layout {
  margin-top: 10px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 14px;
  align-items: start;
}

.talks-main {
  min-width: 0;
}

.talks-side {
  /* 和说说卡片一样：正常在页面流里滚动 */
  position: static;
  width: 100%;
  align-self: start;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: none;
  overflow: visible;
}

.side-mobile-head {
  display: none;
}

.side-toggle-btn {
  height: 32px;
  padding: 0 12px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 12px;
  cursor: pointer;
}

.side-card {
  background: rgba(0, 0, 0, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.35);
  border-radius: 12px;
  padding: 14px 14px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
}

.side-title {
  font-size: 14px;
  font-weight: 800;
  color: #fff;
  margin-bottom: 10px;
}

.status-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.25);
}

.dot--online {
  background: rgba(34, 197, 94, 0.95);
}

.dot--busy {
  background: rgba(245, 158, 11, 0.95);
}

.dot--offline {
  background: rgba(148, 163, 184, 0.95);
}

.status-text {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.88);
}

.side-kv {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}

.kv .k {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.65);
}

.kv .v {
  margin-top: 3px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.92);
  word-break: break-word;
}

.side-list {
  margin: 0;
  padding-left: 16px;
  color: rgba(255, 255, 255, 0.86);
  font-size: 13px;
  line-height: 1.65;
}

.side-list li + li {
  margin-top: 6px;
}

.side-muted {
  color: rgba(255, 255, 255, 0.65);
  font-size: 13px;
}

.todo-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.todo-item {
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: rgba(255, 255, 255, 0.04);
}

.todo-text {
  font-size: 13px;
  line-height: 1.5;
  color: rgba(255, 255, 255, 0.9);
  word-break: break-word;
}

.talk-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.talk-card {
  background: rgba(0, 0, 0, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.35);
  border-radius: 12px;
  padding: 14px 14px;
  cursor: pointer;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  transition: transform 0.15s ease, border 0.15s ease, box-shadow 0.15s ease;
}

.talk-card:hover {
  transform: translateY(-1px);
  border: 1px solid rgba(0, 184, 40, 0.75);
  box-shadow: 0 10px 22px rgba(0, 184, 40, 0.14);
}

.moment {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  object-fit: cover;
  border: 1px solid rgba(255, 255, 255, 0.22);
  flex: 0 0 auto;
}

.moment-main {
  flex: 1;
  min-width: 0;
  text-align: left;
}

.moment-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
}

.nick {
  font-weight: 700;
  color: #7af58f;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 60%;
}

.meta {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.65);
  white-space: nowrap;
  flex: 0 0 auto;
}

.content {
  margin-top: 6px;
  font-size: 14px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.92);
  white-space: pre-wrap;
  word-break: break-word;
  text-align: left;
  padding: 0;
}

.images {
  margin-top: 10px;
  display: grid;
  gap: 6px;
  width: fit-content;
  max-width: 100%;
  /* 电脑端缩略图更小：大概之前的 1/3 观感 */
  --cell: 112px;
}

.images--grid-2 {
  grid-template-columns: repeat(2, var(--cell));
}

.images--grid-3 {
  grid-template-columns: repeat(3, var(--cell));
}

.images--single {
  grid-template-columns: minmax(0, 260px);
}

.img {
  width: var(--cell);
  height: var(--cell);
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  cursor: zoom-in;
}

.pager {
  margin-top: 18px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 14px;
}

.pager-btn {
  height: 34px;
  padding: 0 14px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
  cursor: pointer;
}

.pager-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pager-text {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.75);
}

@media (max-width: 520px) {
  .talks-title-gradient {
    font-size: 34px;
    letter-spacing: 4px;
  }

  /* 手机端：更贴近朋友圈的紧凑布局 */
  .talks {
    padding: 12px 0;
  }

  .talk-card {
    padding: 10px 0;
    border-radius: 0;
    border: none;
    background: transparent;
    box-shadow: none;
  }

  .avatar {
    width: 34px;
    height: 34px;
    border-radius: 8px;
  }

  .nick {
    max-width: 50%;
  }

  .images {
    gap: 5px;
    width: 100%;
    max-width: 100%;
    --cell: auto;
  }

  .images--grid-2 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    width: 100%;
  }

  .images--grid-3 {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    width: 100%;
  }

  .images--single {
    grid-template-columns: minmax(0, min(100%, 280px));
  }

  .img {
    width: 100%;
    height: auto;
    aspect-ratio: 1 / 1;
  }
}

@media (max-width: 980px) {
  .talks {
    padding-left: 0;
    padding-right: 0;
  }

  .talks-inner {
    max-width: none;
  }

  .talks-layout {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .talks-side {
    position: static;
    top: auto;
    left: auto;
    order: -1;
    max-height: none;
    overflow: visible;
    gap: 8px;
  }

  .talk-card {
    border: none;
    background: transparent;
    box-shadow: none;
    position: relative;
  }

  .talk-list .talk-card::after {
    content: '';
    position: absolute;
    left: 8px;
    right: 8px;
    bottom: 0;
    height: 1px;
    background: rgba(255, 255, 255, 0.24);
  }

  .talk-list .talk-card:last-child::after {
    display: none;
  }

  .talk-card:hover {
    transform: none;
    border: none;
    box-shadow: none;
  }

  .side-mobile-head {
    display: block;
  }

  .side-card {
    padding: 10px 10px;
    border-radius: 10px;
  }

  .side-title {
    margin-bottom: 8px;
  }
}

.img-preview {
  position: fixed;
  inset: 0;
  z-index: 5000;
  background: rgba(0, 0, 0, 0.86);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 18px;
  box-sizing: border-box;
}

.preview-img {
  max-width: min(1100px, 96vw);
  max-height: 92vh;
  object-fit: contain;
  border-radius: 12px;
  box-shadow: 0 18px 50px rgba(0, 0, 0, 0.45);
}

.preview-close {
  position: fixed;
  top: 14px;
  right: 14px;
  z-index: 5001;
  width: 40px;
  height: 40px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
}
</style>

