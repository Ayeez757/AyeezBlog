package cn.ayeez.blogpojo.dto.request;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

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
     * 解析后的最终分类ID（服务层解析 categories 后填充，用于持久化到 blog_post.category_id）
     */
    private Long categoryId;

    /**
     * 分类路径（层级数组，从父到子）
     * 示例：["学习","博客"]
     */
    @JsonAlias({"category"})
    private List<String> categories;

    /**
     * 标签数组
     * 示例：["笔记","图床"]
     */
    private List<String> tags;

    /**
     * 设置 createTime，支持多种日期格式，兼容前端的 date 字段
     * @param value 前端传递的日期字符串，支持 "yyyy-MM-dd" 或 "yyyy-MM-dd HH:mm:ss"
     */
    @JsonProperty("date")
    public void setDate(Object value) {
        this.createTime = parseToLocalDateTime(value, true);
    }

    /**
     * 设置 updateTime，支持多种日期格式，兼容前端的 updated 字段
     * @param value 前端传递的日期字符串，支持 "yyyy-MM-dd" 或 "yyyy-MM-dd HH:mm:ss"
     */
    @JsonProperty("updated")
    public void setUpdated(Object value) {
        LocalDateTime parsed = parseToLocalDateTime(value, false);
        if (parsed == null) {
            // 没传 updated：默认等于 createTime；若 createTime 也没有则取当前时间
            this.updateTime = (this.createTime != null) ? this.createTime : LocalDateTime.now();
            return;
        }
        this.updateTime = parsed;
    }

    /**
     * 兼容历史：tags 可能由前端传数组/字符串，统一转为 List<String>
     */
    @JsonProperty("tags")
    public void setTagsFromObject(Object value) {
        this.tags = normalizeToStringList(value);
    }

    /**
     * 兼容历史：categories/category 可能由前端传数组/字符串，统一转为 List<String>
     */
    @JsonProperty("categories")
    public void setCategoriesFromObject(Object value) {
        this.categories = normalizeToStringList(value);
    }

    private static final DateTimeFormatter FLEX_DATE_TIME = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .optionalStart()
            .appendPattern(" HH:mm:ss")
            .optionalEnd()
            .toFormatter();

    private LocalDateTime parseToLocalDateTime(Object value, boolean allowNull) {
        if (value == null) return allowNull ? null : null;

        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        if (value instanceof LocalDate) return ((LocalDate) value).atStartOfDay();
        if (value instanceof java.util.Date) {
            return LocalDateTime.ofInstant(((java.util.Date) value).toInstant(), ZoneId.systemDefault());
        }
        if (value instanceof Number) {
            // 兼容时间戳（毫秒）
            long epochMillis = ((Number) value).longValue();
            return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
        }

        String dateStr = String.valueOf(value).trim();
        if (dateStr.isEmpty()) return null;

        // 1) yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss（你的管理端 date-picker + 你 front-matter 的时间）
        try {
            if (dateStr.length() <= 10) {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
            }
            return LocalDateTime.parse(dateStr, FLEX_DATE_TIME);
        } catch (DateTimeParseException ignore) {
            // fallthrough
        }

        // 2) ISO-8601（2026-02-13T15:56:00 / 带时区的情况）
        try {
            return LocalDateTime.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("日期格式错误，支持：yyyy-MM-dd、yyyy-MM-dd HH:mm:ss 或 ISO-8601", e);
        }
    }

    private List<String> normalizeToStringList(Object value) {
        if (value == null) return null;
        List<String> result = new ArrayList<>();
        if (value instanceof List) {
            for (Object o : (List<?>) value) {
                if (o == null) continue;
                String s = String.valueOf(o).trim();
                if (!s.isEmpty()) result.add(s);
            }
            return result;
        }
        if (value instanceof Object[]) {
            for (Object o : (Object[]) value) {
                if (o == null) continue;
                String s = String.valueOf(o).trim();
                if (!s.isEmpty()) result.add(s);
            }
            return result;
        }
        if (value instanceof String) {
            // 兼容旧：逗号分隔
            String s = ((String) value).trim();
            if (s.isEmpty()) return result;
            for (String part : s.split(",")) {
                String p = part.trim();
                if (!p.isEmpty()) result.add(p);
            }
            return result;
        }
        String s = String.valueOf(value).trim();
        if (!s.isEmpty()) result.add(s);
        return result;
    }

}
