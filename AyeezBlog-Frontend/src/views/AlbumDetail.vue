<template>
  <div class="album-detail-page">
    <div class="album-detail-inner">
      <button class="back-btn" type="button" @click="goBack">← 返回相册列表</button>

      <div v-if="album" class="album-content">
        <h1 class="album-name">{{ album.title }}</h1>
        <p class="album-intro">{{ album.description }}</p>

        <div class="photo-grid">
          <figure v-for="photo in album.photos" :key="photo.id" class="photo-card">
            <img
              :src="photo.imageUrl"
              :alt="photo.caption || 'album photo'"
              loading="lazy"
              data-magnetic-cursor
              @load="onPhotoLoad"
              @click="openPreview(photo.imageUrl, photo.caption)"
            />
            <figcaption>{{ photo.caption }}</figcaption>
          </figure>
        </div>
      </div>

      <div v-else class="empty-state">
        未找到该相册，可能已删除或链接无效。
      </div>

      <div v-if="preview.visible" class="image-preview-mask" @click="closePreview">
        <img
          class="image-preview-img"
          :src="preview.url"
          :alt="preview.caption || 'preview image'"
          @click.stop
        />
        <button class="image-preview-close" type="button" @click="closePreview">×</button>
      </div>
    </div>
  </div>
</template>

<script>
import { fetchAlbumById } from '@/api';

export default {
  name: 'AlbumDetail',
  data() {
    return {
      album: null,
      isLoading: false,
      preview: {
        visible: false,
        url: '',
        caption: ''
      }
    };
  },
  async mounted() {
    await this.loadAlbum();
    window.addEventListener('keydown', this.handlePreviewEsc);
    window.addEventListener('resize', this.relayoutGrid, { passive: true });
  },
  beforeDestroy() {
    window.removeEventListener('keydown', this.handlePreviewEsc);
    window.removeEventListener('resize', this.relayoutGrid);
  },
  watch: {
    '$route.params.id': {
      handler() {
        this.loadAlbum();
      }
    }
  },
  methods: {
    async loadAlbum() {
      this.isLoading = true;
      try {
        const id = Number(this.$route.params.id);
        if (!id) {
          this.album = null;
          return;
        }
        const resp = await fetchAlbumById(id);
        this.album = resp ? resp.data : null;
        this.$nextTick(() => {
          this.relayoutGrid();
        });
      } catch (error) {
        console.error('加载相册详情失败:', error);
        this.album = null;
      } finally {
        this.isLoading = false;
      }
    },
    goBack() {
      this.$router.push({ name: 'Albums' });
    },
    openPreview(url, caption) {
      if (!url) return;
      this.preview.visible = true;
      this.preview.url = url;
      this.preview.caption = caption || '';
    },
    closePreview() {
      this.preview.visible = false;
      this.preview.url = '';
      this.preview.caption = '';
    },
    handlePreviewEsc(event) {
      if (event.key === 'Escape' && this.preview.visible) {
        this.closePreview();
      }
    },
    onPhotoLoad(event) {
      const card = event && event.target ? event.target.closest('.photo-card') : null;
      if (card) this.resizeGridItem(card);
    },
    relayoutGrid() {
      const cards = document.querySelectorAll('.photo-grid .photo-card');
      cards.forEach((card) => this.resizeGridItem(card));
    },
    resizeGridItem(card) {
      const grid = document.querySelector('.photo-grid');
      if (!grid || !card) return;
      const rowGap = 12;
      const rowHeight = 8;
      const total = card.getBoundingClientRect().height;
      const span = Math.max(1, Math.ceil((total + rowGap) / (rowHeight + rowGap)));
      card.style.gridRowEnd = `span ${span}`;
    }
  }
};
</script>

<style scoped>
.album-detail-page {
  padding: 20px;
  width: 100%;
  box-sizing: border-box;
  color: white;
  display: flex;
  justify-content: center;
  min-height: calc(100vh - 68px);
}

.album-detail-inner {
  width: 100%;
  max-width: 1100px;
}

.back-btn {
  height: 36px;
  padding: 0 14px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: transparent;
  color: #fff;
  cursor: pointer;
  transition: none;
}

.back-btn:hover {
  transform: none;
  border-color: rgba(255, 255, 255, 0.2);
  box-shadow: none;
}

.album-content {
  margin-top: 18px;
}

.album-name {
  margin: 0;
  font-size: 34px;
  color: #ffffff;
}

.album-intro {
  margin: 10px 0 20px;
  color: rgba(255, 255, 255, 0.82);
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  grid-auto-rows: 8px;
  gap: 12px;
  align-items: start;
}

.photo-card {
  margin: 0;
  padding: 0;
  border-radius: 0;
  background: transparent;
  border: none;
  box-shadow: none;
}

.photo-card img {
  width: 100%;
  height: auto;
  object-fit: contain;
  border-radius: 0;
  border: 3px solid #ffffff;

  display: block;
  cursor: zoom-in;
  transform: scale(0.96);
  transform-origin: center top;
  transition: transform 0.2s ease;
}

.photo-card img:hover {
  transform: scale(0.99);
}

.photo-card figcaption {
  margin-top: 2px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
}

.empty-state {
  margin-top: 18px;
  color: #cccccc;
}

.image-preview-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.88);
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 22px;
}

.image-preview-img {
  max-width: 96vw;
  max-height: 92vh;
  width: auto;
  height: auto;
  object-fit: contain;
  border-radius: 0;
}

.image-preview-close {
  position: fixed;
  top: 18px;
  right: 22px;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
}

@media (max-width: 980px) {
  .photo-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .album-detail-page {
    padding: 16px;
  }

  .album-name {
    font-size: 28px;
  }

  .photo-grid {
    grid-template-columns: 1fr;
  }

  .photo-card img {
    height: auto;
  }
}
</style>
