
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



create table user
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


-- 插入默认管理员用户
insert into user (username, password, role, status)
values ('admin', 'admin', 1, 1);

-- 插入一篇默认欢迎使用AyeezBlog博客测试文章
insert into blog_post (id, title, content, cover, create_time, update_time, description)
values ('welcome', '欢迎使用AyeezBlog博客系统', '欢迎使用AyeezBlog博客系统，这是默认的欢迎文章。', null, now(), now(), '这是默认的欢迎文章');

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
