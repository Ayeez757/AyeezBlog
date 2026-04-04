package cn.ayeez.blogpojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 关于页追番列表，对应表 blog_about_anime
 */
@Data
public class AboutAnime {

    private Long id;
    private String imageUrl;
    private String title;
    private String linkUrl;
    private Integer sort;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
