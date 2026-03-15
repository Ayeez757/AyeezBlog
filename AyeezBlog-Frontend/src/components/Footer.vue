<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

// 站点创建时间（本地时间）
const siteCreatedAt = new Date('2026-02-19T00:00:00');

const runningTimeText = ref('');
let timerId = null;

const updateRunningTime = () => {
  const now = new Date();
  let diffSeconds = Math.floor((now.getTime() - siteCreatedAt.getTime()) / 1000);
  if (diffSeconds < 0) diffSeconds = 0;

  const days = Math.floor(diffSeconds / (24 * 60 * 60));
  diffSeconds %= 24 * 60 * 60;
  const hours = Math.floor(diffSeconds / (60 * 60));
  diffSeconds %= 60 * 60;
  const minutes = Math.floor(diffSeconds / 60);
  const seconds = diffSeconds % 60;

  const pad = (n) => String(n).padStart(2, '0');

  runningTimeText.value = `本站已经运行 ${days} 天 ${pad(hours)} 时 ${pad(minutes)} 分 ${pad(seconds)} 秒`;
};

onMounted(() => {
  updateRunningTime();
  timerId = setInterval(updateRunningTime, 1000);
});

onUnmounted(() => {
  if (timerId) {
    clearInterval(timerId);
  }
});
</script>

<template>
  <footer class="site-footer">
    <div class="footer-content">
      <span class="footer-line">{{ runningTimeText }}</span>
      <span class="footer-line">2026-2026 by 阿叶Ayeez</span>
      <span class="footer-line">
        旧站：
        <a href="https://blog.ayeez.cn" target="_blank" rel="noopener noreferrer">
          https://blog.ayeez.cn
        </a>
      </span>
      <span class="footer-line">粤ICP备2025505813号-1</span>
    </div>
  </footer>
</template>

<style scoped>
.site-footer {
  width: 100%;
  margin-top: 40px;
  padding: 26px 16px 26px;
  background-color: rgba(0, 0, 0, 0.78);
  color: #f5f5f5;
  font-size: 13px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-top: 1px solid rgba(111, 155, 119, 0.4);
  box-shadow: 0 -4px 14px rgba(0, 0, 0, 0.65);
}

.footer-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 6px;
  text-align: center;
}

.footer-line:first-child {
  margin-top: 6px; /* 让内容整体离上边缘稍微远一点 */
}

.site-footer a {
  color: #b8ffbf;
  text-decoration: none;
}

.site-footer a:hover {
  color: #6bff7a;
  text-decoration: underline;
}

@media (max-width: 768px) {
  .site-footer {
    padding: 26px 10px 26px;
    font-size: 12px;
  }
}
</style>

