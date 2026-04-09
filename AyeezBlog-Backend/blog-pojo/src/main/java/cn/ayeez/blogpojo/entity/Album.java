package cn.ayeez.blogpojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Album {
    private Long id;
    private String title;
    private String description;
    private Integer sort;
    /** 是否作为博客文章默认封面来源相册：0-否，1-是 */
    private Integer defaultCoverSource;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
