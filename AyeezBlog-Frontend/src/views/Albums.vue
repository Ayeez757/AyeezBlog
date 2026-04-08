<template>
  <div class="albums-page">
    <div class="albums-inner">
      <div class="albums-header">
        <div class="albums-tagline">SNAPSHOT COLLECTION</div>
        <div class="albums-title">ALBUMS</div>
      </div>
      <p class="albums-subtitle">
        点击任意相册进入二级页面，查看完整图片内容。
      </p>

      <div class="albums-grid">
        <article
          v-for="album in albums"
          :key="album.id"
          class="album-card"
          @click="goToAlbum(album.id)"
          role="button"
          tabindex="0"
          @keydown.enter="goToAlbum(album.id)"
        >
          <div class="album-stack">
            <img class="stack-photo stack-photo--one" :src="getCover(album, 0)" :alt="album.title" />
            <img class="stack-photo stack-photo--two" :src="getCover(album, 1)" :alt="album.title" />
            <img class="stack-photo stack-photo--three" :src="getCover(album, 2)" :alt="album.title" />
          </div>
          <h3 class="album-title">{{ album.title }}</h3>
          <p class="album-desc">{{ album.description }}</p>
        </article>
      </div>
      <p v-if="!isLoading && !albums.length" class="empty-tip">暂无相册内容</p>
    </div>
  </div>
</template>

<script>
import { fetchAlbums } from '@/api';

export default {
  name: 'Albums',
  data() {
    return {
      albums: [],
      isLoading: false
    };
  },
  async mounted() {
    await this.loadAlbums();
  },
  methods: {
    async loadAlbums() {
      this.isLoading = true;
      try {
        const resp = await fetchAlbums();
        const list = resp && Array.isArray(resp.data) ? resp.data : [];
        this.albums = list;
      } catch (error) {
        console.error('加载相册失败:', error);
        this.albums = [];
      } finally {
        this.isLoading = false;
      }
    },
    getCover(album, index) {
      const covers = album && Array.isArray(album.coverImages) ? album.coverImages : [];
      if (covers[index]) return covers[index];
      if (covers.length > 0) return covers[covers.length - 1];
      return '';
    },
    goToAlbum(id) {
      this.$router.push({ name: 'AlbumDetail', params: { id } });
    }
  }
};
</script>

<style scoped>
.albums-page {
  padding: 20px;
  width: 100%;
  box-sizing: border-box;
  color: white;
  display: flex;
  justify-content: center;
  min-height: calc(100vh - 68px);
}

.albums-inner {
  width: 100%;
  max-width: 1100px;
}

.albums-header {
  font-family: 'Bebas Neue', Arial, sans-serif;
  margin-bottom: 8px;
}

.albums-tagline {
  font-size: 20px;
  letter-spacing: 4px;
  color: #cccccc;
}

.albums-title {
  font-size: 46px;
  letter-spacing: 6px;
  background-image: linear-gradient(to right, #abe6a8, #00b828);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.albums-subtitle {
  margin: 6px 0 24px;
  color: #cccccc;
}

.albums-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 22px;
}

.album-card {
  background: transparent;
  border-radius: 12px;
  border: none;
  box-shadow: none;
  padding: 16px 16px 14px;
  cursor: pointer;
  transition: none;
  outline: none;
}

.album-card:hover,
.album-card:focus-visible {
  transform: none;
  border-color: transparent;
  box-shadow: none;
}

.album-stack {
  position: relative;
  height: 200px;
  margin-bottom: 14px;
}

.stack-photo {
  position: absolute;
  width: 78%;
  height: 165px;
  object-fit: cover;
  border-radius: 10px;
  border: 4px solid #ffffff;
  box-shadow: none;
  transition: transform 0.2s ease;
}

.stack-photo--one {
  left: 0;
  bottom: 0;
  transform: rotate(-4deg);
  z-index: 1;
}

.stack-photo--two {
  left: 11%;
  bottom: 6px;
  transform: rotate(2deg);
  z-index: 2;
}

.stack-photo--three {
  right: 0;
  bottom: 0;
  transform: rotate(5deg);
  z-index: 3;
}

.album-card:hover .stack-photo--one {
  transform: rotate(-6deg) translateY(-4px);
}

.album-card:hover .stack-photo--two {
  transform: rotate(3deg) translateY(-5px);
}

.album-card:hover .stack-photo--three {
  transform: rotate(7deg) translateY(-4px);
}

.album-title {
  font-size: 20px;
  margin: 0;
}

.album-desc {
  font-size: 13px;
  line-height: 1.5;
  color: rgba(255, 255, 255, 0.8);
  margin: 8px 0 0;
}

.empty-tip {
  color: #cccccc;
  margin-top: 12px;
}

@media (max-width: 980px) {
  .albums-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .albums-page {
    padding: 16px;
  }

  .albums-title {
    font-size: 38px;
    letter-spacing: 4px;
  }

  .albums-grid {
    grid-template-columns: 1fr;
  }

  .album-stack {
    height: 190px;
  }
}
</style>
