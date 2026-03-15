package cn.ayeez.blogpojo.dto.request;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 博客文章实体类
 * 对应数据库表：blog_post
 */
@Data
public class PostBody {

    /**
     * 文章 ID（字符串，如 UUID 或雪花 ID）
     * 数据库字段：id VARCHAR(64)
     *
     * 文章短链接 abbrlink
     *
     */
    private String id;

    /**
     * 文章标题
     * 数据库字段：title VARCHAR(255)
     */
    private String title;

    /**
     * 文章正文（支持 Markdown/HTML）
     * 数据库字段：content LONGTEXT
     */
    private String content;

    /**
     * 封面图片 URL（可选，可为 null）
     * 数据库字段：cover VARCHAR(512)
     */
    private String cover;

    /**
     * 创建时间
     * 数据库字段：create_time DATETIME(3)
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     * 数据库字段：update_time DATETIME(3)
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 文章描述
     * 数据库字段：description VARCHAR(255)
     */
    private String description;


    /**
     * 分类名称
     * 数据库字段：category VARCHAR(255)
     */
    private String category;

    /**
     * 标签名称，多个标签用逗号分隔
     * 数据库字段：tags VARCHAR(255)
     */
    private String tags;

    /**
     * 设置 createTime，支持多种日期格式，兼容前端的 date 字段
     * @param value 前端传递的日期字符串，支持 "yyyy-MM-dd" 或 "yyyy-MM-dd HH:mm:ss"
     */
    @JsonProperty("date")
    public void setDate(Object value) {
        if (value == null) {
            this.createTime = null;
            return;
        }

        if (value instanceof LocalDateTime) {
            this.createTime = (LocalDateTime) value;
            return;
        }

        String dateStr = String.valueOf(value);

        // 尝试多种格式
        try {
            // 格式 1: yyyy-MM-dd HH:mm:ss
            this.createTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException e1) {
            try {
                // 格式 2: yyyy-MM-dd (只有日期，时间默认为 00:00:00)
                LocalDate localDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                this.createTime = localDate.atStartOfDay();
            } catch (DateTimeParseException e2) {
                try {
                    // 格式 3: ISO-8601 格式 (带 T 和时区)
                    this.createTime = LocalDateTime.parse(dateStr);
                } catch (DateTimeParseException e3) {
                    throw new RuntimeException("日期格式错误，支持的格式：yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss", e3);
                }
            }
        }
    }

    /**
     * 设置 updateTime，支持多种日期格式，兼容前端的 updated 字段
     * @param value 前端传递的日期字符串，支持 "yyyy-MM-dd" 或 "yyyy-MM-dd HH:mm:ss"
     */
    @JsonProperty("updated")
    public void setUpdated(Object value) {
        if (value == null) {
            // 如果前端没有传 updated，则默认使用 createTime，
            // 如果 createTime 也为空，则使用当前时间，避免数据库 NOT NULL 约束报错
            if (this.createTime != null) {
                this.updateTime = this.createTime;
            } else {
                this.updateTime = LocalDateTime.now();
            }
            return;
        }

        if (value instanceof LocalDateTime) {
            this.updateTime = (LocalDateTime) value;
            return;
        }

        String dateStr = String.valueOf(value);

        // 尝试多种格式
        try {
            // 格式 1: yyyy-MM-dd HH:mm:ss
            this.updateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException e1) {
            try {
                // 格式 2: yyyy-MM-dd (只有日期，时间默认为 00:00:00)
                LocalDate localDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                this.updateTime = localDate.atStartOfDay();
            } catch (DateTimeParseException e2) {
                try {
                    // 格式 3: ISO-8601 格式 (带 T 和时区)
                    this.updateTime = LocalDateTime.parse(dateStr);
                } catch (DateTimeParseException e3) {
                    throw new RuntimeException("日期格式错误，支持的格式：yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss", e3);
                }
            }
        }
    }

    /**
     * 设置 tags，支持数组和字符串两种格式
     * @param value 前端传递的 tags，可以是数组也可以是逗号分隔的字符串
     */
    @JsonProperty("tags")
    public void setTagsFromObject(Object value) {
        if (value == null) {
            this.tags = null;
        } else if (value instanceof String) {
            this.tags = (String) value;
        } else if (value instanceof java.util.List) {
            this.tags = ((java.util.List<?>) value).stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        } else if (value instanceof Object[]) {
            this.tags = Stream.of((Object[]) value)
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        } else {
            this.tags = String.valueOf(value);
        }
    }

    /**
     * 设置 category，支持数组和字符串两种格式
     * @param value 前端传递的 category，可以是数组也可以是逗号分隔的字符串
     */
    @JsonProperty("category")
    public void setCategoryFromObject(Object value) {
        if (value == null) {
            this.category = null;
        } else if (value instanceof String) {
            this.category = (String) value;
        } else if (value instanceof java.util.List) {
            this.category = ((java.util.List<?>) value).stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        } else if (value instanceof Object[]) {
            this.category = Stream.of((Object[]) value)
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        } else {
            this.category = String.valueOf(value);
        }
    }


}
