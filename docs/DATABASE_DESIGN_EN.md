# Database Design

This document describes the database design for the `AyeezBlog` project (MySQL 8+).

## User Table

| Field | Type | Constraints | Description |
| --- | --- | --- | --- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT, NOT NULL | User ID |
| username | VARCHAR(50) | NOT NULL | Username |
| nickname | VARCHAR(50) | DEFAULT NULL | Nickname |
| password | VARCHAR(255) | NOT NULL | Password (encrypted storage) |
| role | TINYINT | NOT NULL, DEFAULT 0 | Role: 0-regular user, 1-admin |
| status | TINYINT | NOT NULL, DEFAULT 1 | Account status: 0-disabled, 1-enabled |

```sql
CREATE TABLE `user`
(
    `id`       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50)  NOT NULL COMMENT '用户名',
    `nickname` VARCHAR(50)           DEFAULT NULL COMMENT '昵称',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    `role`     TINYINT      NOT NULL DEFAULT 0 COMMENT '角色：0-普通用户，1-管理员',
    `status`   TINYINT      NOT NULL DEFAULT 1 COMMENT '账户状态：0-禁用，1-启用',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='用户表';
```

## Blog Post Table

| Field | Type | Constraints | Description |
| --- | --- | --- | --- |
| id | VARCHAR(64) | PRIMARY KEY, NOT NULL | Post ID (string, e.g. UUID) |
| title | VARCHAR(255) | NOT NULL | Post title |
| content | LONGTEXT | NOT NULL | Post content (Markdown) |
| cover | VARCHAR(512) | NULL | Cover image URL (optional) |
| create_time | DATETIME(3) | NOT NULL, DEFAULT CURRENT_TIMESTAMP(3) | Created time (millisecond precision) |
| update_time | DATETIME(3) | NOT NULL, DEFAULT CURRENT_TIMESTAMP(3), ON UPDATE CURRENT_TIMESTAMP(3) | Last updated time |
| description | VARCHAR(255) | DEFAULT NULL | Description |
| category_id | BIGINT UNSIGNED | NULL, FK -> blog_category(id) | Category ID (nullable means uncategorized) |

```sql
CREATE TABLE blog_post
(
    id          VARCHAR(64)  NOT NULL COMMENT '文章ID（字符串，如UUID）',
    title       VARCHAR(255) NOT NULL COMMENT '文章标题',
    content     LONGTEXT     NOT NULL COMMENT '文章正文（Markdown）',
    cover       VARCHAR(512) NULL COMMENT '封面图片URL，可为空',
    create_time DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（毫秒精度）',
    update_time DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
    description VARCHAR(255)          DEFAULT NULL COMMENT '描述',
    PRIMARY KEY (id),
    INDEX idx_create_time (create_time DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT ='博客文章表';
```

```sql
ALTER TABLE blog_post
    ADD COLUMN category_id BIGINT UNSIGNED NULL COMMENT '分类ID（可为空表示未分类）' AFTER description,
    ADD KEY idx_category_id (category_id),
    ADD CONSTRAINT fk_blog_post_category
        FOREIGN KEY (category_id) REFERENCES blog_category(id)
            ON DELETE SET NULL ON UPDATE CASCADE;
```

## Friend Link Class Table

| Field | Type | Constraints | Description |
| --- | --- | --- | --- |
| id | BIGINT UNSIGNED | PRIMARY KEY, AUTO_INCREMENT, NOT NULL | Class ID |
| class_name | VARCHAR(64) | NOT NULL, UNIQUE | Class name |
| class_desc | VARCHAR(255) | NULL | Class description |
| sort | INT | NOT NULL, DEFAULT 0 | Sort order |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Created time |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP, ON UPDATE CURRENT_TIMESTAMP | Updated time |

```sql
CREATE TABLE IF NOT EXISTS friend_link_class (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    class_name   VARCHAR(64)  NOT NULL,
    class_desc   VARCHAR(255) NULL,
    sort         INT          NOT NULL DEFAULT 0,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_class_name (class_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## Friend Link Table

| Field | Type | Constraints | Description |
| --- | --- | --- | --- |
| id | BIGINT UNSIGNED | PRIMARY KEY, AUTO_INCREMENT, NOT NULL | Friend link ID |
| class_id | BIGINT UNSIGNED | NOT NULL, FK -> friend_link_class(id) | Friend link class ID |
| name | VARCHAR(128) | NOT NULL | Site name |
| link | VARCHAR(512) | NOT NULL | Site URL |
| avatar | VARCHAR(512) | NULL | Site avatar |
| descr | VARCHAR(512) | NULL | Site description |
| rss | VARCHAR(512) | NULL | RSS URL (reserved) |
| sort | INT | NOT NULL, DEFAULT 0 | Sort order |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Created time |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP, ON UPDATE CURRENT_TIMESTAMP | Updated time |

```sql
CREATE TABLE IF NOT EXISTS friend_link (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    class_id    BIGINT UNSIGNED NOT NULL,
    name        VARCHAR(128) NOT NULL,
    link        VARCHAR(512) NOT NULL,
    avatar      VARCHAR(512) NULL,
    descr       VARCHAR(512) NULL,
    rss         VARCHAR(512) NULL,
    sort        INT          NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_class_id (class_id),
    CONSTRAINT fk_friend_link_class
        FOREIGN KEY (class_id) REFERENCES friend_link_class(id)
            ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## Blog Category Table (Tree)

| Field | Type | Constraints | Description |
| --- | --- | --- | --- |
| id | BIGINT UNSIGNED | PRIMARY KEY, AUTO_INCREMENT | Category ID |
| parent_id | BIGINT UNSIGNED | NULL, FK -> blog_category(id) | Parent category ID, NULL for root |
| name | VARCHAR(64) | NOT NULL, UNIQUE(parent_id, name) | Category name (unique under same parent) |
| slug | VARCHAR(64) | NULL, UNIQUE | URL-friendly slug |
| description | VARCHAR(255) | NULL | Category description |
| sort | INT | NOT NULL, DEFAULT 0 | Sibling sort order |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Created time |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP, ON UPDATE CURRENT_TIMESTAMP | Updated time |

```sql
CREATE TABLE blog_category
(
    id          BIGINT UNSIGNED AUTO_INCREMENT COMMENT '分类ID'
        PRIMARY KEY,
    parent_id   BIGINT UNSIGNED                    NULL COMMENT '父分类ID，NULL表示顶级',
    name        VARCHAR(64)                        NOT NULL COMMENT '分类名称',
    slug        VARCHAR(64)                        NULL COMMENT '可选：URL友好标识，唯一',
    description VARCHAR(255)                       NULL COMMENT '分类描述',
    sort        INT      DEFAULT 0                 NOT NULL COMMENT '同级排序',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_category_name_parent
        UNIQUE (parent_id, name),
    CONSTRAINT uk_category_slug
        UNIQUE (slug),
    CONSTRAINT fk_blog_category_parent
        FOREIGN KEY (parent_id) REFERENCES blog_category (id)
            ON UPDATE CASCADE
)
    COMMENT '博客文章分类表（树）'
    COLLATE = utf8mb4_unicode_ci;
```

```sql
CREATE INDEX idx_parent_id
    ON blog_category (parent_id);
```

## Blog Tag Table

| Field | Type | Constraints | Description |
| --- | --- | --- | --- |
| id | BIGINT UNSIGNED | PRIMARY KEY, AUTO_INCREMENT, NOT NULL | Tag ID |
| name | VARCHAR(64) | NOT NULL, UNIQUE | Tag name |
| slug | VARCHAR(64) | NULL, UNIQUE | URL-friendly slug |
| description | VARCHAR(255) | NULL | Tag description |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Created time |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP, ON UPDATE CURRENT_TIMESTAMP | Updated time |

```sql
CREATE TABLE IF NOT EXISTS blog_tag (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    name        VARCHAR(64)     NOT NULL COMMENT '标签名',
    slug        VARCHAR(64)     NULL COMMENT '可选：URL友好标识，唯一',
    description VARCHAR(255)    NULL COMMENT '标签描述',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tag_name (name),
    UNIQUE KEY uk_tag_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客标签表';
```

## Blog Post-Tag Relation Table (Many-to-Many)

| Field | Type | Constraints | Description |
| --- | --- | --- | --- |
| post_id | VARCHAR(64) | PRIMARY KEY (composite), NOT NULL, FK -> blog_post(id) | Post ID |
| tag_id | BIGINT UNSIGNED | PRIMARY KEY (composite), NOT NULL, FK -> blog_tag(id) | Tag ID |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Relation created time |

```sql
CREATE TABLE IF NOT EXISTS blog_post_tag (
    post_id    VARCHAR(64)     NOT NULL COMMENT '文章ID（对应blog_post.id）',
    tag_id     BIGINT UNSIGNED NOT NULL COMMENT '标签ID（对应blog_tag.id）',
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (post_id, tag_id),
    KEY idx_tag_id (tag_id),
    CONSTRAINT fk_blog_post_tag_post
        FOREIGN KEY (post_id) REFERENCES blog_post(id)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_blog_post_tag_tag
        FOREIGN KEY (tag_id) REFERENCES blog_tag(id)
            ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客文章-标签关联表';
```
