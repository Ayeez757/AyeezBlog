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
          date: '2026-03-23',
          version: 'v0.17.0',
          changes: [
            '修复了一些站点迁移后遗留的问题'
          ]
        },
      {
          date: '2026-03-22',
          version: 'v0.16.0',
          changes: [
            '部署了管理端，并且添加了workfile，让管理端也自动化部署',
            '管理端：加上了淡淡的背景图（测试工作流是否成功）'
          ]
        },
      {
          date: '2026-03-21',
          version: 'v0.15.0',
          changes: [
            '测试了readme快速开始，修复了很多问题，并且删除了docker部分'
          ]
        },
      {
          date: '2026-03-20',
          version: 'v0.14.0',
          changes: [
            '前端：制作了about页，包含个人简介，社交账号，技术栈，追番列表',
          '前端：修复了进入文章页面总是跳转到中间的问题',
            '管理端：添加了分类管理和标签管理的功能',
            '管理端：重写了管理端页面的样式',
            '修改了SQL建表脚本',
            '大量更新了README.md,完善了文档内容，新增了数据库设计说明、英文文档'
          
          ]
        },
      {
          date: '2026-03-19',
          version: 'v0.13.0',
          changes: [
            '文章与评论迁移完成！',
            '前端：添加了文章归档页面，支持搜索、按日期筛选与排序。',
            '前端：做了文章日志页面的手机适配',
            '前端：重做了留言页，添加了全站评论汇总功能，支持最新评论、按页面分类浏览',
            '前端：修复了评论页面的手机适配的一系列问题',
          ]
        },
      {
          date: '2026-03-16',
          version: 'v0.12.0',
          changes: [
            '管理端：添加了解析分类和标签并添加到数据库的功能',
            '管理端：添加了编辑文章的功能',
            '前端：文章页面的代码块样式优化', 
            '前端：文章页面增加了评论、目录、跳转至顶部的悬浮球', 
            '前端：文章页面做了手机适配',
            '前端：迁移了旧站的友链以及部分评论数据',
            '前端：分页查询文章按照了更新顺序排序',
            '前端：调整了首页样式',
          ]
        },
                {
          date: '2026-03-15',
          version: 'v0.11.0',
          changes: [
            '前端：写了友链页面',
            '前端：添加了页脚',
            '前端：页头做了手机适配',
            '前端：首页做了手机适配',
            '前端：修复了文章目录的一系列问题',
            '管理端：修复了发布文章的一系列问题'
          ]
        },{
          date: '2026-03-09',
          version: 'v0.10.0',
          changes: [
            '管理端：完成了删除文章的功能'
          ]
        },{
          date: '2026-03-05',
          version: 'v0.9.0',
          changes: [
            '运维：设置了GitHub Actions工作流自动部署后端服务，每次push到master分支如果后端文件有变化就会自动构建并部署到服务器上',
            '管理端: 加了登录页面，完成了用户登录功能以及401页面跳转，修改了管理前端的api/index.js文件以及utils/request.js文件，允许了跨域请求',
          ]
        },{
          date: '2026-02-28',
          version: 'v0.8.1',
          changes: [
            '管理端：修复了不能解析文章日期的问题',
            '编写了README.md，添加了LICENSE文件,使用的是Apache License 2.0协议',
          ]
        },{
          date: '2026-02-27',
          version: 'v0.8.0',
          changes: [
            '运维：设置了GitHub Actions工作流自动部署前端，每次push到main分支如果前端文件有变化就会自动构建并部署到服务器上',
            '管理端：搭建了基本的页面路由，写了文章管理页面的查询文章列表以及添加文章的页面，包括编辑文章时的实时渲染以及自动解析markdwon的文章头内容',
          ]
        },{
          date: '2026-02-26',
          version: 'v0.7.0',
          changes: [
            '运维：初次将前后端服务以及数据库用docker部署到服务器上，前端用的nginx反向代理，编写了dockerfile文件',
            '前端：加上了评论功能，用了以前的twikoo，服务以前配过了，前端直接引入js文件，目前外观只建了个毛胚房',
            '前端：将文字存在了服务器本地，优化了体验',
            '前端：将网页头像换成了我的头像',
            '修复了直接访问非首页的页面会404的问题（nginx配置问题）',
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
  width: 100%;
  padding: 20px;
  color: white;
  font-family: 'Arial', sans-serif;
  background: rgba(0, 0, 0, 0.7);
  /* 半透明黑色背景 */
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

/* 移动端适配 */
@media (max-width: 768px) {
  .logs {
    padding: 14px 12px;
  }

  .title {
    margin-bottom: 18px;
    font-size: 1.55em;
  }

  .timeline {
    padding-left: 18px;
  }

  .timeline::before {
    left: 12px;
  }

  .log-item {
    flex-direction: column;
    align-items: stretch;
    justify-content: flex-start;
    gap: 10px;
    margin-bottom: 18px;
  }

  .date {
    flex: none;
    text-align: left;
    padding-right: 0;
    font-size: 0.95em;
  }

  .content {
    flex: none;
    padding: 12px;
  }

  .content h2 {
    font-size: 1.2em;
  }

  .content li {
    padding-left: 18px;
  }
}

@media (max-width: 420px) {
  .logs {
    padding: 12px 10px;
  }

  .title {
    font-size: 1.4em;
  }

  .timeline {
    padding-left: 16px;
  }

  .timeline::before {
    left: 10px;
  }
}
</style>