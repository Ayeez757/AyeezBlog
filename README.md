注意：开发中，目前还不足以正常使用，敬请期待......（现在的README.md看看就好）
可先插眼......

# 阿叶Ayeez的博客

项目demo：https://dev-blog.ayeez.cn/

![](https://qiniu.ayeez.cn/20260228215441383.jpg)

<!--
<p align="center">
  <a href="https://github.com/ayeez757/AyeezBlog"><img alt="GitHub 版本" src="https://img.shields.io/github/v/release/ayeez757/AyeezBlog"></a>
  <a href="https://github.com/ayeez757/AyeezBlog/blob/main/LICENSE"><img alt="许可证" src="https://img.shields.io/github/license/ayeez757/AyeezBlog"></a>
  <a href="https://github.comayeez757/AyeezBlog/actions/workflows/ci.yml"><img alt="构建状态" src="https://img.shields.io/github/actions/workflow/status/ayeez757/AyeezBlog/ci.yml?branch=main"></a>
  <a href="https://codecov.io/gh/ayeez757/AyeezBlog">
  <img alt="代码覆盖率" src="https://img.shields.io/codecov/c/github/your-username/your-blog-project"></a>-->
</p>

## 项目简介

**AyeezBlog** 是一个开源的博客系统，采用前后端分离架构设计。前端基于 Vue 3 构建；后端使用 Spring Boot 框架，确保系统的稳定与可扩展性。项目包含用户前台展示与后台管理，支持文章管理、评论互动、分类标签等核心功能，适用于个人博客、技术社区或企业官网。

通过 Docker 容器化部署，结合 Nginx 反向代理与负载均衡，可快速搭建高可用生产环境。项目遵循 RESTful API 设计规范，代码结构清晰，易于二次开发和维护。

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
- **文章列表**：分页展示，支持按分类、标签、日期归档筛选（开发中）
- **文章详情**：Markdown 渲染，代码高亮，目录自动生成 
- **评论系统**：支持嵌套评论，邮件通知（twikroo）
- **站内搜索**：基于标题、内容、标签的全文检索 （待开发）
- **RSS 订阅**：生成 Atom 格式订阅源（待开发）
- **友情链接**：申请与审核流程 （待开发）
- **日志页面**：清晰展示博客网站功能更新日志

### 后台管理
- **仪表盘**：展示文章数、评论数、访问量等统计图表（待开发）
- **文章管理**：增删改查，支持 Markdown 编辑器，草稿保存，定时发布（待开发）
- **分类与标签**：树形分类管理，标签自动补全（待开发）
- **文件管理**：图片上传（支持七牛云/阿里云 OSS），附件管理
- **数据备份**：数据库导出与恢复 （待开发）

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
- **Node.js** 18+ (本人使用 18.19.0)
- **JDK** 17+ (本人使用 21)
- **Maven** 3.6+
- **MySQL** 8.0+
- **Docker** 
- **Git**

### 开发环境搭建
首先star，再fork

#### 1. 克隆代码
```bash
git clone https://github.com/Ayeez757/AyeezBlog.git
cd AyeezBlog
```

#### 2. 后端配置与启动
- 创建 MySQL 数据库，例如 `ayeezblog`，字符集 `utf8mb4`。
- 在 `AyeezBlog-Backend/blog-server/src/main/resources/application-dev.yml`以及`application.yml` 下修改数据库连接、Redis 地址等配置。
- 在数据库执行项目根目录下的sql建表脚本。
- 启动后端：
  ```bash
  cd AyeezBlog-Backend
  mvn clean spring-boot:run -Dspring-boot.run.profiles=dev
  ```
  默认 API 地址：`http://localhost:8080`

#### 3. 前端启动（前台）
修改`AyeezBlog-Frontend/src/api/index.js`下的`const BASE_URL = '/';` 为你的后端地址（默认 API 地址：`http://localhost:8080`）
```bash
cd AyeezBlog-Frontend
npm install
npm run dev
```
访问 `http://localhost:5173`

#### 4. 管理端启动
```bash
cd admin
cp .env.example .env.development   # 修改 API 地址
npm install
npm run dev
```
访问 `http://localhost:5174`

%% #### 5. 测试账户
- 管理员：`admin@example.com` / `admin123`
- 普通用户：`user@example.com` / `user123` %%
%% 
### Docker 部署

项目根目录目前仅包含后端的Dockerfile。

#### 1. 修改配置
- 复制环境变量模板：`cp .env.example .env`，并按需修改密码、密钥等。
- 检查 Nginx 配置：`nginx/nginx.conf`，调整域名、SSL 证书路径（可选）。

#### 2. 构建并运行
```bash
docker-compose up -d --build
```

#### 3. 访问服务
- 前台：`http://your-domain`
- 管理端：`http://your-domain/admin`
- API：`http://your-domain/api`
- MySQL（主机映射）：`localhost:3306`，用户 `root`，密码见 `.env`

#### 4. 初始化数据库（首次）
```bash
docker exec -i blog-mysql mysql -uroot -p${MYSQL_ROOT_PASSWORD} blog < sql/init.sql
```

--- %%

## 配置说明

### 后端配置 (application-{profile}.yml)

|配置项|说明|示例值|部署必改|
|---|---|---|---|
|`spring.datasource.url`|MySQL 数据库连接地址|`localhost:3306`|✅|
|`spring.datasource.username`|数据库用户名|`root`|✅|
|`spring.datasource.password`|数据库密码|`${hm.db.password}`（建议通过环境变量传入）|✅|
|`server.port`|后端服务端口|`8080`|按需|
|`aliyun.oss.endpoint`|OSS 地域节点|`https://oss-cn-beijing.aliyuncs.com`|✅|
|`aliyun.oss.bucketName`|OSS Bucket 名称|`javaweb-ayeez`|✅|
|`aliyun.oss.region`|OSS 地域|`cn-beijing`|✅|

%% ### 前端配置 (.env)
| 变量名                  | 说明                     | 示例值                |
|-------------------------|--------------------------|-----------------------|
| `VITE_API_BASE_URL`     | API 基础路径             | `/api`                |
| `VITE_APP_TITLE`        | 站点标题                 | `我的博客`            |
| `VITE_APP_ICP`          | 备案号                   | `京ICP备12345678号`   | %%

%% ### Nginx 配置要点
- 反向代理 `/api` 到后端容器（`http://backend:8080`）。
- 静态文件缓存：对 js、css、图片设置 `expires`。
- SSL 配置：推荐使用 Let's Encrypt 自动续签。 %%

---

## API 文档

Apifox：
- 开发环境：https://tix3ut2jpw.apifox.cn

---

## 数据库设计
### 表结构概览

#### 1. 用户表 (`user`)

存储系统用户信息，支持普通用户和管理员角色。

|字段名|类型|约束|说明|
|---|---|---|---|
|id|BIGINT|PRIMARY KEY AUTO_INCREMENT|用户ID，自增主键|
|username|VARCHAR(50)|NOT NULL UNIQUE|用户名，唯一|
|nickname|VARCHAR(50)||昵称|
|password|VARCHAR(255)|NOT NULL|密码（BCrypt 加密存储）|
|email|VARCHAR(100)||邮箱，用于找回密码、通知|
|avatar|VARCHAR(512)||头像URL|
|role|TINYINT|NOT NULL DEFAULT 0|角色：0-普通用户，1-管理员|
|status|TINYINT|NOT NULL DEFAULT 1|状态：0-禁用，1-启用|
|create_time|DATETIME(3)|DEFAULT CURRENT_TIMESTAMP(3)|注册时间，精确到毫秒|

**索引建议**：`username` 唯一索引，`email` 普通索引。

#### 2. 文章表 (`blog_post`)

存储博客文章内容，支持 Markdown 格式。

|字段名|类型|约束|说明|
|---|---|---|---|
|id|VARCHAR(64)|PRIMARY KEY|文章ID（UUID 或雪花算法生成）|
|title|VARCHAR(255)|NOT NULL|文章标题|
|content|LONGTEXT|NOT NULL|文章正文（Markdown 格式）|
|description|VARCHAR(255)||文章摘要，用于列表展示和 SEO|
|cover|VARCHAR(512)||封面图片URL|
|category_id|BIGINT||所属分类ID，外键关联 `category(id)`|
|user_id|BIGINT|NOT NULL|作者ID，外键关联 `user(id)`|
|status|TINYINT|NOT NULL DEFAULT 1|状态：0-草稿，1-已发布，2-回收站|
|views|INT|NOT NULL DEFAULT 0|阅读量|
|create_time|DATETIME(3)|NOT NULL DEFAULT CURRENT_TIMESTAMP(3)|创建时间|
|update_time|DATETIME(3)|NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)|最后更新时间|



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
博客内日志： https://dev-blog.ayeez.cn/logs/
GitHub的commit记录：[Commits · Ayeez757/AyeezBlog](https://github.com/Ayeez757/AyeezBlog/commits/master/)

---

%% ## 许可证

本项目基于 [MIT 许可证](LICENSE) 开源，这意味着您可以自由使用、修改和分发，但需保留原版权声明。

---
 %%

## 联系方式

- 作者：[阿叶Ayeez]
- 邮箱：[3406608593@qq.com]
- 博客：旧博客 https://blog.ayeez.cn 或 新博客（本项目） https://dev.ayeez.cn
- GitHub Issues： https://github.com/ayeez757/AyeezBlog/issues
- QQ交流群（不仅限于本博客，欢迎加入）：421300955
---

*最后更新：2026-02-28