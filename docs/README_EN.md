# AyeezBlog

> Note: The project is under active development. Core features are already usable, and more features are coming soon.

Progress demo: https://dev-blog.ayeez.cn

## Language

- [中文文档](../README.md)

<p align="center">
  <!-- Version badge: Shields fetches /api/logs/current (same “current” marker as the on-site changelog) -->
  <a href="https://dev-blog.ayeez.cn/logs/">
    <img alt="Project version" src="https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fdev-blog.ayeez.cn%2Fapi%2Flogs%2Fcurrent&query=%24.data.version&label=AyeezBlog%20version">
  </a>
  <a href="https://github.com/ayeez757/AyeezBlog/blob/master/LICENSE">
    <img alt="License" src="https://img.shields.io/github/license/ayeez757/AyeezBlog">
  </a>
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.2.0-6DB33F?logo=spring">
  <img alt="Java" src="https://img.shields.io/badge/Java-21-007396?logo=java">
</p>

Main blog: https://blog.ayeez.cn  
Legacy blog (no longer maintained): https://butterfly.ayeez.cn

![](https://qiniu.ayeez.cn/20260228215441383.jpg)

## Introduction

**AyeezBlog** is an open-source blog system built with a frontend-backend separated architecture. The public site is built with Vue 3 and focuses on reading experience and content presentation; the admin panel focuses on content operations such as post, category, and tag management; the backend is based on Spring Boot and provides stable, clear RESTful APIs with good scalability and maintainability.

The project centers around "content creation + reader interaction + admin management", including common blog capabilities such as a Markdown-based post system, comments, category/tag organization, archives, and friend links. The overall stack covers frontend engineering, backend security and data access, caching, containerized deployment, and automation workflows, making it suitable for personal blogs, tech communities, and small-to-medium content sites.

Core page features:

- Home (post feed)
- Post detail (Markdown)
- Archive page
- Comments page
- Friend links page
- Changelog page

---

## Tech Stack

| Layer | Technology | Description |
| --- | --- | --- |
| **Frontend** | Vue 3 + Vite + Vue Router + Axios | Public-facing site |
| **Admin Panel** | Vue 3 + Element Plus + ECharts | Admin management UI |
| **Backend** | Java 21 + Spring Boot 3.2.0 + Spring Security + JWT + MyBatis + Spring AI (optional DeepSeek) + Redis | Business logic and APIs |
| **Database** | MySQL | Persistent storage |
| **Deployment** | Docker + Docker Compose + Nginx | Containerized deployment and reverse proxy |
| **CI/CD** | GitHub Actions | Automated testing and builds |
| **Server (current deployment)** | Ubuntu 22.04 | Runtime environment |
| **Other tools** | Twikoo (comments) | Third-party integration |

---

## Features

### Public Site
- **Home and post feed**: Announcement card, social links, and post card flow; posts are paginated by update time with previous/next navigation
- **Post detail reading experience**: Route by post ID (backend-generated short link), Markdown rendering, Front Matter parsing, `highlight.js` syntax highlighting, code language label, and one-click copy
- **TOC and quick navigation**: Auto-generated TOC from headings, hierarchical collapse/expand, smooth anchor scrolling, and floating buttons for top/comments/TOC toggle
- **Comment system**: Twikoo post comments (path-isolated) + dedicated comments page (`/comments`) + site-wide comment aggregation (posts/comments/friend-links) + two viewing modes (latest feed / page tree)
- **Archive system**: Timeline view of all posts with keyword search, year/month filtering, and ascending/descending sorting
- **Social and content pages**: Grouped friend-link cards with friend-link comments, site changelog timeline, and reserved About/Friends Circle entries (with old-site links)
- **Responsive design**: Mobile adaptations for home, post detail, comments, and changelog pages

### Admin Panel
- **Login and access control**: Admin login/authentication, local token persistence, and route guard redirect for unauthenticated access
- **End-to-end post management**: Paginated post list with keyword search, create/edit/delete, and ID-based detail echo with save
- **Writing and parsing capabilities**: Form fields for title/description/cover/short link/date/updated date, side-by-side Markdown editor + preview, and automatic Front Matter parsing (title/tags/category/date, etc.)
- **AI post excerpt (optional)**: A “generate excerpt from body” action on the description field; when the backend switch and DeepSeek API key are set, saving a post with an empty description can auto-fill an excerpt (existing descriptions are not overwritten)
- **AI cover image (optional)**: “AI generate cover (Jimeng)” on the cover field; uses Volcengine Jimeng (CV `CVProcess` with a `req_key`) with a fixed style prompt, then **uploads to Qiniu** and fills the cover URL (requires Volcengine Access Key + Secret, feature flag, and Qiniu config)
- **Category management**: List/query/create/edit/delete categories, with category-post lookup and quick jump to edit
- **Tag management**: List/query/create/edit/delete tags, with tag-post lookup and quick jump to edit
- **Admin home**: Reserved home entry for future dashboard/stat expansion

### Backend Capabilities
- **Post API suite**: Public post list/detail APIs + full admin post CRUD APIs
- **Category and tag API suite**: Full CRUD for categories and tags, plus "posts by category" and "posts by tag" query APIs
- **Authentication and security**: Admin login/token response, with baseline security via Spring Security + JWT + token filter
- **Unified response structure**: `Result` for standard API responses and `PageResult` for paginated data
- **Data validity and transactions**: Basic null/duplicate validation for category/tag writes, with transaction guarantees for consistency
- **Backend infrastructure**: CORS support, MyBatis + Mapper XML data access, and Redis cache integration
- **AI and Spring AI**: `spring-ai-starter-model-openai` calls DeepSeek via the OpenAI-compatible endpoint (`https://api.deepseek.com`) to generate post descriptions; admin `POST /admin/ai/article-description`; optional hook on post create/update when description is blank
- **AI cover (Jimeng)**: Volcengine visual text-to-image, download image bytes, server-side upload to Qiniu; admin `POST /admin/ai/article-cover` returns persistent `coverUrl`

---

## Architecture

- **Client**: Browser accesses services via HTTP/HTTPS.
- **Nginx**: Serves static files and proxies API requests.
- **Backend**: Spring Boot provides RESTful APIs and connects to MySQL/Redis.
- **Database**: MySQL stores data, Redis caches hot data.
- **Storage**: Static assets can be stored locally or in cloud object storage.

---

## Quick Start

### Requirements
- **Node.js** 18+
- **JDK** 17+ (21 recommended)
- **Maven** 3.6+
- **MySQL** 8.0+
- **Git**

### Local Development

#### 1. Clone repository
```bash
git clone https://github.com/Ayeez757/AyeezBlog.git
cd AyeezBlog
```

#### 2. Initialize database
- Create database: `ayeezblog` (recommended charset: `utf8mb4`)
- Run schema script in project root: `AyeezBlog建表.sql`

#### 3. Start backend (`blog-server`)

Configure database connection first. Note: the backend config has **no default values** for `hm.db.username` / `hm.db.password`. If you don't provide them, the backend will fail to start (errors like *Could not resolve placeholder 'hm.db.username'*).

You can do either:

- **Option A (recommended)**: set environment variables (Windows PowerShell example)

```powershell
$env:HM_DB_HOST="localhost"
$env:HM_DB_USER="root"
$env:HM_DB_PASSWORD="your_db_password"
```

- **Option B**: edit `AyeezBlog-Backend/blog-server/src/main/resources/application.yml` and fill `hm.db.host / hm.db.username / hm.db.password`.

Notes:

- **Qiniu** is only required if you use admin upload token or cover AI (the base CRUD APIs work without it).

**Optional — AI**: For DeepSeek excerpts, set `hm.deepseek.api-key` and `hm.deepseek.summary-enabled=true`. For Jimeng covers, set `hm.volcengine.access-key`, `hm.volcengine.secret-key`, and `hm.volcengine.cover-enabled=true`, and ensure Qiniu is configured (see **Configuration**). If unset, the rest of the app works as before.

If you run with the `dev` profile (enabling `application-dev.yml`), it is recommended to inject via environment variables: DeepSeek uses `DEEPSEEK_API_KEY` (optional `DEEPSEEK_VERBOSE_LOG`), and Jimeng uses `VOLCENGINE_ACCESS_KEY` / `VOLCENGINE_SECRET_KEY` (optional `VOLCENGINE_VERBOSE_HTTP_LOG`).

```bash
cd AyeezBlog-Backend
mvn clean install
cd blog-server
mvn spring-boot:run
```
Default API: `http://localhost:8080`

#### 4. Start frontend (`AyeezBlog-Frontend`)
```bash
cd AyeezBlog-Frontend
npm install
npm run dev
```
Visit: `http://localhost:5173`

#### 5. Start admin panel (`AyeezBlog-AdminPanel`)
```bash
cd AyeezBlog-AdminPanel
npm install
npm run dev
```
Visit: `http://localhost:5173/admin/`

Port note:

- The admin panel is fixed to port `5173` in `vite.config.js`, while the public frontend defaults to `5173`.
- If you run both at the same time, change the **frontend** port (e.g. `npm run dev -- --port 5174`) or change the admin `server.port`.

### Docker Deployment

#### 1. Update configs
- Copy env template: `cp .env.example .env`
- Check Nginx config: `nginx/nginx.conf`

#### 2. Build and run
```bash
docker-compose up -d --build
```

#### 3. Access
- Frontend: `http://your-domain`
- Admin: `http://your-domain/admin`
- API: `http://your-domain/api`
- MySQL: `localhost:3306` (`root`, password in `.env`)

#### 4. First-time DB initialization
```bash
docker exec -i blog-mysql mysql -uroot -p${MYSQL_ROOT_PASSWORD} blog < sql/init.sql
```

---

## Configuration

### Backend config (`application.yml`)

This section keeps only the essential keys. For detailed notes, examples, and optional parameters, see the inline comments in:

- `AyeezBlog-Backend/blog-server/src/main/resources/application.yml`

| Key | Description |
| --- | --- |
| `hm.db.host` / `hm.db.username` / `hm.db.password` | MySQL connection (required; backend won't start without these) |
| `hm.db.port` | MySQL port (defaults to 3306) |
| `server.port` | Backend port (defaults to 8080) |
| `qiniu.*` | Qiniu config (required only for admin upload token / cover AI; optional otherwise) |
| `hm.deepseek.*` | Excerpt generation (optional) |
| `hm.volcengine.*` | Cover generation (optional; typically requires Qiniu) |

### AI post excerpts (Spring AI + DeepSeek)

- **Scope**: Text chat only—generate a short Chinese excerpt from title + Markdown body for lists/cards; **no image generation**.
- **Admin UI**: Post create/edit page → description area → “generate excerpt from body” → `POST /admin/ai/article-description` with JSON `title` and `content` (same auth token as other admin APIs; admin dev proxy often uses `/api` as base).
- **Save hook**: When `blog.ai.summary.enabled` is true and the API key works, **create/update** with an **empty** description auto-fills before persist; non-empty descriptions are kept.
- **Disable**: Omit `hm.deepseek.api-key` or set `hm.deepseek.summary-enabled=false`; the app still starts.

### AI post cover (Volcengine Jimeng + Qiniu)

- **Flow**: Admin sends title + description (简介) → backend builds a fixed-style prompt → calls Volcengine visual `CVProcess` (Jimeng `req_key`) → downloads bytes from the temporary result URL → **uploads to Qiniu** under `covers/yyyy/MM/dd/` → returns a long-lived CDN URL.
- **Admin UI**: Post create/edit → optional “AI image instructions” textarea → “AI generate cover (Jimeng)”; API `POST /admin/ai/article-cover` with `title`, `description`, optional `coverPrompt`, response `coverUrl`.
- **Prerequisites**: `blog.ai.cover.enabled=true`, valid `hm.volcengine.access-key` / `hm.volcengine.secret-key`, and complete `qiniu.*` settings.
- **Auth**: Backend uses Volcengine OpenAPI signing (HMAC-SHA256), not Bearer tokens; tune `req_key` and dimensions per current Jimeng docs in `application.yml`.

---

## API Docs

Apifox:
- Dev env: https://tix3ut2jpw.apifox.cn

---

## Database Design

The database design has been moved to a separate document:

- [Database Design (English)](./DATABASE_DESIGN_EN.md)
- [Database Design (Chinese)](./DATABASE_DESIGN.md)

---

## CI/CD

GitHub Actions currently handles:

1. **Code push checks**: unit tests and style checks
2. **Image build**: build Docker images for services
3. **Deployment**: pull and restart services on server via SSH

Workflow files are in `.github/workflows/`.

---

## Contributing

Contributions are welcome, including:
- Bug reports
- Feature requests
- Code improvements
- Documentation fixes

### Workflow
1. Fork the repository and clone locally
2. Create branch: `git checkout -b feature/your-feature`
3. Open a Pull Request with clear change/testing notes

### Coding Standards
- Frontend: Vue 3 official style guide
- Backend: Alibaba Java coding conventions

---

## Changelog

- In-site logs: https://dev-blog.ayeez.cn/logs/
- GitHub activity: [Activity · Ayeez757/AyeezBlog](https://github.com/Ayeez757/AyeezBlog/activity)

---

## License

This project is open-sourced under the [Apache License 2.0](../LICENSE).

---

## Contact

- Author: Ayeez
- Email: 3406608593@qq.com
- Blog: main https://blog.ayeez.cn; legacy (no longer maintained) https://butterfly.ayeez.cn
- GitHub Issues: https://github.com/ayeez757/AyeezBlog/issues

---

*Last updated: 2026-04-03*
