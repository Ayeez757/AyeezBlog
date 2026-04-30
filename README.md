


# 阿叶Ayeez的博客

- [中文](./README.md) | [English](./docs/README_EN.md)

## 考核材料（热重载）

- 考核题目文档：[第三轮后端考核说明（热重载技术方向）](https://github.com/gduf-cs-tribe/2025-backend-recruit-03/blob/main/docs/level3/arceca.md)
- 本项目考核交付文档：[配置文件热重载考核交付文档](./docs/配置文件热重载考核交付文档.md)

> 评审提示：请以“本地运行 + 本地热重载复现”为准；线上 CI/CD + Docker 已验证但不要求复现（流程依赖评审侧环境）。


![](https://qiniu.ayeez.cn/20260228215441383.jpg)


本博客链接：[https://blog.ayeez.cn](https://blog.ayeez.cn)  
旧博客链接（已停止维护）：[https://butterfly.ayeez.cn](https://butterfly.ayeez.cn)


<p align="center">
    <!-- 版本：shields 拉取线上 /api/logs/current，与站内「当前日志版本」一致（非 package.json） -->
    <a href="https://dev-blog.ayeez.cn/logs/">
    <img alt="项目版本" src="https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fblog.ayeez.cn%2Fapi%2Flogs%2Fcurrent&query=%24.data.version&label=AyeezBlog%20version">
  </a>
  <a href="https://github.com/ayeez757/AyeezBlog/blob/master/LICENSE"><img alt="许可证" src="https://img.shields.io/github/license/ayeez757/AyeezBlog"></a>
  <!-- Spring Boot 版本（从 parent.version 手动获取） -->
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.2.0-6DB33F?logo=spring">
  <!-- Java 版本（从 properties.java.version 手动获取） -->
  <img alt="Java" src="https://img.shields.io/badge/Java-21-007396?logo=java">
</p>








## 项目简介

**AyeezBlog** 是一个开源的博客系统，采用前后端分离架构设计。前台基于 Vue 3 构建，聚焦阅读体验与内容展示；管理端面向内容管理场景，便于文章、分类、标签等信息的统一维护；后端基于 Spring Boot 提供稳定、清晰的 RESTful API 能力，兼顾扩展性与可维护性。

项目以“内容创作 + 阅读互动 + 后台管理”作为核心方向，支持 Markdown 文章体系、评论互动、分类标签组织、归档与友链等博客常用能力。整体技术栈覆盖前端工程化、后端安全与数据访问、缓存、容器化部署和自动化流程，适用于个人博客、技术社区与中小型内容站点的搭建和二次开发。

核心页面功能：

- 首页（文章流）
- 文章详情页（Markdown）
- 归档页
- 留言页
- 友链页
- 更新日志页

---

## 技术栈选用


| 层次              | 技术                                                                        | 说明         |
| --------------- | ------------------------------------------------------------------------- | ---------- |
| **前端**          | Vue 3 + Vite + Vue Router + Axios                                         | 前台展示界面     |
| **管理端**         | Vue 3 + Element Plus + ECharts                                            | 后台管理界面     |
| **后端**          | Java 21 + Spring Boot 3.2.0 + Spring Security + JWT + MyBatis + Spring AI（可选 DeepSeek）+ Redis | 业务逻辑与数据接口  |
| **数据库**         | MySQL                                                                     | 持久化存储      |
| **部署**          | Docker + Docker Compose + Nginx                                           | 容器化部署，反向代理 |
| **服务器（当前实际部署）** | ubantu22.04                                                               | 服务器系统      |
| **其他第三方工具**     | twikoo（评论）                                                                |            |


---

## 功能特性

### 前台展示

- **首页与文章流**：公告卡片、社交链接、文章卡片流展示；文章按更新时间分页查询并支持上一页/下一页切换
- **文章详情阅读体验**：按文章 ID 路由访问（后端随机短链接）、Markdown 正文渲染、Front Matter 解析、`highlight.js` 代码高亮、代码块语言标识与一键复制
- **目录与快速导航**：自动提取标题生成 TOC、目录按层级折叠/展开、目录锚点平滑跳转、悬浮按钮支持回顶/跳评论/开关目录
- **评论体系**：Twikoo 文章评论（按路径隔离）+ 独立留言页（`/comments`）+ 全站评论聚合（文章/留言/友链）+ 最新流与按页面树形两种查看模式
- **归档体系**：时间轴浏览全部文章，支持关键词搜索、年份/月筛选、正序/倒序排序
- **社交与内容页**：友链页分组卡片展示与友链留言区、网站更新日志时间线、关于页与朋友圈页预留入口（含旧站跳转）
- **多端适配**：首页、文章页、留言页、日志页完成移动端响应式优化

### 后台管理

- **登录与访问控制**：后台登录鉴权、token 本地持久化、路由守卫未登录自动跳转登录页
- **文章管理全流程**：文章列表分页与关键词搜索、文章新增/编辑/删除、按 ID 回显详情并保存修改
- **写作与解析能力**：写作表单支持标题/描述/封面/短链/日期/更新时间，Markdown 编辑区与预览区同屏，自动解析 Front Matter（标题/标签/分类/日期等）
- **AI 文章简介（可选）**：描述区域提供「根据正文生成简介」按钮；在后端开启开关且配置 DeepSeek API Key 后，保存文章时若描述为空可自动生成简介（不覆盖已有描述）
- **AI 文章封面（可选）**：封面区域提供「AI 生成封面（即梦）」；使用火山引擎即梦（智能视觉 CVProcess）按统一风格出图后**转存七牛**，自动填入封面链接（需配置火山 Access Key/Secret、开启开关且七牛可用）
- **分类管理**：分类列表查询、新增、编辑、删除，并支持查看分类下文章与快速跳转编辑
- **标签管理**：标签列表查询、新增、编辑、删除，并支持查看标签下文章与快速跳转编辑
- **后台首页**：已预留首页入口，可继续扩展统计看板

### 后端能力

- **文章接口体系**：公共文章列表/详情接口 + 管理端文章增删改查完整接口
- **分类与标签接口体系**：分类与标签均支持增删改查，并提供分类下文章查询、标签下文章查询接口
- **认证与安全**：管理员登录认证并返回登录信息/token，基于 Spring Security + JWT + Token 过滤器实现安全基础能力
- **统一数据返回**：`Result` 统一响应结构与 `PageResult` 标准分页结构
- **数据有效性与事务**：分类/标签写操作含基础参数判空与重名校验，并通过事务保证写入一致性
- **后端基础设施**：CORS 跨域支持、MyBatis + Mapper XML 数据访问
- **AI 与 Spring AI**：集成 `spring-ai-starter-model-openai`，通过 DeepSeek 官方 OpenAI 兼容接口（`https://api.deepseek.com`）生成文章描述；管理端 `POST /admin/ai/article-description`；与文章新增/更新流程可选联动
- **AI 封面（即梦）**：调用火山引擎即梦文生图接口生成图片，下载后服务端上传七牛；管理端 `POST /admin/ai/article-cover`，返回持久 `coverUrl`

---

## 系统架构

- **客户端**：浏览器通过 HTTP/HTTPS 访问 Nginx。
- **Nginx**：分发静态资源，代理 API 请求到后端容器，支持 Gzip 压缩和 SSL 终止。
- **后端服务**：Spring Boot 应用提供 RESTful API，连接 MySQL 和 Redis。
- **数据库**：MySQL 存储业务数据，Redis 缓存热点数据（如文章详情、用户会话）。
- **文件存储**：图片等静态资源可存储在本地或云对象存储。

---

## 快速开始

  

### 环境要求

  

- **Node.js** 18+

- **JDK** 17+（推荐 21）

- **Maven** 3.6+

- **MySQL** 8.0+

- **Git**

  

### 开发环境搭建

  

首先star

  

#### 1. 克隆代码

  

```bash

git clone https://github.com/Ayeez757/AyeezBlog.git

cd AyeezBlog

```

若你正在进行**第三轮后端考核（热重载方向）**，请切换到考核分支后再继续后续步骤：

```bash
git checkout 25-backendRecruit3-hotReload
```


  

#### 2. 初始化数据库

  

- 创建数据库：`ayeezblog`（字符集建议 `utf8mb4`）。

- 执行项目根目录建表脚本：`AyeezBlog建表.sql`。

  

#### 3. 启动后端（blog-server）

先配置数据库连接再启动后端。注意：后端默认配置里 **数据库账号/密码没有默认值**，如果不配置会直接启动失败（报错类似 *Could not resolve placeholder 'hm.db.username'*）。

你可以二选一：

- **方式 A**：通过环境变量配置数据库连接（Windows PowerShell 示例）

```powershell
$env:HM_DB_HOST="localhost"
$env:HM_DB_USER="root"
$env:HM_DB_PASSWORD="你的数据库密码"
```

- **方式 B**：直接修改 `AyeezBlog-Backend/blog-server/src/main/resources/application.yml`，把 `hm.db.host / hm.db.username / hm.db.password` 填成你的本地配置。

说明：

- **七牛（qiniu）配置**：仅在后台需要“获取上传 token/AI 生成封面后转存七牛”等功能时才需要；只跑文章/分类/标签等基础功能可以不配。

**可选 — AI**：若需 DeepSeek 生成描述，请配置 `hm.deepseek.api-key` 与 `hm.deepseek.summary-enabled=true`；若需即梦生成封面，请配置 `hm.volcengine.access-key`、`hm.volcengine.secret-key` 与 `hm.volcengine.cover-enabled=true`，并保证七牛已配置（详见下文「配置说明」）。未配置时其余功能不受影响。

```bash
cd AyeezBlog-Backend
mvn clean install
cd blog-server
mvn spring-boot:run
```

  

默认 API 地址：`http://localhost:8080`

  

#### 4. 启动前台（AyeezBlog-Frontend）

新建控制台，回到项目根目录，执行如下指令：

```bash

cd AyeezBlog-Frontend

npm install

npm run dev

```

  

访问：`http://localhost:5173`  

说明：项目已在 `vite.config.js` 中配置本地开发代理（例如 `/api`、`/post`、`/logs`、`/links/list`、`/twikoo-proxy` → `http://localhost:8080` 或第三方服务），无需手动改请求地址。

  

#### 5. 启动管理端（AyeezBlog-AdminPanel）

新建控制台，回到项目根目录，执行如下指令：

```bash

cd AyeezBlog-AdminPanel

npm install

npm run dev

```

  

访问：`http://localhost:5173/admin/`

端口提示：

- 管理端在 `vite.config.js` 中固定了 `5173`，而前台 Vite 默认也会用 `5173`，**两者同时启动会端口冲突**。
- 建议同时运行时把 **前台** 换端口（例如 `npm run dev -- --port 5174`），或把管理端 `vite.config.js` 的 `server.port` 改成其他端口。

默认用户：admin

密码：admin
  
  
---

## 配置说明

### 后端配置 (application.yml)

配置说明仅保留关键项；更完整的填写示例、可选项与环境变量映射已写入 `AyeezBlog-Backend/blog-server/src/main/resources/application.yml` 的注释中（建议直接打开该文件照注释填）。

| 配置项 | 说明 |
| --- | --- |
| `hm.db.host` / `hm.db.username` / `hm.db.password` | MySQL 连接信息（必填，否则后端无法启动） |
| `hm.db.port` | MySQL 端口（默认 3306，可不填） |
| `server.port` | 后端端口（默认 8080） |
| `RUNTIME_CONFIG_PATH` 或 `-Druntime.config.path` | 运行时热重载配置文件路径（Docker 推荐设置为 `/app/config/runtime-config.yml`） |
| `qiniu.*` | 七牛配置（仅在管理端需要“获取上传 token”/AI 封面转存时必填；不使用可不配） |
| `hm.deepseek.*` | 简介生成（可选） |
| `hm.volcengine.*` | 封面生成（可选；通常还需要七牛可用） |

### 热重载相关配置文件说明

| 文件 | 场景 | 作用 |
| --- | --- | --- |
| `AyeezBlog-Backend/blog-server/src/main/resources/runtime-config.yml` | 本地开发 | 本地运行时热重载配置文件（IDE / `mvn spring-boot:run` 常用） |
| `deploy/prod/runtime-config.yml` | Docker 线上 | 线上外置热重载配置文件，挂载到容器 `/app/config/runtime-config.yml` |
| `deploy/prod/.env` | Docker 线上 | Compose 运行时环境变量（数据库、密钥、`RUNTIME_CONFIG_PATH` 等） |
| `deploy/prod/docker-compose.yml` | Docker 线上 | 定义容器编排、runtime-config 文件挂载、后端环境变量注入 |
| `.github/workflows/cicd-deploy.yml` | CI/CD | 仅改 runtime-config 时只同步文件并调用重载接口，不重启后端容器 |

说明：

- 后端优先读取 `-Druntime.config.path`，其次读取 `RUNTIME_CONFIG_PATH`；
- Docker 推荐固定使用 `RUNTIME_CONFIG_PATH=/app/config/runtime-config.yml`；
- 若只变更 `deploy/prod/runtime-config.yml`，CI/CD 会执行“上传配置 + 调用重载接口”而非 `compose up`。





---

## 线上部署（Docker Compose）

面向开源用户的线上部署文档请查看：

- [Docker Compose 线上部署指南](./docs/Docker-Compose线上部署指南.md)

热重载配置文件（Docker）：

- 线上 compose 默认挂载 `deploy/prod/runtime-config.yml` 到容器内 `/app/config/runtime-config.yml`；
- 后端通过环境变量 `RUNTIME_CONFIG_PATH=/app/config/runtime-config.yml` 读取并监听该文件；
- 修改 `deploy/prod/runtime-config.yml` 后，容器内可自动触发配置热重载（无需重启后端进程）。

该文档包含从 0 到 1 的完整步骤：

- 服务器准备
- 代码上传
- `.env` 配置
- 一键 `docker compose up -d --build`
- 上线验证与常见故障排查

---

## API 文档

Apifox：

- 开发环境：[https://tix3ut2jpw.apifox.cn](https://tix3ut2jpw.apifox.cn)

---

## 数据库设计

数据库设计已拆分到独立文档，点击查看：

- [数据库设计文档](./docs/数据库设计.md)


---

## 贡献指南

我们欢迎任何形式的贡献，包括但不限于：

- 报告 Bug
- 提交功能需求
- 代码优化
- 文档完善勘误

### 开发流程

1. Fork 项目并克隆到本地。
2. 创建新分支：`git checkout -b feature/your-feature`
3. 提交 Pull Request，描述清楚改动内容和测试情况。

### 代码规范

- 前端：遵循 Vue 3 官方风格指南。
- 后端：阿里巴巴编码规范

---

## 更新日志

请参阅
博客内日志： [https://dev-blog.ayeez.cn/logs/](https://dev-blog.ayeez.cn/logs/)
GitHub的activity记录：[Activity · Ayeez757/AyeezBlog](https://github.com/Ayeez757/AyeezBlog/activity)

---

## 许可证

本项目基于 [Apache License 2.0 许可证](LICENSE) 开源，这意味着您可以自由使用、修改和分发，但需保留原版权声明。

---

## 联系方式

- 作者：[阿叶Ayeez]
- 邮箱：[[3406608593@qq.com](mailto:3406608593@qq.com)]
- 博客：本博客 [https://blog.ayeez.cn](https://blog.ayeez.cn)；旧博客（已停止维护） [https://butterfly.ayeez.cn](https://butterfly.ayeez.cn)
- GitHub Issues： [https://github.com/ayeez757/AyeezBlog/issues](https://github.com/ayeez757/AyeezBlog/issues)
- QQ交流群（不仅限于本博客，欢迎加入）：421300955

---

*最后更新：2026-04-25


[![Star History Chart](https://api.star-history.com/chart?repos=ayeez757/ayeezblog&type=date&legend=top-left)](https://www.star-history.com/?repos=ayeez757%2Fayeezblog&type=date&legend=top-left)
