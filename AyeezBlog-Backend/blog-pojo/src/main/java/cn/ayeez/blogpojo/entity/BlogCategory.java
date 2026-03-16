package cn.ayeez.blogpojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlogCategory {
    private Long id;
    private Long parentId;
    private String name;
    private String description;
    private Integer sort;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}

