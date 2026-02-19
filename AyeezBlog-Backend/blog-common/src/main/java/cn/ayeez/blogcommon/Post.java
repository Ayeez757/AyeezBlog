package cn.ayeez.blogcommon;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 博客文章实体类
 * 对应数据库表：blog_post
 */
@Data
public class Post {

    /**
     * 文章ID（字符串，如UUID或雪花ID）
     * 数据库字段：id VARCHAR(64)
     */
    private String id;

    /**
     * 文章标题
     * 数据库字段：title VARCHAR(255)
     */
    private String title;

    /**
     * 文章正文（支持Markdown/HTML）
     * 数据库字段：content LONGTEXT
     */
    private String content;

    /**
     * 封面图片URL（可选，可为null）
     * 数据库字段：cover VARCHAR(512)
     */
    private String cover;

    /**
     * 创建时间
     * 数据库字段：create_time DATETIME(3)
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     * 数据库字段：update_time DATETIME(3)
     */
    private LocalDateTime updateTime;
}