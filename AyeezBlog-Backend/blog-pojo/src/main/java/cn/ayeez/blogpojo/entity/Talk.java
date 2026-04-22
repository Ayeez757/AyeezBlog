package cn.ayeez.blogpojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 说说/朋友圈
 * 对应数据库表：blog_talk
 */
@Data
public class Talk {
    private Long id;

    /**
     * 说说正文（纯文本/简单 HTML 均可；前台按文本展示）
     */
    private String content;

    /**
     * 图片 URL 列表（JSON 字符串，示例：["https://...","https://..."]）
     */
    private String images;

    /**
     * 是否发布：0 草稿/下线；1 已发布
     */
    private Integer published;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

