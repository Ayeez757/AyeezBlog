package cn.ayeez.blogpojo.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 站点更新日志版本实体。
 * 对应数据库表：blog_log_version
 */
@Data
public class BlogLogVersion {

    private Long id;

    /**
     * 版本号，例如 v1.3.0
     */
    private String version;

    /**
     * 版本日期
     */
    private LocalDate logDate;

    /**
     * 是否为当前生效版本：0/1
     */
    private Integer isCurrent;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

