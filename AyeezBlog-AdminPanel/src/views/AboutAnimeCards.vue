<template>
  <div class="about-anime-cards page-card">
    <div class="page-toolbar">
      <el-button native-type="button" @click="goTable">返回表格列表</el-button>
      <el-button native-type="button" type="primary" :loading="loading" @click="onRefreshClick">刷新数据</el-button>
      <el-button
        native-type="button"
        type="warning"
        :disabled="!sortDirty || savingOrder"
        :loading="savingOrder"
        @click="saveOrder"
      >
        保存排序
      </el-button>
      <span class="toolbar-hint">拖动左侧手柄调整顺序，与前台 About 页网格一致；保存后写入数据库</span>
    </div>

    <div v-loading="loading" class="preview-canvas">
      <section class="section-card-inner" aria-label="追番卡片预览">
        <div class="anime-title-row">
          <h2 class="section-title">追番列表</h2>
          <span class="preview-badge">预览</span>
        </div>

        <el-empty v-if="!loading && !list.length" description="暂无数据，请先在表格列表中添加" />

        <draggable
          v-else
          v-model="list"
          item-key="id"
          tag="div"
          class="anime-grid"
          handle=".drag-handle"
          ghost-class="anime-card-ghost"
          chosen-class="anime-card-chosen"
          :animation="200"
          @end="onDragEnd"
        >
          <template #item="{ element, index }">
            <div class="anime-card" :title="element.title || `番剧 ${index + 1}`">
              <button
                type="button"
                class="drag-handle"
                aria-label="拖动排序"
                @click.stop
              >
                ⋮⋮
              </button>
              <div class="anime-card-body">
                <img
                  :src="element.imageUrl"
                  :alt="element.title || `番剧 ${index + 1}`"
                  class="anime-card-img"
                  loading="lazy"
                  draggable="false"
                  @dragstart.prevent
                />
              </div>
            </div>
          </template>
        </draggable>
      </section>
    </div>
  </div>
</template>

<script>
import draggable from 'vuedraggable'
import { getAboutAnimeList, reorderAboutAnime } from '@/api'

export default {
  name: 'AboutAnimeCards',
  components: { draggable },
  data() {
    return {
      loading: false,
      savingOrder: false,
      sortDirty: false,
      list: []
    }
  },
  mounted() {
    this.fetchList()
  },
  beforeRouteLeave(to, from, next) {
    if (!this.sortDirty) {
      next()
      return
    }
    this.$confirm('当前有未保存的排序调整，离开将丢失，是否继续？', '提示', { type: 'warning' })
      .then(() => next())
      .catch(() => next(false))
  },
  methods: {
    goTable() {
      this.$router.push('/about-anime')
    },
    onDragEnd(evt) {
      if (
        evt &&
        typeof evt.oldIndex === 'number' &&
        typeof evt.newIndex === 'number' &&
        evt.oldIndex !== evt.newIndex
      ) {
        this.sortDirty = true
      }
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
        this.$confirm('当前有未保存的排序调整，刷新将丢失，是否继续？', '提示', { type: 'warning' })
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
      } catch (e) {
        console.error(e)
        this.$message.error(e.message || '保存排序失败')
        await this.fetchList()
      } finally {
        this.savingOrder = false
      }
    }
  }
}
</script>

<style scoped>
.about-anime-cards {
  padding: 16px;
}

.toolbar-hint {
  margin-left: 12px;
  font-size: 13px;
  color: #6b7280;
}

.preview-canvas {
  margin-top: 16px;
  min-height: 200px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: rgba(0, 0, 0, 0.78);
  box-shadow: 0 10px 26px rgba(0, 0, 0, 0.15);
}

.section-card-inner {
  padding: 20px 18px;
}

.anime-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.section-title {
  margin: 0;
  font-family: 'Bebas Neue', 'Arial Narrow', Arial, sans-serif;
  font-size: 34px;
  letter-spacing: 0.09em;
  background-image: linear-gradient(to right, #abe6a8, #00b828);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.preview-badge {
  flex-shrink: 0;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(0, 184, 40, 0.45);
  color: rgba(122, 245, 143, 0.95);
  background: rgba(0, 184, 40, 0.12);
}

.anime-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
  gap: 6px;
  padding: 0;
}

.anime-card {
  position: relative;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(0, 0, 0, 0.35);
  overflow: hidden;
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.18);
  transition: transform 0.15s ease, border 0.15s ease, box-shadow 0.15s ease;
}

.anime-card:hover {
  transform: translateY(-2px);
  border-color: rgba(0, 184, 40, 0.75);
  box-shadow: 0 16px 36px rgba(0, 184, 40, 0.12);
}

.drag-handle {
  position: absolute;
  top: 6px;
  left: 6px;
  z-index: 3;
  width: 28px;
  height: 28px;
  padding: 0;
  margin: 0;
  border: 1px solid rgba(255, 255, 255, 0.35);
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.65);
  color: rgba(255, 255, 255, 0.92);
  font-size: 14px;
  line-height: 1;
  cursor: grab;
  display: flex;
  align-items: center;
  justify-content: center;
  user-select: none;
}

.drag-handle:active {
  cursor: grabbing;
}

.anime-card-body {
  display: block;
}

.anime-card-img {
  width: 100%;
  aspect-ratio: 125 / 176;
  height: auto;
  object-fit: cover;
  display: block;
  filter: grayscale(25%);
  transition: filter 0.2s ease, transform 0.2s ease;
  vertical-align: middle;
  pointer-events: none;
}

.anime-card:hover .anime-card-img {
  filter: grayscale(0%);
  transform: scale(1.02);
}

:deep(.anime-card-ghost) {
  opacity: 0.45;
  border: 2px dashed rgba(0, 184, 40, 0.85);
}

:deep(.anime-card-chosen) {
  border-color: rgba(0, 184, 40, 0.9);
  box-shadow: 0 12px 32px rgba(0, 184, 40, 0.2);
}
</style>
