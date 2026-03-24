package cn.ayeez.blogpojo.entity;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * 友链实体（含分组联查字段）
 * 主体字段对应表：friend_link
 * 分组字段（className/classDesc/classSort）来自 friend_link_class 联表查询
 */
@Data
public class Link {

    /** 友链ID */
    private Long id;

    /** 分类ID（friend_link_class.id） */
    private Long classId;

    /** 分类名称（联查字段） */
    private String className;

    /** 分类描述（联查字段） */
    private String classDesc;

    /** 分类排序（联查字段） */
    private Integer classSort;

    /** 站点名称 */
    private String name;

    /** 站点链接 */
    private String link;

    /** 头像地址 */
    private String avatar;

    /** 站点描述 */
    private String descr;

    /** RSS订阅地址 */
    private String rss;

    /** 友链排序 */
    private Integer sort;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
