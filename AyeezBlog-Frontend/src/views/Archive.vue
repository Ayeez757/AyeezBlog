<template>
  <div class="archive">
    <div class="archive-inner">
      <div class="archive-header">
        <div class="archive-tagline">BROWSE&nbsp;ALL</div>
        <div class="archive-title-gradient">ARCHIVES</div>
      </div>
      <p class="archive-subtitle">
        以时间轴快速浏览所有文章，支持搜索、筛选与排序。
      </p>

      <!-- 工具栏 -->
      <div class="archive-toolbar">
        <div class="toolbar-left">
          <div class="toolbar-item">
            <span class="toolbar-label">搜索</span>
            <input
              v-model.trim="keyword"
              class="toolbar-input"
              type="text"
              placeholder="标题 / 描述关键词"
              @keydown.enter="noop"
            />
          </div>

          <div class="toolbar-item">
            <span class="toolbar-label">年份</span>
            <select v-model="year" class="toolbar-select">
              <option value="">全部</option>
              <option v-for="y in yearOptions" :key="y" :value="String(y)">
                {{ y }}
              </option>
            </select>
          </div>

          <div class="toolbar-item">
            <span class="toolbar-label">月份</span>
            <select v-model="month" class="toolbar-select" :disabled="!year">
              <option value="">全部</option>
              <option v-for="m in monthOptions" :key="m" :value="String(m)">
                {{ pad2(m) }}
              </option>
            </select>
          </div>
        </div>

        <div class="toolbar-right">
          <div class="toolbar-item">
            <span class="toolbar-label">排序</span>
            <button
              class="toolbar-btn"
              :class="{ 'toolbar-btn--active': orderType === 'desc' }"
              @click="setOrder('desc')"
              type="button"
              title="从新到旧"
            >
              倒序
            </button>
            <button
              class="toolbar-btn"
              :class="{ 'toolbar-btn--active': orderType === 'asc' }"
              @click="setOrder('asc')"
              type="button"
              title="从旧到新"
            >
              正序
            </button>
          </div>

          <div class="toolbar-item">
            <button class="toolbar-btn toolbar-btn--ghost" type="button" @click="resetFilters">
              清空
            </button>
          </div>
        </div>
      </div>

      <!-- 结果信息 -->
      <div class="archive-summary">
        <span class="summary-pill">
          共 {{ filteredPosts.length }} 篇
        </span>
        <span v-if="isLoading" class="summary-muted">加载中...</span>
        <span v-else-if="loadError" class="summary-error">加载失败：{{ loadError }}</span>
      </div>

      <!-- 时间轴 -->
      <div v-if="!isLoading && !loadError" class="timeline">
        <div v-if="!timelineGroups.length" class="empty-state">
          没有匹配的文章。
        </div>

        <div v-for="group in timelineGroups" :key="group.key" class="timeline-group">
          <div class="group-label">
            <span class="group-label__pill">{{ group.label }}</span>
          </div>

          <div class="group-items">
            <div
              v-for="post in group.items"
              :key="post.id"
              class="timeline-item row-reveal-item"
              @click="goToPost(post.id)"
              role="button"
              tabindex="0"
              @keydown.enter="goToPost(post.id)"
            >
              <div class="timeline-card">
                <div class="timeline-title" :title="post.title">{{ post.title || '未命名文章' }}</div>
                <div class="timeline-desc">
                  {{ post.description || '暂无描述' }}
                </div>
                <div class="timeline-date">{{ formatDate(post.updateTime) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="!isLoading && !loadError" class="archive-footer">
        <a class="archive-footer__link" href="https://blog.ayeez.cn/archives" target="_blank" rel="noopener">
          想看旧站归档？点这里
        </a>
      </div>
    </div>
  </div>
</template>

<script>
import { fetchPosts } from '@/api';

export default {
  name: 'Archive'
  ,
  data() {
    return {
      isLoading: true,
      loadError: '',

      posts: [],

      keyword: '',
      year: '',
      month: '',
      orderBy: 'update_time',
      orderType: 'desc',
      rowObserver: null,
      revealTimer: null
    };
  },
  computed: {
    normalizedKeyword() {
      return (this.keyword || '').trim().toLowerCase();
    },
    filteredPosts() {
      const kw = this.normalizedKeyword;
      const year = this.year ? Number(this.year) : null;
      const month = this.month ? Number(this.month) : null;

      return (this.posts || []).filter((p) => {
        const dt = new Date(p.updateTime || p.createTime || '');
        if (year && (!this.isValidDate(dt) || dt.getFullYear() !== year)) return false;
        if (month && (!this.isValidDate(dt) || dt.getMonth() + 1 !== month)) return false;

        if (!kw) return true;
        const title = (p.title || '').toLowerCase();
        const desc = (p.description || '').toLowerCase();
        return title.includes(kw) || desc.includes(kw);
      });
    },
    yearOptions() {
      const ys = new Set();
      (this.posts || []).forEach((p) => {
        const dt = new Date(p.updateTime || p.createTime || '');
        if (!this.isValidDate(dt)) return;
        ys.add(dt.getFullYear());
      });
      return Array.from(ys).sort((a, b) => b - a);
    },
    monthOptions() {
      if (!this.year) return [];
      const targetYear = Number(this.year);
      const ms = new Set();
      (this.posts || []).forEach((p) => {
        const dt = new Date(p.updateTime || p.createTime || '');
        if (!this.isValidDate(dt)) return;
        if (dt.getFullYear() !== targetYear) return;
        ms.add(dt.getMonth() + 1);
      });
      return Array.from(ms).sort((a, b) => b - a);
    },
    timelineGroups() {
      const groups = new Map();
      const list = this.filteredPosts.slice();

      // 二次排序：保障在客户端也按 updateTime 排
      list.sort((a, b) => {
        const ta = new Date(a.updateTime || a.createTime || '').getTime();
        const tb = new Date(b.updateTime || b.createTime || '').getTime();
        if (Number.isNaN(ta) && Number.isNaN(tb)) return 0;
        if (Number.isNaN(ta)) return 1;
        if (Number.isNaN(tb)) return -1;
        return this.orderType === 'asc' ? ta - tb : tb - ta;
      });

      list.forEach((p) => {
        const dt = new Date(p.updateTime || p.createTime || '');
        const y = this.isValidDate(dt) ? dt.getFullYear() : 0;
        const m = this.isValidDate(dt) ? dt.getMonth() + 1 : 0;
        const key = `${y}-${this.pad2(m)}`;
        const label = y && m ? `${y} / ${this.pad2(m)}` : '未知时间';
        if (!groups.has(key)) groups.set(key, { key, label, items: [] });
        groups.get(key).items.push(p);
      });

      // 分组顺序：按年月排序，方向跟 orderType 同步
      const arr = Array.from(groups.values());
      arr.sort((a, b) => {
        if (a.key === '0-00') return 1;
        if (b.key === '0-00') return -1;
        const [ay, am] = a.key.split('-').map(Number);
        const [by, bm] = b.key.split('-').map(Number);
        const va = ay * 100 + am;
        const vb = by * 100 + bm;
        return this.orderType === 'asc' ? va - vb : vb - va;
      });
      return arr;
    }
  },
  watch: {
    year() {
      // 切换年份后，如果月份不在该年份范围内，自动清空
      if (!this.year) {
        this.month = '';
        this.$nextTick(() => this.setupArchiveRowReveal());
        return;
      }
      if (this.month && !this.monthOptions.includes(Number(this.month))) {
        this.month = '';
      }
      this.$nextTick(() => this.setupArchiveRowReveal());
    },
    month() {
      this.$nextTick(() => this.setupArchiveRowReveal());
    },
    keyword() {
      this.scheduleArchiveReveal();
    },
    orderType() {
      // 排序变更时重新拉一次，确保跟后端一致（同时本地 computed 也会排序）
      this.loadAllPosts();
    }
  },
  async mounted() {
    await this.loadAllPosts();
  },
  beforeDestroy() {
    if (this.rowObserver) {
      this.rowObserver.disconnect();
      this.rowObserver = null;
    }
    if (this.revealTimer) {
      clearTimeout(this.revealTimer);
      this.revealTimer = null;
    }
  },
  methods: {
    noop() {},
    isValidDate(d) {
      return d instanceof Date && !Number.isNaN(d.getTime());
    },
    pad2(n) {
      return String(n).padStart(2, '0');
    },
    formatDate(dateString) {
      const d = new Date(dateString || '');
      if (!this.isValidDate(d)) return '未知';
      return d.toLocaleDateString('zh-CN');
    },
    setOrder(type) {
      this.orderType = type;
    },
    resetFilters() {
      this.keyword = '';
      this.year = '';
      this.month = '';
      this.$nextTick(() => this.setupArchiveRowReveal());
    },
    goToPost(id) {
      if (!id && id !== 0) return;
      this.$router.push({ name: 'PostDetail', params: { id } });
    },
    async loadAllPosts() {
      this.isLoading = true;
      this.loadError = '';
      try {
        // 尽量一次性拿到所有文章用于归档（若后端有限制，可再改为分页拉取）
        const page = 1;
        const pageSize = 9999;
        const resp = await fetchPosts(page, pageSize, this.orderBy, this.orderType);
        const rows = resp && resp.data && Array.isArray(resp.data.rows) ? resp.data.rows : [];
        this.posts = rows;
        this.$nextTick(() => this.setupArchiveRowReveal());
      } catch (e) {
        this.loadError = e && e.message ? e.message : '未知错误';
      } finally {
        this.isLoading = false;
      }
    },
    scheduleArchiveReveal() {
      if (this.revealTimer) clearTimeout(this.revealTimer);
      this.revealTimer = setTimeout(() => {
        this.setupArchiveRowReveal();
      }, 120);
    },
    setupArchiveRowReveal() {
      if (typeof window === 'undefined' || !('IntersectionObserver' in window)) return;
      if (this.rowObserver) this.rowObserver.disconnect();

      const items = document.querySelectorAll('.timeline .row-reveal-item');
      items.forEach((item) => item.classList.remove('row-revealed'));

      this.rowObserver = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
          if (!entry.isIntersecting) return;
          entry.target.classList.add('row-revealed');
          this.rowObserver.unobserve(entry.target);
        });
      }, {
        threshold: 0.12,
        rootMargin: '0px 0px -10% 0px'
      });

      items.forEach((item) => this.rowObserver.observe(item));
    }
  }
};
</script>

<style scoped>
.archive {
  padding: 20px;
  width: 100%;
  box-sizing: border-box;
  color: white;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  min-height: calc(100vh - 68px);
}

.archive-inner {
  width: 100%;
  max-width: 1100px;
}

.archive-header {
  font-family: 'Bebas Neue', Arial, sans-serif;
  margin-bottom: 8px;
}

.archive-tagline {
  font-size: 20px;
  letter-spacing: 4px;
  color: #cccccc;
}

.archive-title-gradient {
  font-size: 46px;
  letter-spacing: 6px;
  background-image: linear-gradient(to right, #abe6a8, #00b828);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.archive-subtitle {
  margin-bottom: 18px;
  color: #cccccc;
}

.archive-toolbar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  padding: 14px 14px;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.35);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.35);
}

.toolbar-left,
.toolbar-right {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.toolbar-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toolbar-label {
  font-size: 12px;
  color: #d0d0d0;
  letter-spacing: 0.04em;
}

.toolbar-input,
.toolbar-select {
  height: 34px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.28);
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  padding: 0 10px;
  outline: none;
}

/* 下拉框：自定义箭头 + 更贴合站内风格 */
.toolbar-select {
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
  padding-right: 34px;
  cursor: pointer;
  background-image:
    linear-gradient(135deg, rgba(108, 171, 106, 0.12), rgba(42, 184, 73, 0.18)),
    url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='18' height='18' viewBox='0 0 24 24'%3E%3Cpath fill='%23a7f0b5' d='M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6z'/%3E%3C/svg%3E");
  background-repeat: no-repeat, no-repeat;
  background-position: 0 0, right 10px center;
  background-size: 100% 100%, 18px 18px;
}

.toolbar-input:focus,
.toolbar-select:focus {
  border: 1px solid rgba(0, 184, 40, 0.9);
  box-shadow: 0 0 0 3px rgba(0, 184, 40, 0.16);
}

.toolbar-select:hover:not(:disabled) {
  border: 1px solid rgba(0, 184, 40, 0.75);
}

.toolbar-input {
  width: 240px;
}

.toolbar-input::placeholder {
  color: rgba(255, 255, 255, 0.55);
}

.toolbar-select:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

/* 选项列表（不同浏览器支持度不一，但尽量统一） */
.toolbar-select option {
  background-color: #111;
  color: #fff;
}

.toolbar-btn {
  height: 34px;
  padding: 0 12px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.28);
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  cursor: pointer;
  transition: transform 0.15s ease, border 0.15s ease, box-shadow 0.15s ease, background 0.15s ease;
}

.toolbar-btn:hover {
  transform: translateY(-1px);
  border: 1px solid rgba(0, 184, 40, 0.9);
  box-shadow: 0 6px 14px rgba(0, 184, 40, 0.22);
}

.toolbar-btn--active {
  border: 1px solid rgba(0, 184, 40, 0.9);
  background: rgba(0, 184, 40, 0.18);
}

.toolbar-btn--ghost {
  background: transparent;
}

.archive-summary {
  margin-top: 12px;
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.summary-pill {
  display: inline-flex;
  align-items: center;
  height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(0, 184, 40, 0.16);
  border: 1px solid rgba(0, 184, 40, 0.45);
  color: #dfffe5;
  font-size: 12px;
}

.summary-muted {
  font-size: 12px;
  color: #c8c8c8;
}

.summary-error {
  font-size: 12px;
  color: #ffb4b4;
}

.timeline {
  margin-top: 14px;
  padding: 8px 0 24px;
}

.timeline-group {
  position: relative;
  padding: 10px 0 18px;
}

.group-label {
  position: sticky;
  top: 72px;
  z-index: 5;
  margin-bottom: 10px;
}

.group-label__pill {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.35);
  color: #ffffff;
  font-size: 12px;
  letter-spacing: 0.05em;
}

.group-items {
  position: relative;
  padding-left: 22px;
}

.group-items::before {
  content: '';
  position: absolute;
  left: 10px;
  top: 6px;
  bottom: 8px;
  width: 2px;
  background: linear-gradient(to bottom, rgba(0, 184, 40, 0.9), rgba(255, 255, 255, 0.15));
  border-radius: 999px;
  box-shadow: 0 0 10px rgba(0, 184, 40, 0.25);
}

.timeline-item {
  position: relative;
  display: flex;
  gap: 10px;
  padding: 10px 0;
  cursor: pointer;
  outline: none;
  opacity: 0;
  transform: translateY(24px);
}

.timeline-item.row-revealed {
  opacity: 1;
  transform: translateY(0);
  transition: opacity 0.55s ease, transform 0.55s ease;
}

.timeline-item:focus-visible .timeline-card {
  border: 1px solid rgba(0, 184, 40, 0.95);
  box-shadow: 0 8px 18px rgba(0, 184, 40, 0.18);
}

.timeline-card {
  width: 100%;
  padding: 14px 14px;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(108, 171, 106, 0.28), rgba(42, 184, 73, 0.55));
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: inset 0 0 10px rgba(255, 255, 255, 0.12), 0 6px 16px rgba(0, 0, 0, 0.35);
  transition: transform 0.15s ease, border 0.15s ease, box-shadow 0.15s ease;
  position: relative;
  padding-bottom: 34px;
}

.timeline-item:hover .timeline-card {
  transform: translateY(-2px);
  border: 1px solid rgba(0, 184, 40, 0.85);
  box-shadow: 0 8px 18px rgba(0, 184, 40, 0.18);
}

.timeline-date {
  position: absolute;
  right: 14px;
  bottom: 10px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.75);
  white-space: nowrap;
}

.timeline-title {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: left;
}

.timeline-desc {
  margin-top: 8px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.82);
  line-height: 1.4;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
  color: #cfcfcf;
}

.archive-footer {
  margin-top: 18px;
  padding-bottom: 20px;
}

.archive-footer__link {
  color: #7af58f;
  text-decoration: none;
}

.archive-footer__link:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .archive {
    padding: 16px;
  }

  .archive-title-gradient {
    font-size: 38px;
  }

  .toolbar-input {
    width: 200px;
  }

  .group-label {
    top: 60px;
  }

  .timeline-title {
    font-size: 15px;
  }
}

@media (max-width: 520px) {
  .archive-tagline {
    font-size: 18px;
    letter-spacing: 3px;
  }

  .archive-title-gradient {
    font-size: 34px;
    letter-spacing: 4px;
  }

  .archive-subtitle {
    margin-bottom: 14px;
    font-size: 13px;
    line-height: 1.6;
  }

  .archive-toolbar {
    padding: 12px 12px;
    gap: 10px;
  }

  .toolbar-left,
  .toolbar-right {
    width: 100%;
    justify-content: flex-start;
  }

  .toolbar-item {
    width: 100%;
    flex: 1 1 100%;
    flex-wrap: wrap;
    align-items: flex-start;
  }

  .toolbar-label {
    width: 100%;
  }

  .toolbar-input,
  .toolbar-select {
    width: 100%;
    min-width: 0;
  }

  .toolbar-btn {
    flex: 1 1 auto;
  }

  .toolbar-right .toolbar-item {
    justify-content: flex-start;
  }

  .group-label {
    top: 56px;
  }

  .group-items {
    padding-left: 18px;
  }

  .group-items::before {
    left: 8px;
  }

  .timeline-card {
    padding: 12px 12px;
    padding-bottom: 34px;
  }

  .timeline-date {
    right: 12px;
  }
}

@media (max-width: 360px) {
  .archive {
    padding: 12px;
  }

  .archive-title-gradient {
    font-size: 30px;
    letter-spacing: 3px;
  }

  .archive-toolbar {
    padding: 10px 10px;
  }

  .group-items {
    padding-left: 16px;
  }

  .group-items::before {
    left: 7px;
  }
}
</style>