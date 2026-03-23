

# 阿叶Ayeez的博客

- [中文](./README.md) | [English](./docs/README_EN.md)

![](https://qiniu.ayeez.cn/20260228215441383.jpg)


项目demo：[https://blog.ayeez.cn/](https://blog.ayeez.cn/)


<p align="center">
    <a href="https://github.com/Ayeez757/AyeezBlog/blob/master/AyeezBlog-Frontend/package.json">
    <img alt="项目版本" src="https://img.shields.io/github/package-json/v/ayeez757/AyeezBlog?filename=AyeezBlog-Frontend%2Fpackage.json&label=AyeezBlog%20version">
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
| **后端**          | Java21 + Spring Boot 3.2.0 + Spring Security + JWT + MyBatis-Plus + Redis | 业务逻辑与数据接口  |
| **数据库**         | MySQL                                                                     | 持久化存储      |
| **部署**          | Docker + Docker Compose + Nginx                                           | 容器化部署，反向代理 |
| **CI/CD**       | GitHub Actions                                                            | 自动化测试与构建   |
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
- **分类管理**：分类列表查询、新增、编辑、删除，并支持查看分类下文章与快速跳转编辑
- **标签管理**：标签列表查询、新增、编辑、删除，并支持查看标签下文章与快速跳转编辑
- **后台首页**：已预留首页入口，可继续扩展统计看板

### 后端能力

- **文章接口体系**：公共文章列表/详情接口 + 管理端文章增删改查完整接口
- **分类与标签接口体系**：分类与标签均支持增删改查，并提供分类下文章查询、标签下文章查询接口
- **认证与安全**：管理员登录认证并返回登录信息/token，基于 Spring Security + JWT + Token 过滤器实现安全基础能力
- **统一数据返回**：`Result` 统一响应结构与 `PageResult` 标准分页结构
- **数据有效性与事务**：分类/标签写操作含基础参数判空与重名校验，并通过事务保证写入一致性
- **后端基础设施**：CORS 跨域支持、MyBatis-Plus + Mapper XML 数据访问

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

  

#### 2. 初始化数据库

  

- 创建数据库：`ayeezblog`（字符集建议 `utf8mb4`）。

- 执行项目根目录建表脚本：`AyeezBlog建表.sql`。

  

#### 3. 启动后端（blog-server）

  

后端默认读取 `hm.db.host` 和 `hm.db.password`，本地开发可直接在启动命令传入：

  

```bash

cd AyeezBlog-Backend

mvn clean install

cd blog-server

mvn spring-boot:run "-Dspring-boot.run.jvmArguments=-Dhm.db.host=localhost -Dhm.db.password=你的数据库密码"

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

说明：项目已在 `vite.config.js` 中将 `/post`、`/logs` 代理到 `http://localhost:8080`，无需手动改 `src/api/index.js`。

  

#### 5. 启动管理端（AyeezBlog-AdminPanel）

新建控制台，回到项目根目录，执行如下指令：

```bash

cd AyeezBlog-AdminPanel

npm install

npm run dev

```

  

访问：`http://localhost:5173/admin/`（如与前台同时运行，请指定其他端口，例如 `npm run dev -- --port 5174`）

默认用户：admin

密码：admin
  
  
---

## 配置说明

### 后端配置 (application.yml / application-dev.yml / application-local.yml)

后端通过 `hm.db.host` 和 `hm.db.password` 组装数据库连接（`spring.datasource.url`），其中密码推荐使用环境变量传入（避免仓库内明文）。

| 配置项 | 说明 | 示例值 | 部署必改 |
| --- | --- | --- | --- |
| `hm.db.host` | MySQL 主机名/IP（用于拼接 `spring.datasource.url`） | `mysql` 或 `localhost` | ✅ |
| `hm.db.password` | MySQL 密码 | `${HM_DB_PASSWORD}` | ✅ |
| `spring.datasource.url` | JDBC 数据库连接地址 | `jdbc:mysql://localhost:3306/ayeezblog` | ✅ |
| `spring.datasource.username` | 数据库用户名 | `root` | ✅ |
| `spring.datasource.password` | 数据库密码（来自 `${hm.db.password}`） | `${hm.db.password}` | ✅ |
| `server.port` | 后端服务端口 | `8080` | 按需 |
| `spring.servlet.multipart.max-file-size` | 单个文件上传上限 | `10MB` | 按需 |
| `spring.servlet.multipart.max-request-size` | 单次请求上传上限 | `100MB` | 按需 |
| `aliyun.oss.endpoint` | OSS 地域节点 | `https://oss-cn-beijing.aliyuncs.com` | ✅ |
| `aliyun.oss.bucketName` | OSS Bucket 名称 | `javaweb-ayeez` | ✅ |
| `aliyun.oss.region` | OSS 地域 | `cn-beijing` | ✅ |






---

## API 文档

Apifox：

- 开发环境：[https://tix3ut2jpw.apifox.cn](https://tix3ut2jpw.apifox.cn)

---

## 数据库设计

数据库设计已拆分到独立文档，点击查看：

- [数据库设计文档](./docs/DATABASE_DESIGN.md)

---

## CI/CD

项目使用 GitHub Actions 自动执行以下流程：

1. **代码推送**：触发单元测试、代码风格检查。
2. **构建镜像**：通过 Dockerfile 构建后端和前端镜像，并推送到 Docker Hub 或私有仓库。
3. **部署**：利用 SSH 登录服务器，拉取新镜像并重启服务。

工作流配置文件位于 `.github/workflows/`目录下。

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
- 博客：旧博客 [https://blog.ayeez.cn](https://blog.ayeez.cn) 或 新博客（本项目） [https://dev.ayeez.cn](https://dev.ayeez.cn)
- GitHub Issues： [https://github.com/ayeez757/AyeezBlog/issues](https://github.com/ayeez757/AyeezBlog/issues)
- QQ交流群（不仅限于本博客，欢迎加入）：421300955

---

*最后更新：2026-03-20