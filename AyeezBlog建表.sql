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