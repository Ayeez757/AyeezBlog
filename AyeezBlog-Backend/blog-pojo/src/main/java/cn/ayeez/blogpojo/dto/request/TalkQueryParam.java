package cn.ayeez.blogpojo.dto.request;

import lombok.Data;

/**
 * 说说分页/筛选参数
 */
@Data
public class TalkQueryParam {
    private Integer page = 1;
    private Integer pageSize = 10;

    /** 模糊搜索内容 */
    private String keyword;

    /** 是否已发布：null=全部；0=草稿/下线；1=已发布 */
    private Integer published;

    /** 排序字段：created_at / updated_at / id */
    private String orderBy = "created_at";

    /** asc / desc */
    private String orderType = "desc";
}

