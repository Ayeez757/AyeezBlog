package cn.ayeez.blogpojo.entity;

import lombok.Data;

/**
 * 站点更新日志条目实体。
 * 对应数据库表：blog_log_entry
 */
@Data
public class BlogLogEntry {

    private Long id;

    /**
     * 日志版本ID
     */
    private Long versionId;

    /**
     * 排序（用于保持 changes 列表顺序）
     */
    private Integer sort;

    /**
     * 具体变更内容
     */
    private String content;
}

