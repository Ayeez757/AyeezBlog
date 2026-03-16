package cn.ayeez.blogpojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlogTag {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

