-- 用户表
CREATE TABLE `user`
(
    `id`       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50)  NOT NULL COMMENT '用户名',
    `nickname` varchar(50) comment '昵称',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    `role`     TINYINT      NOT NULL DEFAULT 0 COMMENT '角色：0-普通用户，1-管理员',
    `status`   TINYINT      NOT NULL DEFAULT 1 COMMENT '账户状态：0-禁用，1-启用',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';



CREATE TABLE blog_post
(
    id          VARCHAR(64)  NOT NULL COMMENT '文章ID（字符串，如UUID）',
    title       VARCHAR(255) NOT NULL COMMENT '文章标题',
    content     LONGTEXT     NOT NULL COMMENT '文章正文（Markdown）',
    cover       VARCHAR(512) NULL COMMENT '封面图片URL，可为空',
    create_time DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（毫秒精度）',
    update_time DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
    description varchar(255) comment '描述',

    -- 主键约束
    PRIMARY KEY (id),

    -- 索引优化（按需添加）
    INDEX idx_create_time (create_time DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT ='博客文章表';

ALTER TABLE blog_post
    ADD COLUMN category_id BIGINT UNSIGNED NULL COMMENT '分类ID（可为空表示未分类）' AFTER description,
    ADD KEY idx_category_id (category_id),
    ADD CONSTRAINT fk_blog_post_category
        FOREIGN KEY (category_id) REFERENCES blog_category(id)
            ON DELETE SET NULL ON UPDATE CASCADE;

# select * from blog_post;
# SELECT
#     id,
#     title,
#     content,
#     cover,
#     description,
#     create_time as createTime,
#     update_time as updateTime
# FROM blog_post
# WHERE id = 'f50487ff';

-- 友链分类表
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

-- 友链表
CREATE TABLE IF NOT EXISTS friend_link (
                                           id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                           class_id    BIGINT UNSIGNED NOT NULL,
                                           name        VARCHAR(128) NOT NULL,
                                           link        VARCHAR(512) NOT NULL,
                                           avatar      VARCHAR(512) NULL,
                                           descr       VARCHAR(512) NULL,
                                           rss         VARCHAR(512) NULL,   -- 预留
                                           sort        INT          NOT NULL DEFAULT 0,
                                           created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                           PRIMARY KEY (id),
                                           KEY idx_class_id (class_id),
                                           CONSTRAINT fk_friend_link_class
                                               FOREIGN KEY (class_id) REFERENCES friend_link_class(id)
                                                   ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 文章分类表（树：parent_id 自关联）
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



ALTER TABLE blog_post
    ADD COLUMN category_id BIGINT UNSIGNED NULL COMMENT '分类ID（可为空表示未分类）',
    ADD KEY idx_category_id (category_id),
    ADD CONSTRAINT fk_blog_post_category
        FOREIGN KEY (category_id) REFERENCES blog_category(id)
            ON DELETE SET NULL ON UPDATE CASCADE;

-- 标签表
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


-- 文章-标签关联表（多对多）
CREATE TABLE IF NOT EXISTS blog_post_tag (
                                             post_id VARCHAR(64)      NOT NULL COMMENT '文章ID（对应blog_post.id）',
                                             tag_id  BIGINT UNSIGNED  NOT NULL COMMENT '标签ID（对应blog_tag.id）',
                                             created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                             PRIMARY KEY (post_id, tag_id),
                                             KEY idx_tag_id (tag_id),
                                             CONSTRAINT fk_blog_post_tag_post
                                                 FOREIGN KEY (post_id) REFERENCES blog_post(id)
                                                     ON DELETE CASCADE ON UPDATE CASCADE,
                                             CONSTRAINT fk_blog_post_tag_tag
                                                 FOREIGN KEY (tag_id) REFERENCES blog_tag(id)
                                                     ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客文章-标签关联表';