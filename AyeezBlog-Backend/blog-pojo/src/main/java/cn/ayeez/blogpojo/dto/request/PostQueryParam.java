package cn.ayeez.blogpojo.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文章分页查询参数
 */
@Data
public class PostQueryParam {
    /**
     * 页码，默认第1页
     */
    private Integer page = 1;
    
    /**
     * 每页大小，默认10条
     */
    private Integer pageSize = 10;
    
    /**
     * 文章标题搜索关键词
     */
    private String title;
    
    /**
     * 开始时间
     */
    private LocalDateTime begin;
    
    /**
     * 结束时间
     */
    private LocalDateTime end;
    
    /**
     * 排序字段
     */
    private String orderBy = "create_time";
    
    /**
     * 排序方式：asc/desc
     */
    private String orderType = "desc";
}
