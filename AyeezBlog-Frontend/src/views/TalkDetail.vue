<template>
  <div class="talk-detail">
    <div class="inner">
      <div class="card">
        <div v-if="loading" class="state">加载中…</div>
        <div v-else-if="error" class="state state--error">加载失败：{{ error }}</div>

        <template v-else>
          <div class="moment">
            <div class="author-row">
              <img class="avatar" :src="authorAvatar" alt="" />
              <div class="nick">{{ authorName }}</div>
            </div>

            <div class="moment-main">
              <div class="content">{{ talk.content || '' }}</div>
            </div>

            <div v-if="images.length" class="images" :class="imageLayoutClass(images.length)">
              <img
                v-for="(url, idx) in images"
                :key="url + idx"
                :src="url"
                class="img"
                alt=""
                loading="lazy"
                @click.stop="openPreview(url)"
              />
            </div>

            <div class="card-foot">
              <div class="meta">
                <span class="time">{{ formatTime(talk.createdAt) }}</span>
                <span class="id">#{{ talk.id }}</span>
              </div>
              <button class="back-btn" type="button" @click="goBack">返回</button>
            </div>
          </div>
        </template>
      </div>

      <div class="card">
        <div class="card-title">评论</div>
        <div id="tcomment-talk" ref="twikooTalk"></div>
      </div>
    </div>

    <div v-if="previewOpen" class="img-preview" @click.self="closePreview" role="dialog" aria-modal="true">
      <button class="preview-close" type="button" @click="closePreview" aria-label="关闭">×</button>
      <img class="preview-img" :src="previewUrl" alt="" />
    </div>
  </div>
</template>

<script>
import { fetchTalkById } from '@/api'
import { loadTwikoo, getTwikooEnvId } from '@/utils/twikoo'

export default {
  name: 'TalkDetail',
  props: ['id'],
  data() {
    return {
      authorName: '阿叶Ayeez',
      authorAvatar: 'https://qiniu.ayeez.cn/avatar.jpg',
      talk: {},
      loading: false,
      error: '',
      images: [],
      previewOpen: false,
      previewUrl: ''
    }
  },
  async created() {
    await this.load()
  },
  async mounted() {
    await this.$nextTick()
    await this.initTwikoo()
    window.addEventListener('keydown', this.onKeydown, { passive: true })
  },
  beforeDestroy() {
    window.removeEventListener('keydown', this.onKeydown)
  },
  methods: {
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
      return d.toLocaleString('zh-CN', { hour12: false })
    },
    async load() {
      this.loading = true
      this.error = ''
      try {
        const resp = await fetchTalkById(this.id)
        const talk = resp?.data || resp
        this.talk = talk || {}
        this.images = this.parseImages(this.talk.images)
      } catch (e) {
        const msg = e?.message ? String(e.message) : String(e)
        this.error = msg
      } finally {
        this.loading = false
      }
    },
    async initTwikoo() {
      const el = this.$refs.twikooTalk
      if (!el) return
      try {
        const tw = await loadTwikoo()
        await Promise.resolve(
          tw.init({
            envId: getTwikooEnvId(),
            el,
            path: `/talks/${this.id}`
          })
        )
      } catch (e) {
        console.error('说说页 Twikoo 初始化失败', e)
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
      document.body.style.overflow = 'hidden'
    },
    closePreview() {
      this.previewOpen = false
      this.previewUrl = ''
      document.body.style.overflow = ''
    },
    goBack() {
      if (window.history.length > 1) this.$router.back()
      else this.$router.push('/talks')
    }
  }
}
</script>

<style scoped>
.talk-detail {
  width: 100%;
  box-sizing: border-box;
  color: white;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  min-height: calc(100vh - 68px);
  padding: 20px;
}

.inner {
  width: 100%;
  max-width: 900px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin: 0 auto;
  align-items: center;
}

.card {
  border-radius: 12px;
  padding: 18px;
  background: rgba(0, 0, 0, 0.82);
  border: 1px solid rgba(255, 255, 255, 0.28);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.35);
  width: 100%;
  max-width: 760px;
}

.meta {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  white-space: nowrap;
}

.back-btn {
  height: 32px;
  padding: 0 12px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
  cursor: pointer;
}

.back-btn:hover {
  border: 1px solid rgba(0, 184, 40, 0.85);
}

.state {
  color: rgba(255, 255, 255, 0.82);
}

.state--error {
  color: #ffb4b4;
}

.moment {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.author-row {
  display: flex;
  align-items: center;
  gap: 10px;
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
  text-align: left;
}

.nick {
  font-weight: 700;
  color: #7af58f;
  font-size: 14px;
}

.content {
  margin-top: 6px;
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
  color: rgba(255, 255, 255, 0.92);
  text-align: left;
  padding: 0;
}

.card-foot {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}

.images {
  margin-top: 4px;
  display: grid;
  gap: 6px;
  width: 100%;
  max-width: 100%;
}

.images--grid-2 {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.images--grid-3 {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.images--single {
  grid-template-columns: 1fr;
}

.img {
  width: 100%;
  aspect-ratio: 1 / 1;
  height: auto;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  cursor: zoom-in;
}

/* 单图：做成更像朋友圈的大横图，横向占满正文区域的大半甚至全部 */
.images--single .img {
  aspect-ratio: 16 / 10;
  max-height: 520px;
}

.card-title {
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 700;
}

#tcomment-talk {
  color: #ffffff;
  min-height: 120px;
}

@media (max-width: 768px) {
  .talk-detail {
    padding: 10px 0;
  }
  .inner {
    max-width: none;
    gap: 8px;
  }
  .card {
    padding: 12px 10px;
    border: none;
    border-radius: 0;
    background: transparent;
    box-shadow: none;
    max-width: none;
  }
  .avatar {
    width: 34px;
    height: 34px;
    border-radius: 9px;
  }
  .nick {
    font-size: 13px;
  }
  .content {
    font-size: 14px;
    line-height: 1.75;
  }
  .images {
    gap: 5px;
  }
  .images--grid-2 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .images--grid-3 {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
  .img {
    border-radius: 8px;
  }
  .images--single .img {
    aspect-ratio: 4 / 3;
    max-height: 560px;
  }
  .card-foot {
    justify-content: flex-end;
  }
}

@media (max-width: 420px) {
  .images {
    gap: 5px;
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

