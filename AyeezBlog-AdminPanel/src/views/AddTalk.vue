<template>
  <div v-loading="loading" class="add-talk page-card">
    <header class="page-head">
      <h1 class="page-title">{{ isEdit ? '编辑说说' : '发布说说' }}</h1>
      <div class="head-actions">
        <el-checkbox v-model="draftChecked">存草稿</el-checkbox>
      </div>
    </header>

    <section class="panel panel--editor">
      <el-input
        v-model="form.content"
        type="textarea"
        :rows="6"
        maxlength="2000"
        show-word-limit
        resize="none"
        placeholder="这一刻想说点什么…"
      />
    </section>

    <section class="panel panel--images">
      <div class="uploader-row">
        <el-upload
          class="img-upload"
          :show-file-list="false"
          accept="image/*"
          multiple
          :http-request="uploadToQiniu"
          :disabled="uploading"
        >
          <el-button type="primary" plain :loading="uploading">
            {{ uploading ? '上传中…' : '上传图片' }}
          </el-button>
        </el-upload>
        <el-progress
          v-if="uploading"
          :percentage="uploadProgress"
          :stroke-width="10"
          status="success"
          class="upload-progress"
        />
      </div>

      <div v-if="images.length" class="img-grid">
        <div v-for="(url, idx) in images" :key="url + idx" class="img-item">
          <img :src="url" alt="" class="img" />
          <div class="img-overlay">
            <button class="ov-btn" type="button" @click.stop="move(idx, -1)" :disabled="idx === 0">↑</button>
            <button class="ov-btn" type="button" @click.stop="move(idx, 1)" :disabled="idx === images.length - 1">↓</button>
            <button class="ov-btn ov-btn--danger" type="button" @click.stop="remove(idx)">删</button>
          </div>
        </div>
      </div>
      <div v-else class="empty">上传后会以九宫格展示。</div>
    </section>

    <footer class="actions">
      <el-button size="large" @click="goBack">返回</el-button>
      <el-button type="primary" size="large" @click="submit">
        {{ isEdit ? '保存' : '发布' }}
      </el-button>
    </footer>
  </div>
</template>

<script>
import axios from 'axios'
import { addTalk, getTalkDetail, updateTalk, getQiniuUploadToken } from '@/api'

export default {
  name: 'AddTalk',
  data() {
    return {
      loading: false,
      uploading: false,
      uploadProgress: 0,
      form: {
        id: null,
        content: '',
        // 默认发布
        published: 1,
        images: ''
      },
      images: []
    }
  },
  computed: {
    isEdit() {
      return Boolean(this.$route.params && this.$route.params.id)
    },
    talkId() {
      return this.$route.params?.id || ''
    },
    draftChecked: {
      get() {
        return this.form.published !== 1
      },
      set(v) {
        this.form.published = v ? 0 : 1
      }
    }
  },
  async created() {
    if (this.isEdit) {
      await this.loadDetail()
    }
  },
  methods: {
    /**
     * 图片压缩（前端）：缩放到最大边 <= maxDim，转换为 JPEG（可大幅减小体积）
     * - 透明 PNG 会丢失透明（转 JPEG）；说说配图一般可接受
     * - 极小图/已很小的图会尽量少处理
     */
    async compressImageFile(file, { maxDim = 1600, quality = 0.82 } = {}) {
      if (!file) return file
      // 体积很小的图不处理（避免重复编码导致变糊）
      if (typeof file.size === 'number' && file.size > 0 && file.size < 220 * 1024) {
        return file
      }

      const readAsDataUrl = (f) =>
        new Promise((resolve, reject) => {
          const reader = new FileReader()
          reader.onload = () => resolve(reader.result)
          reader.onerror = reject
          reader.readAsDataURL(f)
        })

      const loadImg = (src) =>
        new Promise((resolve, reject) => {
          const img = new Image()
          img.onload = () => resolve(img)
          img.onerror = reject
          img.src = src
        })

      const dataUrl = await readAsDataUrl(file)
      const img = await loadImg(dataUrl)
      const w = img.naturalWidth || img.width
      const h = img.naturalHeight || img.height
      if (!w || !h) return file

      const maxSide = Math.max(w, h)
      const scale = maxSide > maxDim ? maxDim / maxSide : 1
      const targetW = Math.max(1, Math.round(w * scale))
      const targetH = Math.max(1, Math.round(h * scale))

      const canvas = document.createElement('canvas')
      canvas.width = targetW
      canvas.height = targetH
      const ctx = canvas.getContext('2d', { alpha: false })
      if (!ctx) return file

      // JPEG 没透明，先铺白底，避免 PNG 透明区域变黑
      ctx.fillStyle = '#ffffff'
      ctx.fillRect(0, 0, targetW, targetH)
      ctx.drawImage(img, 0, 0, targetW, targetH)

      const blob = await new Promise((resolve) => {
        // 强制 JPEG，体积更可控；若浏览器不支持则 fallback 到 PNG dataURL
        canvas.toBlob(
          (b) => resolve(b),
          'image/jpeg',
          quality
        )
      })
      if (!blob) return file

      // 如果压缩后反而更大，就保留原图
      if (typeof file.size === 'number' && blob.size >= file.size) {
        return file
      }

      const baseName = String(file.name || 'image').replace(/\.[^.]+$/, '')
      const nextName = `${baseName}.jpg`
      return new File([blob], nextName, { type: 'image/jpeg', lastModified: Date.now() })
    },
    normalizeUrl(url) {
      const raw = (url || '').trim()
      if (!raw) return ''
      if (/^https?:\/\//i.test(raw)) return raw
      if (/^\/\//.test(raw)) return `https:${raw}`
      return `https://${raw}`
    },
    async loadDetail() {
      this.loading = true
      try {
        const talk = await getTalkDetail({ id: this.talkId })
        this.form.id = talk.id
        this.form.content = talk.content || ''
        this.form.published = talk.published === 1 ? 1 : 0
        this.form.images = talk.images || ''
        this.images = this.safeParseImages(talk.images)
      } catch (e) {
        console.error('加载说说失败:', e)
        this.$message.error('加载说说失败')
        this.$router.push('/talk')
      } finally {
        this.loading = false
      }
    },
    safeParseImages(images) {
      if (!images) return []
      try {
        const arr = JSON.parse(images)
        return Array.isArray(arr) ? arr.map((x) => this.normalizeUrl(String(x))).filter(Boolean) : []
      } catch (_) {
        return []
      }
    },
    syncImagesToForm() {
      this.form.images = JSON.stringify(this.images || [])
    },
    move(idx, delta) {
      const next = idx + delta
      if (next < 0 || next >= this.images.length) return
      const arr = this.images.slice()
      const t = arr[idx]
      arr[idx] = arr[next]
      arr[next] = t
      this.images = arr
      this.syncImagesToForm()
    },
    remove(idx) {
      const arr = this.images.slice()
      arr.splice(idx, 1)
      this.images = arr
      this.syncImagesToForm()
    },
    async uploadToQiniu(options) {
      const { file, onProgress, onSuccess, onError } = options || {}
      if (!file) {
        onError && onError(new Error('未选择文件'))
        return
      }
      this.uploading = true
      this.uploadProgress = 0
      try {
        const compressedFile = await this.compressImageFile(file)
        const tokenData = await getQiniuUploadToken({
          filename: compressedFile.name,
          dir: 'talks'
        })

        const { uploadToken, key, domain, uploadUrl } = tokenData || {}
        if (!uploadToken || !key || !domain || !uploadUrl) {
          throw new Error('获取七牛上传凭证失败（返回数据不完整）')
        }

        const formData = new FormData()
        formData.append('token', uploadToken)
        formData.append('key', key)
        formData.append('file', compressedFile)

        await axios.post(uploadUrl, formData, {
          onUploadProgress: (evt) => {
            if (!evt || !evt.total) return
            const percent = Math.min(99, Math.round((evt.loaded / evt.total) * 100))
            this.uploadProgress = percent
            onProgress && onProgress({ percent })
          }
        })

        const normalizedDomain = String(domain).replace(/\/+$/, '')
        const url = this.normalizeUrl(`${normalizedDomain}/${key}`)
        this.images = [...this.images, url]
        this.syncImagesToForm()

        this.uploadProgress = 100
        onSuccess && onSuccess({ url, key })
      } catch (e) {
        console.error('上传失败:', e)
        const serverMsg = e?.response?.data?.error || e?.response?.data?.message || ''
        this.$message.error(serverMsg || (e && e.message ? e.message : '上传失败'))
        onError && onError(e)
      } finally {
        this.uploading = false
        setTimeout(() => (this.uploadProgress = 0), 600)
      }
    },
    async submit() {
      const content = (this.form.content || '').trim()
      if (!content) {
        this.$message.warning('请先填写说说正文')
        return
      }
      this.syncImagesToForm()

      const idNum = this.isEdit ? Number(this.talkId) : undefined
      if (this.isEdit && !Number.isFinite(idNum)) {
        this.$message.error('说说 ID 非法，请返回列表重试')
        return
      }

      const payload = {
        id: this.isEdit ? idNum : undefined,
        content,
        images: this.form.images || '[]',
        published: this.form.published
      }

      this.loading = true
      try {
        if (this.isEdit) {
          await updateTalk(payload)
          this.$message.success('保存成功')
        } else {
          await addTalk(payload)
          this.$message.success('发布成功')
        }
        this.$router.push('/talk')
      } catch (e) {
        console.error('提交失败:', e)
        this.$message.error('提交失败，请稍后再试')
      } finally {
        this.loading = false
      }
    },
    goBack() {
      this.$router.push('/talk')
    }
  }
}
</script>

<style scoped>
.add-talk {
  padding: 16px 18px 18px;
  width: 100%;
  max-width: 980px;
  margin: 0 auto;
}

.page-head {
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.page-title {
  margin: 0 0 6px;
  font-size: 1.5rem;
  font-weight: 600;
  color: #0f172a;
}

.head-actions {
  display: flex;
  align-items: center;
}

.panel {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 12px 12px;
}

.panel--images {
  margin-top: 12px;
}

.uploader-row {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.upload-progress {
  width: 220px;
  max-width: 100%;
}

.img-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.img-item {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 6px;
  overflow: hidden;
  position: relative;
}

.img {
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid #eef2f7;
  display: block;
}

.img-overlay {
  position: absolute;
  right: 8px;
  bottom: 8px;
  display: flex;
  gap: 6px;
}

.ov-btn {
  width: 28px;
  height: 28px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.28);
  background: rgba(15, 23, 42, 0.6);
  color: #fff;
  cursor: pointer;
  font-size: 12px;
  line-height: 1;
}

.ov-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.ov-btn--danger {
  background: rgba(220, 38, 38, 0.75);
}

.empty {
  margin-top: 12px;
  color: #94a3b8;
  font-size: 13px;
}

.actions {
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .add-talk {
    padding: 12px 10px 16px;
  }
  .actions {
    flex-direction: column-reverse;
    align-items: stretch;
  }
  .actions .el-button {
    width: 100%;
    margin: 0;
  }
  .page-head {
    margin-bottom: 10px;
    padding-bottom: 10px;
  }
  .page-title {
    margin: 0;
    font-size: 18px;
  }
  .upload-progress {
    width: 100%;
  }
}
</style>

