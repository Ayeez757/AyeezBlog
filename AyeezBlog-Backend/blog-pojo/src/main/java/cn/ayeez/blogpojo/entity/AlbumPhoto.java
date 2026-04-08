package cn.ayeez.blogpojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlbumPhoto {
    private Long id;
    private Long albumId;
    private String imageUrl;
    private String caption;
    private Integer sort;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
