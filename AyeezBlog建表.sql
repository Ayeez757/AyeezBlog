
create database if not exists ayeezblog;
use ayeezblog;

create table blog_category
(
    id          bigint unsigned auto_increment comment '分类ID'
        primary key,
    parent_id   bigint unsigned                    null comment '父分类ID，NULL表示顶级',
    name        varchar(64)                        not null comment '分类名称',
    slug        varchar(64)                        null comment '可选：URL友好标识，唯一',
    description varchar(255)                       null comment '分类描述',
    sort        int      default 0                 not null comment '同级排序',
    created_at  datetime default CURRENT_TIMESTAMP not null,
    updated_at  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uk_category_name_parent
        unique (parent_id, name),
    constraint uk_category_slug
        unique (slug),
    constraint fk_blog_category_parent
        foreign key (parent_id) references blog_category (id)
            on update cascade
)
    comment '博客文章分类表（树）' collate = utf8mb4_unicode_ci;

create index idx_parent_id
    on blog_category (parent_id);

create table blog_post
(
    id          varchar(64)                              not null comment '文章ID（字符串，如UUID）'
        primary key,
    title       varchar(255)                             not null comment '文章标题',
    content     longtext                                 not null comment '文章正文（Markdown）',
    cover       varchar(512)                             null comment '封面图片URL，可为空',
    create_time datetime(3) default CURRENT_TIMESTAMP(3) not null comment '创建时间（毫秒精度）',
    update_time datetime(3) default CURRENT_TIMESTAMP(3) not null on update CURRENT_TIMESTAMP(3) comment '最后更新时间',
    description varchar(255)                             null comment '描述',
    category_id bigint unsigned                          null comment '分类ID（可为空表示未分类）',
    pinned      tinyint(1) default 0                     not null comment '首页卡片角标：置顶',
    featured    tinyint(1) default 0                     not null comment '首页卡片角标：推荐',
    editing     tinyint(1) default 0                     not null comment '首页卡片角标：正在编辑',
    water       tinyint(1) default 0                     not null comment '首页卡片角标：水',
    constraint fk_blog_post_category
        foreign key (category_id) references blog_category (id)
            on update cascade on delete set null
)
    comment '博客文章表' collate = utf8mb4_unicode_ci;

create index idx_category_id
    on blog_post (category_id);

create index idx_create_time
    on blog_post (create_time desc);

create table blog_tag
(
    id          bigint unsigned auto_increment comment '标签ID'
        primary key,
    name        varchar(64)                        not null comment '标签名',
    slug        varchar(64)                        null comment '可选：URL友好标识，唯一',
    description varchar(255)                       null comment '标签描述',
    created_at  datetime default CURRENT_TIMESTAMP not null,
    updated_at  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uk_tag_name
        unique (name),
    constraint uk_tag_slug
        unique (slug)
)
    comment '博客标签表' collate = utf8mb4_unicode_ci;

create table blog_post_tag
(
    post_id    varchar(64)                        not null comment '文章ID（对应blog_post.id）',
    tag_id     bigint unsigned                    not null comment '标签ID（对应blog_tag.id）',
    created_at datetime default CURRENT_TIMESTAMP not null,
    primary key (post_id, tag_id),
    constraint fk_blog_post_tag_post
        foreign key (post_id) references blog_post (id)
            on update cascade on delete cascade,
    constraint fk_blog_post_tag_tag
        foreign key (tag_id) references blog_tag (id)
            on update cascade on delete cascade
)
    comment '博客文章-标签关联表' collate = utf8mb4_unicode_ci;

create index idx_tag_id
    on blog_post_tag (tag_id);

create table friend_link_class
(
    id         bigint unsigned auto_increment
        primary key,
    class_name varchar(64)                        not null,
    class_desc varchar(255)                       null,
    sort       int      default 0                 not null,
    created_at datetime default CURRENT_TIMESTAMP not null,
    updated_at datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uk_class_name
        unique (class_name)
)
    collate = utf8mb4_unicode_ci;

create table friend_link
(
    id         bigint unsigned auto_increment
        primary key,
    class_id   bigint unsigned                    not null,
    name       varchar(128)                       not null,
    link       varchar(512)                       not null,
    avatar     varchar(512)                       null,
    descr      varchar(512)                       null,
    rss        varchar(512)                       null,
    sort       int      default 0                 not null,
    created_at datetime default CURRENT_TIMESTAMP not null,
    updated_at datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint fk_friend_link_class
        foreign key (class_id) references friend_link_class (id)
            on update cascade
)
    collate = utf8mb4_unicode_ci;

create index idx_class_id
    on friend_link (class_id);



create table `user`
(
    id       bigint auto_increment comment '用户ID'
        primary key,
    username varchar(50)       not null comment '用户名',
    nickname varchar(50)       null comment '昵称',
    password varchar(255)      not null comment '密码（加密存储）',
    role     tinyint default 0 not null comment '角色：0-普通用户，1-管理员',
    status   tinyint default 1 not null comment '账户状态：0-禁用，1-启用'
)
    comment '用户表';


-- 插入默认管理员用户（密码为 admin 的 BCrypt 哈希，强度 10）
insert into `user` (username, password, role, status)
values ('admin', '$2a$10$eBH3YL6wptUNHJidoKGlzeVue1QUxgDEpHOfr0zefa6eM1IxuNfga', 1, 1);

-- 插入默认友链分组 + 默认友链
insert ignore into friend_link_class (id, class_name, class_desc, sort)
values (1, '默认', '默认分组', 0);

insert ignore into friend_link (id, class_id, name, link, avatar, descr, rss, sort)
values (1, 1, '阿叶Ayeez博客', 'https://blog.ayeez.cn', 'https://qiniu.ayeez.cn/avatar.jpg', '是博客作者呀~', null, 0);

-- 插入一篇默认欢迎文章（包含 Front Matter）
insert into blog_post (
  id, title, content, cover, create_time, update_time, description,
  category_id, pinned, featured, editing, water
)
values (
  'fe3410',
  '欢迎访问阿叶Ayeez博客~',
  '---\nabbrlink: fe3410\ncategories:\n  - 博客\ncover: ''''\ndate: ''2026-04-05''\ndescription: |-\n  （默认欢迎文章）\n  阿叶Ayeez博客是一个基于Vue 3与Spring Boot构建的开源博客系统，提供前后端分离架构、Markdown写作、评论互动与后台管理等功能，支持容器化部署与AI辅助创作。\nediting: false\nfeatured: false\npinned: true\ntags:\n  - 博客\ntitle: 欢迎访问阿叶Ayeez博客~\nupdated: ''2026-04-05''\nwater: false\n---\n\n# 欢迎使用阿叶Ayeez的博客\n\n![](https://qiniu.ayeez.cn/20260228215441383.jpg)\n\n\n本博客链接：[https://blog.ayeez.cn](https://blog.ayeez.cn)  \n旧博客链接（已停止维护）：[https://butterfly.ayeez.cn](https://butterfly.ayeez.cn)\n\n\n\n\n## 项目简介\n\n**AyeezBlog** 是一个开源的博客系统，采用前后端分离架构设计。前台基于 Vue 3 构建，聚焦阅读体验与内容展示；管理端面向内容管理场景，便于文章、分类、标签等信息的统一维护；后端基于 Spring Boot 提供稳定、清晰的 RESTful API 能力，兼顾扩展性与可维护性。\n\n项目以“内容创作 + 阅读互动 + 后台管理”作为核心方向，支持 Markdown 文章体系、评论互动、分类标签组织、归档与友链等博客常用能力。整体技术栈覆盖前端工程化、后端安全与数据访问、缓存、容器化部署和自动化流程，适用于个人博客、技术社区与中小型内容站点的搭建和二次开发。\n\n核心页面功能：\n\n- 首页（文章流）\n- 文章详情页（Markdown）\n- 归档页\n- 留言页\n- 友链页\n- 更新日志页\n\n## 技术栈选用\n\n\n| 层次              | 技术                                                                                            | 说明         |\n| --------------- | --------------------------------------------------------------------------------------------- | ---------- |\n| **前端**          | Vue 3 + Vite + Vue Router + Axios                                                             | 前台展示界面     |\n| **管理端**         | Vue 3 + Element Plus + ECharts                                                                | 后台管理界面     |\n| **后端**          | Java 21 + Spring Boot 3.2.0 + Spring Security + JWT + MyBatis + Spring AI（可选 DeepSeek）+ Redis | 业务逻辑与数据接口  |\n| **数据库**         | MySQL                                                                                         | 持久化存储      |\n| **部署**          | Docker + Docker Compose + Nginx                                                               | 容器化部署，反向代理 |\n| **CI/CD**       | GitHub Actions                                                                                | 自动化测试与构建   |\n| **服务器（当前实际部署）** | ubantu22.04                                                                                   | 服务器系统      |\n| **其他第三方工具**     | twikoo（评论）                                                                                    |            |\n| **接入大模型**       | deepseek+即梦ai                                                                                 |            |\n\n\n## 功能特性\n\n### 前台展示\n\n- **首页与文章流**：公告卡片、社交链接、文章卡片流展示；文章按更新时间分页查询并支持上一页/下一页切换\n- **文章详情阅读体验**：按文章 ID 路由访问（后端随机短链接）、Markdown 正文渲染、Front Matter 解析、`highlight.js` 代码高亮、代码块语言标识与一键复制\n- **目录与快速导航**：自动提取标题生成 TOC、目录按层级折叠/展开、目录锚点平滑跳转、悬浮按钮支持回顶/跳评论/开关目录\n- **评论体系**：Twikoo 文章评论（按路径隔离）+ 独立留言页（`/comments`）+ 全站评论聚合（文章/留言/友链）+ 最新流与按页面树形两种查看模式\n- **归档体系**：时间轴浏览全部文章，支持关键词搜索、年份/月筛选、正序/倒序排序\n- **社交与内容页**：友链页分组卡片展示与友链留言区、网站更新日志时间线、关于页与朋友圈页预留入口（含旧站跳转）\n- **多端适配**：首页、文章页、留言页、日志页完成移动端响应式优化\n\n### 后台管理\n\n- **登录与访问控制**：后台登录鉴权、token 本地持久化、路由守卫未登录自动跳转登录页\n- **文章管理全流程**：文章列表分页与关键词搜索、文章新增/编辑/删除、按 ID 回显详情并保存修改\n- **写作与解析能力**：写作表单支持标题/描述/封面/短链/日期/更新时间，Markdown 编辑区与预览区同屏，自动解析 Front Matter（标题/标签/分类/日期等）\n- **AI 文章简介（可选）**：描述区域提供「根据正文生成简介」按钮；在后端开启开关且配置 DeepSeek API Key 后，保存文章时若描述为空可自动生成简介（不覆盖已有描述）\n- **AI 文章封面（可选）**：封面区域提供「AI 生成封面（即梦）」；使用火山引擎即梦（智能视觉 CVProcess）按统一风格出图后**转存七牛**，自动填入封面链接（需配置火山 Access Key/Secret、开启开关且七牛可用）\n- **分类管理**：分类列表查询、新增、编辑、删除，并支持查看分类下文章与快速跳转编辑\n- **标签管理**：标签列表查询、新增、编辑、删除，并支持查看标签下文章与快速跳转编辑\n- **后台首页**：已预留首页入口，可继续扩展统计看板\n\n### 后端能力\n\n- **文章接口体系**：公共文章列表/详情接口 + 管理端文章增删改查完整接口\n- **分类与标签接口体系**：分类与标签均支持增删改查，并提供分类下文章查询、标签下文章查询接口\n- **认证与安全**：管理员登录认证并返回登录信息/token，基于 Spring Security + JWT + Token 过滤器实现安全基础能力\n- **统一数据返回**：`Result` 统一响应结构与 `PageResult` 标准分页结构\n- **数据有效性与事务**：分类/标签写操作含基础参数判空与重名校验，并通过事务保证写入一致性\n- **后端基础设施**：CORS 跨域支持、MyBatis + Mapper XML 数据访问\n- **AI 与 Spring AI**：集成 `spring-ai-starter-model-openai`，通过 DeepSeek 官方 OpenAI 兼容接口（`https://api.deepseek.com`）生成文章描述；管理端 `POST /admin/ai/article-description`；与文章新增/更新流程可选联动\n- **AI 封面（即梦）**：调用火山引擎即梦文生图接口生成图片，下载后服务端上传七牛；管理端 `POST /admin/ai/article-cover`，返回持久 `coverUrl`\n\n\n## 系统架构\n\n- **客户端**：浏览器通过 HTTP/HTTPS 访问 Nginx。\n- **Nginx**：分发静态资源，代理 API 请求到后端容器，支持 Gzip 压缩和 SSL 终止。\n- **后端服务**：Spring Boot 应用提供 RESTful API，连接 MySQL 和 Redis。\n- **数据库**：MySQL 存储业务数据，Redis 缓存热点数据（如文章详情、用户会话）。\n- **文件存储**：图片等静态资源可存储在本地或云对象存储。\n\n## API 文档\n\nApifox：\n- 开发环境：[https://tix3ut2jpw.apifox.cn](https://tix3ut2jpw.apifox.cn)\n\n## 许可证\n\n本项目基于 [Apache License 2.0 许可证](LICENSE) 开源，这意味着您可以自由使用、修改和分发，但需保留原版权声明。\n\n---\n\n## 联系方式\n\n- 作者：[阿叶Ayeez]\n- 邮箱：[[3406608593@qq.com](mailto:3406608593@qq.com)]\n- 博客：本博客 [https://blog.ayeez.cn](https://blog.ayeez.cn)；旧博客（已停止维护） [https://butterfly.ayeez.cn](https://butterfly.ayeez.cn)\n- GitHub Issues： [https://github.com/Ayeez757/AyeezBlog/issues](https://github.com/Ayeez757/AyeezBlog/issues)\n- QQ交流群（不仅限于本博客，欢迎加入）：421300955\n\n---\n\n\n\n[![Star History Chart](https://api.star-history.com/chart?repos=ayeez757/ayeezblog&type=date&legend=top-left)](https://www.star-history.com/?repos=ayeez757%2Fayeezblog&type=date&legend=top-left)\n',
  '',
  now(),
  now(),
  '（默认欢迎文章）\n阿叶Ayeez博客是一个基于Vue 3与Spring Boot构建的开源博客系统，提供前后端分离架构、Markdown写作、评论互动与后台管理等功能，支持容器化部署与AI辅助创作。',
  null,
  1, 0, 0, 0
);

-- 站点访问统计：总访问量（PV）
create table if not exists blog_site_stats
(
    id         tinyint unsigned                not null primary key comment '固定单行ID=1',
    page_views bigint unsigned default 0       not null comment '总访问量PV',
    updated_at datetime       default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
)
    comment '站点访问统计' collate = utf8mb4_unicode_ci;

insert ignore into blog_site_stats (id, page_views) values (1, 0);

-- 访客记录：用于统计总访客数（UV）
create table if not exists blog_site_visitor
(
    id               bigint unsigned auto_increment primary key,
    visitor_key      varchar(128)                      not null comment '前端生成的访客唯一标识',
    ip_address       varchar(64)                       null comment '访客IP',
    user_agent       varchar(512)                      null comment '浏览器UA',
    first_path       varchar(255)                      null comment '首次访问路径',
    first_visit_time datetime default CURRENT_TIMESTAMP not null comment '首次访问时间',
    last_visit_time  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '最后访问时间',
    constraint uk_blog_site_visitor_key unique (visitor_key)
)
    comment '站点访客记录（UV统计）' collate = utf8mb4_unicode_ci;

-- 站点访问统计：每日PV
create table if not exists blog_site_pv_daily
(
    stat_date   date                     not null primary key comment '统计日期（yyyy-MM-dd）',
    page_views  bigint unsigned default 0 not null comment '当日访问量PV'
)
    comment '站点每日访问统计（PV）' collate = utf8mb4_unicode_ci;

-- 站点访客统计：每日UV聚合（用于图表展示）
create table if not exists blog_site_uv_daily
(
    stat_date        date                     not null primary key comment '统计日期（yyyy-MM-dd）',
    unique_visitors  bigint unsigned default 0 not null comment '当日唯一访客UV'
)
    comment '站点每日访客统计（UV）' collate = utf8mb4_unicode_ci;

-- 站点访客统计：每日UV去重明细（visitor_key + stat_date）
-- tracker 每次请求先插入明细，第一次出现的 visitor 才会累积到 blog_site_uv_daily
create table if not exists blog_site_uv_daily_detail
(
    stat_date   date           not null comment '统计日期（yyyy-MM-dd）',
    visitor_key varchar(128)  not null comment '前端生成的访客唯一标识',
    first_seen_time datetime default CURRENT_TIMESTAMP not null,
    primary key (stat_date, visitor_key)
)
    comment '站点每日访客去重明细（UV Detail）' collate = utf8mb4_unicode_ci;

-- 站点更新日志版本表
create table if not exists blog_log_version
(
    id          bigint unsigned auto_increment primary key,
    version     varchar(64) not null comment '版本号，例如 v1.3.0',
    log_date    date         not null comment '版本日期',
    is_current  tinyint      not null default 0 comment '是否为当前生效版本：0/1',
    created_at  datetime     default CURRENT_TIMESTAMP not null,
    updated_at  datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    unique uk_blog_log_version (version)
) comment '站点更新日志版本表' collate = utf8mb4_unicode_ci;

-- 站点更新日志条目表
create table if not exists blog_log_entry
(
    id         bigint unsigned auto_increment primary key,
    version_id bigint unsigned not null comment '日志版本ID',
    sort       int             not null default 0 comment '排序（保持 changes 顺序）',
    content    varchar(2048)  not null comment '变更内容',
    index idx_blog_log_entry_version_id (version_id),
    constraint fk_blog_log_entry_version
        foreign key (version_id) references blog_log_version (id)
            on update cascade on delete cascade,
    unique uk_blog_log_entry_version_sort (version_id, sort)
) comment '站点更新日志条目表' collate = utf8mb4_unicode_ci;

-- 关于页追番列表（管理端可排序）
create table if not exists blog_about_anime
(
    id         bigint unsigned auto_increment primary key,
    image_url  varchar(512)                        not null comment '封面图完整 URL',
    title      varchar(128)                        null comment '标题/tooltip（可选）',
    link_url   varchar(512)                        null comment '点击跳转（可选）',
    sort       int      default 0                 not null comment '展示顺序，升序',
    created_at datetime default CURRENT_TIMESTAMP not null,
    updated_at datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
) comment '关于页追番列表' collate = utf8mb4_unicode_ci;

-- 相册表（前台展示 + 管理端维护）
create table if not exists blog_album
(
    id          bigint unsigned auto_increment primary key,
    title       varchar(128)                        not null comment '相册标题',
    description varchar(512)                        null comment '相册描述',
    sort        int      default 0                 not null comment '相册排序，升序',
    default_cover_source tinyint(1) default 0      not null comment '是否为文章默认封面来源相册：0否1是',
    created_at  datetime default CURRENT_TIMESTAMP not null,
    updated_at  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
) comment '相册主表' collate = utf8mb4_unicode_ci;

-- 相册图片表（同一相册下多张图）
create table if not exists blog_album_photo
(
    id          bigint unsigned auto_increment primary key,
    album_id    bigint unsigned                    not null comment '相册ID',
    image_url   varchar(512)                       not null comment '图片URL',
    caption     varchar(255)                       null comment '图片文案',
    sort        int      default 0                 not null comment '图片排序，升序',
    created_at  datetime default CURRENT_TIMESTAMP not null,
    updated_at  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    index idx_blog_album_photo_album_id (album_id),
    constraint fk_blog_album_photo_album
        foreign key (album_id) references blog_album (id)
            on update cascade on delete cascade
) comment '相册图片表' collate = utf8mb4_unicode_ci;

-- 注意：blog_post 表在上方 CREATE TABLE 时已包含 pinned/featured/editing/water 字段
-- 这里不再重复 ALTER，避免脚本执行时报 Duplicate column name
