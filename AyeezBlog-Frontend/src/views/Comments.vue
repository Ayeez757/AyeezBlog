<template>
  <div class="comments">
    <div class="comments-inner">
      <div class="comments-header">
        <div class="comments-tagline">SAY&nbsp;HELLO</div>
        <div class="comments-title-gradient">COMMENTS</div>
      </div>

      <p class="comments-subtitle">
        这里会把 <span class="highlight">全站所有评论</span>汇总到一起。
        旧站留言页请看：
        <a class="sub-link" href="https://blog.ayeez.cn/comments" target="_blank" rel="noopener">旧站留言页</a>
      </p>

      <div class="comment-card comment-card--all">
        <div class="card-header">
          <h2 class="card-title">全站评论汇总</h2>
          <div class="card-actions">
            <button
              class="seg-btn"
              type="button"
              :class="{ 'seg-btn--active': viewMode === 'recent' }"
              @click="viewMode = 'recent'"
            >
              最新
            </button>
            <button
              class="seg-btn"
              type="button"
              :class="{ 'seg-btn--active': viewMode === 'tree' }"
              @click="viewMode = 'tree'"
            >
              按页面/层级
            </button>
            <button class="refresh-btn" type="button" @click="loadAllSiteComments" :disabled="loadingAll">
              {{ loadingAll ? `加载中 ${progressText}` : '刷新' }}
            </button>
          </div>
        </div>

        <div class="recent-meta">
          <span class="meta-pill">页面数：{{ siteUrls.length }}</span>
          <span class="meta-pill">评论总数：{{ flatComments.length }}</span>
          <span v-if="loadError" class="meta-error">加载失败：{{ loadError }}</span>
          <span v-else-if="loadingAll" class="meta-muted">正在拉取全站评论，数量多时会稍慢…</span>
        </div>

        <div v-if="!loadingAll && !loadError && !flatComments.length" class="empty-state">
          暂无评论数据。
        </div>

        <div v-if="viewMode === 'recent' && flatComments.length" class="recent-list">
          <a
            v-for="item in recentComments"
            :key="item.id"
            class="recent-item"
            :href="item._href"
            target="_blank"
            rel="noopener"
            :title="item.url || ''"
          >
            <img class="recent-avatar" :src="item.avatar || fallbackAvatar(item)" alt="" />
            <div class="recent-body">
              <div class="recent-top">
                <span class="recent-nick">{{ item.nick || '匿名' }}</span>
                <span class="recent-time">{{ formatTime(item.created, item.relativeTime) }}</span>
              </div>
              <div class="recent-url">
                {{ displayPath(item.url) }}
                <span v-if="item._replyToNick" class="reply-tag">回复 @{{ item._replyToNick }}</span>
              </div>
              <div class="recent-text">{{ item.commentText || '' }}</div>
            </div>
          </a>
        </div>

        <div v-if="viewMode === 'tree' && siteGroups.length" class="tree-groups">
          <div v-for="g in siteGroups" :key="g.url" class="tree-group">
            <div class="group-head">
              <a class="group-link" :href="toHref(g.url)" target="_blank" rel="noopener">
                {{ displayPath(g.url) }}
              </a>
              <span class="group-count">{{ g.count }} 条</span>
            </div>
            <div v-if="!g.items.length" class="group-empty">该页面暂无评论</div>
            <div v-else class="tree-list">
              <CommentNode
                v-for="node in g.items"
                :key="node.id"
                :node="node"
                :depth="0"
                :to-href="toHref"
                :format-time="formatTime"
                :display-path="displayPath"
                :fallback-avatar="fallbackAvatar"
              />
            </div>
          </div>
        </div>
      </div>

      <div class="comment-card">
        <div class="card-header">
          <h2 class="card-title">留言区</h2>
          <span class="meta-muted">提示：这里发表的是本页（/comments）评论</span>
        </div>
        <div id="tcomment" class="twikoo-wrap"></div>
      </div>
    </div>
  </div>
</template>

<script>
import { fetchPosts } from '@/api';

const CommentNode = {
  name: 'CommentNode',
  props: {
    node: { type: Object, required: true },
    depth: { type: Number, required: true },
    toHref: { type: Function, required: true },
    formatTime: { type: Function, required: true },
    displayPath: { type: Function, required: true },
    fallbackAvatar: { type: Function, required: true }
  },
  template: `
    <div class="node" :style="{ '--depth': depth }">
      <div class="node-row">
        <img class="node-avatar" :src="node.avatar || fallbackAvatar(node)" alt="" />
        <div class="node-body">
          <div class="node-top">
            <span class="node-nick">{{ node.nick || '匿名' }}</span>
            <span class="node-time">{{ formatTime(node.created, node.relativeTime) }}</span>
          </div>
          <div class="node-meta">
            <a class="node-link" :href="toHref(node.url)" target="_blank" rel="noopener">{{ displayPath(node.url) }}</a>
          </div>
          <div class="node-text">{{ node.commentText || '' }}</div>
        </div>
      </div>
      <div v-if="node.replies && node.replies.length" class="node-children">
        <CommentNode
          v-for="c in node.replies"
          :key="c.id"
          :node="c"
          :depth="depth + 1"
          :to-href="toHref"
          :format-time="formatTime"
          :display-path="displayPath"
          :fallback-avatar="fallbackAvatar"
        />
      </div>
    </div>
  `
};

export default {
  name: 'Comments',
  components: { CommentNode },
  data() {
    return {
      envId: 'https://twikoo.ayeez.cn',

      viewMode: 'recent', // recent | tree

      loadingAll: false,
      loadError: '',
      partialErrors: [],
      loadedUrls: 0,
      totalUrls: 0,

      _postsCache: [],
      siteCommentMap: new Map()
    };
  },
  computed: {
    progressText() {
      if (!this.totalUrls) return '';
      return `${this.loadedUrls}/${this.totalUrls}`;
    },
    siteUrls() {
      const urls = new Set(['/comments', '/link']);
      (this._postsCache || []).forEach((p) => {
        const id = p && (p.id ?? p.postId);
        if (id || id === 0) urls.add(`/posts/${id}`);
      });
      return Array.from(urls);
    },
    siteGroups() {
      const arr = [];
      for (const url of this.siteUrls) {
        const g = this.siteCommentMap.get(url);
        if (g) arr.push(g);
        else arr.push({ url, items: [], count: 0 });
      }
      arr.sort((a, b) => (b.count || 0) - (a.count || 0));
      return arr;
    },
    flatComments() {
      const out = [];
      const walk = (node, url, parentNick) => {
        if (!node) return;
        const copy = { ...node, url: node.url || url };
        if (parentNick) copy._replyToNick = parentNick;
        copy._href = this.toHref(copy.url);
        out.push(copy);
        const reps = Array.isArray(node.replies) ? node.replies : [];
        reps.forEach((r) => walk(r, url, node.nick || '匿名'));
      };
      this.siteGroups.forEach((g) => {
        (g.items || []).forEach((n) => walk(n, g.url, ''));
      });
      return out;
    },
    recentComments() {
      return this.flatComments
        .slice()
        .sort((a, b) => (Number(b.created) || 0) - (Number(a.created) || 0));
    }
  },
  mounted() {
    if (window && window.twikoo) {
      window.twikoo.init({
        envId: this.envId,
        el: '#tcomment',
        path: '/comments'
      });
    } else {
      this.loadError = 'Twikoo 未加载';
    }
    this.loadAllSiteComments();
  },
  methods: {
    mapCommentPathToPagePath(raw) {
      const s = (raw || '').toString();
      if (!s) return s;
      // 友链页：历史评论聚合在 /link，但实际页面是 /links
      if (s === '/link') return '/links';
      return s;
    },
    htmlToText(html) {
      const s = (html || '').toString();
      if (!s) return '';
      if (typeof document === 'undefined') return s;
      const el = document.createElement('div');
      el.innerHTML = s;
      return (el.innerText || el.textContent || '').trim();
    },
    normalizeCommentNode(node) {
      if (!node || typeof node !== 'object') return node;
      if (!node.commentText) {
        if (typeof node.comment === 'string') node.commentText = this.htmlToText(node.comment);
        else if (typeof node.text === 'string') node.commentText = node.text;
      }
      const reps = Array.isArray(node.replies) ? node.replies : [];
      if (reps.length) node.replies = reps.map((r) => this.normalizeCommentNode(r));
      return node;
    },
    toHref(raw) {
      const origin = (window && window.location && window.location.origin) ? window.location.origin : '';
      const s = this.mapCommentPathToPagePath((raw || '').toString());
      if (!s) return origin || '#';
      if (/^https?:\/\//i.test(s)) return s;
      return `${origin}${s}`;
    },
    async loadAllSiteComments() {
      this.loadingAll = true;
      this.loadError = '';
      this.partialErrors = [];
      this.loadedUrls = 0;
      try {
        const resp = await fetchPosts(1, 9999, 'update_time', 'desc');
        const rows = resp && resp.data && Array.isArray(resp.data.rows) ? resp.data.rows : [];
        this._postsCache = rows;

        const urls = this.siteUrls;
        this.totalUrls = urls.length;

        const nextMap = new Map();
        const concurrency = 3;
        let idx = 0;
        const worker = async () => {
          while (idx < urls.length) {
            const url = urls[idx++];
            try {
              const g = await this.loadAllForUrl(url);
              nextMap.set(url, g);
            } catch (e) {
              const msg = e && e.message ? e.message : '未知错误';
              this.partialErrors.push({ url, message: msg });
              nextMap.set(url, { url, items: [], count: 0, _error: msg });
            }
            this.loadedUrls += 1;
          }
        };
        const workers = Array.from({ length: Math.min(concurrency, urls.length) }, () => worker());
        await Promise.all(workers);

        this.siteCommentMap = nextMap;
        if (this.partialErrors.length) {
          const sample = this.partialErrors.slice(0, 3).map((x) => `${x.url}（${x.message}）`).join('；');
          this.loadError = `部分页面抓取失败：${this.partialErrors.length}/${this.totalUrls}${sample ? `，例如：${sample}` : ''}`;
        }
      } catch (e) {
        this.loadError = e && e.message ? e.message : '未知错误';
        this.siteCommentMap = new Map();
      } finally {
        this.loadingAll = false;
      }
    },
    async loadAllForUrl(url) {
      const fetchAllByKey = async (twikooUrlKey) => {
        const allTop = [];
        let before = undefined;
        let more = true;
        let guard = 0;
        let totalCount = 0;

        while (more && guard < 10000) {
          guard += 1;
          const res = await this.twikooCommentGet({ url: twikooUrlKey, before });
          const data = Array.isArray(res && res.data) ? res.data : [];
          more = !!(res && res.more);
          totalCount = typeof res.count === 'number' ? res.count : totalCount;
          allTop.push(...data);

          if (!more) break;

          const createdList = [];
          const collectCreated = (nodes) => {
            (nodes || []).forEach((n) => {
              if (n && (typeof n.created === 'number' || typeof n.created === 'string')) {
                const t = Number(n.created);
                if (!Number.isNaN(t) && t) createdList.push(t);
              }
              if (n && Array.isArray(n.replies) && n.replies.length) collectCreated(n.replies);
            });
          };
          collectCreated(data);
          const minCreated = createdList.length ? Math.min(...createdList) : 0;
          if (!minCreated || (before && minCreated >= before)) {
            more = false;
            break;
          }
          before = minCreated;
        }

        return { items: allTop, count: totalCount };
      };

      const pagePath = this.mapCommentPathToPagePath(url);
      const abs = this.toHref(pagePath);
      const keys = Array.from(new Set([pagePath, abs].filter(Boolean)));

      // 兼容不同部署/迁移形态：Twikoo 的 url key 可能是相对路径，也可能是完整 URL
      const mergedTop = [];
      let mergedCount = 0;
      for (const k of keys) {
        const { items, count } = await fetchAllByKey(k);
        mergedTop.push(...(items || []));
        if (typeof count === 'number' && count > mergedCount) mergedCount = count;
      }

      const seen = new Set();
      const dedupTree = (nodes) => {
        const out = [];
        (nodes || []).forEach((n) => {
          if (!n || !n.id) return;
          if (seen.has(n.id)) return;
          seen.add(n.id);
          const replies = Array.isArray(n.replies) ? dedupTree(n.replies) : [];
          out.push(this.normalizeCommentNode({ ...n, url, replies }));
        });
        return out;
      };
      const items = dedupTree(mergedTop);
      const count = mergedCount || this.countTree(items);
      return { url, items, count };
    },
    countTree(nodes) {
      let c = 0;
      const walk = (arr) => {
        (arr || []).forEach((n) => {
          c += 1;
          if (n && Array.isArray(n.replies) && n.replies.length) walk(n.replies);
        });
      };
      walk(nodes);
      return c;
    },
    async twikooCommentGet({ url, before }) {
      const payload = { event: 'COMMENT_GET', url };
      if (before) payload.before = before;
      const r = await fetch(this.envId, {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (!r.ok) {
        let detail = '';
        try {
          detail = await r.text();
        } catch (_) {
          // ignore
        }
        throw new Error(`Twikoo 请求失败：${r.status}${detail ? ` ${detail}` : ''}`);
      }
      return await r.json();
    },
    displayPath(u) {
      const s = this.mapCommentPathToPagePath((u || '').toString());
      if (!s) return '未知页面';
      try {
        const parsed = new URL(s, window.location.origin);
        return parsed.pathname || s;
      } catch (_) {
        return s;
      }
    },
    formatTime(created, relativeTime) {
      if (relativeTime) return relativeTime;
      const ts = typeof created === 'number' ? created : Number(created);
      if (!ts || Number.isNaN(ts)) return '';
      const d = new Date(ts);
      if (Number.isNaN(d.getTime())) return '';
      return d.toLocaleString('zh-CN', { hour12: false });
    },
    fallbackAvatar(item) {
      const md5 = item && item.mailMd5 ? String(item.mailMd5) : '';
      if (!md5) return 'https://www.weavatar.com/avatar/?d=mp&s=96';
      return `https://www.weavatar.com/avatar/${md5}?d=mp&s=96`;
    }
  }
};
</script>

<style scoped>
.comments {
  width: 100%;
  box-sizing: border-box;
  color: white;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  min-height: calc(100vh - 68px);
  padding: 20px;
  --indent-step: 16px;
}

.comments-inner {
  width: 100%;
  max-width: 1100px;
}

.comments-header {
  font-family: 'Bebas Neue', Arial, sans-serif;
  margin-bottom: 8px;
}

.comments-tagline {
  font-size: 20px;
  letter-spacing: 4px;
  color: #cccccc;
}

.comments-title-gradient {
  font-size: 46px;
  letter-spacing: 6px;
  background-image: linear-gradient(to right, #abe6a8, #00b828);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.comments-subtitle {
  margin-bottom: 18px;
  color: #cccccc;
}

.sub-link {
  color: #7af58f;
  text-decoration: none;
}

.sub-link:hover {
  text-decoration: underline;
}

.highlight {
  color: #7af58f;
  font-weight: 600;
}

.comment-card {
  margin-top: 20px;
  border-radius: 12px;
  padding: 18px 18px;
  width: 100%;
  background: rgba(0, 0, 0, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.35);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.35);
}

.comment-card--all {
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
  background: linear-gradient(135deg, rgba(108, 171, 106, 0.12), rgba(0, 0, 0, 0.82));
  border: 1px solid rgba(255, 255, 255, 0.28);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.card-actions {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.card-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.seg-btn {
  height: 32px;
  padding: 0 10px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.28);
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
  cursor: pointer;
  transition: transform 0.15s ease, border 0.15s ease, box-shadow 0.15s ease, background 0.15s ease;
}

.seg-btn:hover {
  transform: translateY(-1px);
  border: 1px solid rgba(0, 184, 40, 0.75);
  box-shadow: 0 6px 14px rgba(0, 184, 40, 0.14);
}

.seg-btn:focus-visible {
  outline: none;
  border: 1px solid rgba(0, 184, 40, 0.95);
  box-shadow: 0 0 0 3px rgba(0, 184, 40, 0.16);
}

.seg-btn--active {
  border: 1px solid rgba(0, 184, 40, 0.95);
  background: rgba(0, 184, 40, 0.18);
}

.refresh-btn {
  height: 32px;
  padding: 0 12px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.28);
  background: linear-gradient(135deg, rgba(108, 171, 106, 0.22), rgba(42, 184, 73, 0.35));
  color: #fff;
  cursor: pointer;
  transition: transform 0.15s ease, border 0.15s ease, box-shadow 0.15s ease, background 0.15s ease;
}

.refresh-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  border: 1px solid rgba(0, 184, 40, 0.85);
  box-shadow: 0 6px 14px rgba(0, 184, 40, 0.18);
}

.refresh-btn:focus-visible {
  outline: none;
  border: 1px solid rgba(0, 184, 40, 0.95);
  box-shadow: 0 0 0 3px rgba(0, 184, 40, 0.16);
}

.refresh-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.recent-meta {
  margin-top: 10px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.meta-pill {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(0, 184, 40, 0.16);
  border: 1px solid rgba(0, 184, 40, 0.45);
  color: #dfffe5;
  font-size: 12px;
}

.meta-muted {
  font-size: 12px;
  color: #c8c8c8;
}

.meta-error {
  font-size: 12px;
  color: #ffb4b4;
}

.empty-state {
  margin-top: 18px;
  padding: 18px 0 4px;
  text-align: center;
  color: #cfcfcf;
}

.recent-list {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.recent-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 10px;
  text-decoration: none;
  color: inherit;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: linear-gradient(135deg, rgba(108, 171, 106, 0.10), rgba(255, 255, 255, 0.04));
  transition: transform 0.15s ease, border 0.15s ease, box-shadow 0.15s ease;
}

.recent-item:hover {
  transform: translateY(-1px);
  border: 1px solid rgba(0, 184, 40, 0.75);
  box-shadow: 0 6px 14px rgba(0, 184, 40, 0.18);
}

.recent-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid rgba(255, 255, 255, 0.25);
  flex: 0 0 auto;
}

.recent-body {
  overflow: hidden;
  flex: 1;
}

.recent-top {
  display: flex;
  gap: 10px;
  align-items: baseline;
  justify-content: space-between;
}

.recent-nick {
  font-weight: 700;
  color: #fff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 60%;
}

.recent-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.75);
  white-space: nowrap;
  flex: 0 0 auto;
}

.recent-url {
  margin-top: 2px;
  font-size: 12px;
  color: rgba(122, 245, 143, 0.9);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.reply-tag {
  margin-left: 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.recent-text {
  margin-top: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.4;
  display: -webkit-box;
  line-clamp: 3;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tree-groups {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.tree-group {
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: linear-gradient(135deg, rgba(108, 171, 106, 0.08), rgba(255, 255, 255, 0.02));
  padding: 12px;
}

.group-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 10px;
}

.group-link {
  color: rgba(122, 245, 143, 0.95);
  text-decoration: none;
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.group-link:hover {
  text-decoration: underline;
}

.group-count {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  white-space: nowrap;
}

.group-empty {
  margin-top: 10px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
}

.tree-list {
  margin-top: 10px;
}

.node {
  padding: 8px 0;
  margin-left: min(calc(var(--depth, 0) * var(--indent-step, 16px)), 64px);
}

.node-row {
  display: flex;
  gap: 10px;
}

.node-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid rgba(255, 255, 255, 0.22);
  flex: 0 0 auto;
}

.node-body {
  flex: 1;
  overflow: hidden;
}

.node-top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: baseline;
}

.node-nick {
  font-weight: 700;
  color: #fff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 70%;
}

.node-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.75);
  white-space: nowrap;
}

.node-meta {
  margin-top: 2px;
}

.node-link {
  font-size: 12px;
  color: rgba(122, 245, 143, 0.92);
  text-decoration: none;
  display: block;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.node-link:hover {
  text-decoration: underline;
}

.node-text {
  margin-top: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.4;
  white-space: pre-wrap;
  word-break: break-word;
}

.node-children {
  margin-top: 6px;
  padding-left: 10px;
  border-left: 2px solid rgba(0, 184, 40, 0.25);
}

#tcomment {
  color: white;
}

/* Twikoo：用 :deep 覆盖内部样式，贴合站点黑金/绿色调 */
.twikoo-wrap :deep(.tk-comments) {
  color: rgba(255, 255, 255, 0.88);
}

.twikoo-wrap :deep(.tk-submit) {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 12px;
  padding: 14px 14px;
}

.twikoo-wrap :deep(.tk-submit .tk-row) {
  flex-wrap: wrap;
  gap: 10px;
}

.twikoo-wrap :deep(.tk-preview) {
  overflow-wrap: anywhere;
  word-break: break-word;
}

.twikoo-wrap :deep(.tk-avatar),
.twikoo-wrap :deep(.tk-avatar img) {
  border-radius: 50%;
}

.twikoo-wrap :deep(textarea),
.twikoo-wrap :deep(input),
.twikoo-wrap :deep(.tk-input) {
  color: #fff;
  background: rgba(0, 0, 0, 0.35);
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 10px;
  outline: none;
}

.twikoo-wrap :deep(textarea:focus),
.twikoo-wrap :deep(input:focus),
.twikoo-wrap :deep(.tk-input:focus) {
  border: 1px solid rgba(0, 184, 40, 0.9);
  box-shadow: 0 0 0 3px rgba(0, 184, 40, 0.16);
}

.twikoo-wrap :deep(.tk-row.actions),
.twikoo-wrap :deep(.tk-row.actions > *) {
  color: rgba(255, 255, 255, 0.85);
}

.twikoo-wrap :deep(.tk-send),
.twikoo-wrap :deep(button),
.twikoo-wrap :deep(.tk-button) {
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  background: linear-gradient(135deg, rgba(108, 171, 106, 0.22), rgba(42, 184, 73, 0.35));
  color: #fff;
  transition: transform 0.15s ease, border 0.15s ease, box-shadow 0.15s ease;
}

.twikoo-wrap :deep(.tk-send:hover),
.twikoo-wrap :deep(button:hover),
.twikoo-wrap :deep(.tk-button:hover) {
  transform: translateY(-1px);
  border: 1px solid rgba(0, 184, 40, 0.85);
  box-shadow: 0 6px 14px rgba(0, 184, 40, 0.18);
}

.twikoo-wrap :deep(.tk-comment) {
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: rgba(255, 255, 255, 0.03);
  padding: 12px 12px;
}

.twikoo-wrap :deep(.tk-content) {
  color: rgba(255, 255, 255, 0.86);
}

.twikoo-wrap :deep(.tk-nick) {
  color: #fff;
  font-weight: 700;
}

.twikoo-wrap :deep(.tk-time) {
  color: rgba(255, 255, 255, 0.65);
  font-size: 12px;
}

.twikoo-wrap :deep(.tk-action) {
  color: rgba(122, 245, 143, 0.92);
}

.twikoo-wrap :deep(.tk-action:hover) {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .comments {
    padding: 16px;
    --indent-step: 14px;
  }

  .comments-title-gradient {
    font-size: 38px;
  }

  .comment-card {
    padding: 16px 14px;
  }
}

@media (max-width: 520px) {
  .comments-tagline {
    font-size: 18px;
    letter-spacing: 3px;
  }

  .comments-title-gradient {
    font-size: 34px;
    letter-spacing: 4px;
  }

  .comments-subtitle {
    margin-bottom: 14px;
    font-size: 13px;
    line-height: 1.6;
  }

  .card-header {
    align-items: flex-start;
  }

  .card-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .seg-btn {
    flex: 1 1 auto;
  }

  .refresh-btn {
    flex: 1 1 100%;
  }

  .recent-item {
    padding: 12px 10px;
    gap: 10px;
  }

  .recent-avatar {
    width: 34px;
    height: 34px;
  }

  .recent-top {
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
  }

  .recent-nick {
    max-width: 100%;
  }

  .recent-url {
    white-space: normal;
    overflow: visible;
    text-overflow: initial;
    word-break: break-word;
  }

  .reply-tag {
    display: inline-block;
    margin-left: 0;
    margin-top: 4px;
  }

  .node-top {
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
  }

  .node-nick {
    max-width: 100%;
  }

  .group-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .group-link {
    white-space: normal;
    word-break: break-word;
  }

  .node-children {
    padding-left: 8px;
  }

  .comments {
    --indent-step: 12px;
  }

  .node {
    margin-left: min(calc(var(--depth, 0) * var(--indent-step, 12px)), 36px);
  }

  .twikoo-wrap :deep(.tk-submit) {
    padding: 12px 12px;
  }

  .twikoo-wrap :deep(.tk-submit .tk-row .tk-col),
  .twikoo-wrap :deep(.tk-submit .tk-row .tk-col-lg),
  .twikoo-wrap :deep(.tk-submit .tk-row .tk-col-md),
  .twikoo-wrap :deep(.tk-submit .tk-row .tk-col-sm),
  .twikoo-wrap :deep(.tk-submit .tk-row .tk-col-xs) {
    flex: 1 1 100%;
    width: 100%;
    max-width: 100%;
  }

  .twikoo-wrap :deep(.tk-submit .tk-row .tk-col + .tk-col) {
    margin-left: 0;
  }

  .twikoo-wrap :deep(.tk-send),
  .twikoo-wrap :deep(.tk-button) {
    width: 100%;
  }
}

@media (max-width: 360px) {
  .comments {
    padding: 12px;
  }

  .comment-card {
    padding: 14px 12px;
  }

  .comments-title-gradient {
    font-size: 30px;
    letter-spacing: 3px;
  }
}
</style>