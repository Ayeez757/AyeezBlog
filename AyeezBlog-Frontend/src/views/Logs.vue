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
          date: '2026-02-26',
          version: 'v0.7.0',
          changes: [
            '运维：初次将前后端服务以及数据库用docker部署到服务器上，前端用的nginx反向代理，编写了dockerfile文件',
          ]
        },{
          date: '2026-02-24',
          version: 'v0.6.0',
          changes: [
            '创建了管理端vue项目'
          ]
        },{
          date: '2026-02-23',
          version: 'v0.5.0',
          changes: [
            '前端：加了文章页面，根据旧博客路径路由文章，并渲染markdown内容（markdown-it）',
            '前端：修改了文章页面的样式，能够解析markdown中的图片，并调整了图片的样式，解析了代码块并加上了高亮(highlight.js)，解析了frontMatter并展示了标题、标签、分类、描述等信息(front-matter)',
            '前端：给文章页面加上了目录（toc），并且在文章内容中添加了锚点链接',
            '后端：给所有接口加上slf4j日志输出',
          ]
        },{
          date: '2026-02-22',
          version: 'v0.4.0',
          changes: [
            '后端：分页查询增加了文章描述',
            '前端：调整了文章卡片样式',
            '前端：新增了仿Linux启动的预加载动画，调整了首页标题动画的时机和样式',
          ]
        },
        {
          date: '2026-02-21',
          version: 'v0.3.0',
          changes: [
            '前端：首页加了一些条纹装饰',
            '前端：首页加了用户卡片，里面有头像、公告和社交链接',
            '前端：抽出了一个api模块，封装了请求函数',
            '前端：首页动态分页查询文章列表并以卡片展示，跑通了前后端',
            '前端：后端加了cors配置',
          ]
        },
        {
          date: '2026-02-20',
          version: 'v0.2.0',
          changes: [
            '前端：搭建了前端开发环境',
            '前端：完成了页头和导航栏的开发，写好了页面路由',
            '前端：定下了整体风格，做了张背景图',
            '前端：修改并固定了拖动条的样式',
            '前端：弄了个临时的日志页面',
            '前端：首页加上了酷炫的标题',
            '前端：首页加上了炫酷的引导线',
          ]
        },
        {
          date: '2026-02-19',
          version: 'v0.1.0',
          changes: [
            '后端：搭建了后端开发环境',
            '后端：完成了分页查询文章列表的接口，跑通了程序，修改了部分配置',
            '数据库：创建了数据库、博客文章表和用户表（没啥用）'
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