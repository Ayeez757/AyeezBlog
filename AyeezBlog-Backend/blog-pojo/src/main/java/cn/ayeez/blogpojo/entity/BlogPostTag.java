package cn.ayeez.blogpojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlogPostTag {
    private String postId;
    private Long tagId;
    private LocalDateTime createdAt;
}

