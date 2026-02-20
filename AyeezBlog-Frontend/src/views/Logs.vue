<template>
  <div class="logs">
    <div class="cards">


      <h1 class="title">网站更新日志</h1>
      <div class="timeline">
        <!-- 日志项 -->
        <div class="log-item" v-for="(log, index) in logs" :key="index">
          <div class="date">{{ log.date }}</div>
          <div class="content">
            <h2>{{ log.version }}</h2>
            <ul>
              <li v-for="(change, i) in log.changes" :key="i">{{ change }}</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Logs',
  data() {
    return {
      logs: [
        {
          date: '2026-02-20',
          version: 'v0.2.0',
          changes: [
            '前端：搭建了前端开发环境',
            '前端：完成了页头和导航栏的开发，写好了页面路由',
            '前端：定下了整体风格，做了张背景图',
            '前端：修改并固定了拖动条的样式',
            '前端：弄了个临时的日志页面',
          ]
        },
        {
          date: '2026-02-19',
          version: 'v0.1.0',
          changes: [
            '后端：搭建了后端开发环境',
            '后端：完成了分页查询文章列表的接口，跑通了程序，修改了部分配置'
          ]
        }
      ]
    };
  }
};
</script>

<style scoped>
/* 页面整体样式 */
.logs {
  position: absolute;
  width: 100%;
  top: 68px;
  padding: 20px;
  color: white;
  font-family: 'Arial', sans-serif;
  background: rgba(0, 0, 0, 0.7);
  /* 半透明黑色背景 */
  min-height: calc(100vh - 68px);
  box-sizing: border-box;
}

.cards {
  margin: 0 auto;
  max-width: 1000px;
}

.title {
  text-align: center;
  margin-bottom: 30px;
  font-size: 2em;
  color: #fff;
}

/* 时间线容器 */
.timeline {
  position: relative;
  /* max-width: 800px; */
  margin: 0 auto;
}

/* 时间线竖线 */
.timeline::before {
  content: '';
  position: absolute;
  left: 10%;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #ccc;
  transform: translateX(-50%);
}

/* 日志项样式 */
.log-item {
  position: relative;
  margin-bottom: 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.date {
  flex: 1;
  text-align: right;
  padding-right: 20px;
  font-weight: bold;
  color: #ffcc00;
  /* 黄金色日期 */
}

.content {
  flex: 3;
  background: rgba(255, 255, 255, 0.1);
  padding: 15px;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s ease;
}

.content:hover {
  transform: translateY(-5px);
  /* 悬停上浮效果 */
}

.content h2 {
  margin-top: 0;
  color: #fff;
  font-size: 1.5em;
}

.content ul {
  list-style-type: none;
  padding-left: 0;
}

.content li {
  margin: 8px 0;
  color: #ddd;
  position: relative;
  padding-left: 20px;
}

.content li::before {
  content: '•';
  color: #ffcc00;
  position: absolute;
  left: 0;
}
</style>