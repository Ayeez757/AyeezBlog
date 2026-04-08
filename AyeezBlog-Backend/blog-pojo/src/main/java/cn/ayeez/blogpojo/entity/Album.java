package cn.ayeez.blogpojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Album {
    private Long id;
    private String title;
    private String description;
    private Integer sort;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
