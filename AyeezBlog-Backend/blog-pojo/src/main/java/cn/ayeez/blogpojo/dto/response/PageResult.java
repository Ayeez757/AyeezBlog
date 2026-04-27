package cn.ayeez.blogpojo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * 分页查询
 * @param <T>
 */

@Data
@NoArgsConstructor
public class PageResult<T> {
    private long total;
    private List<T> rows;
    /**
     * 本次查询实际生效的分页大小。
     * <p>
     * 当前后端在运行时根据配置覆盖前端 pageSize 时，前端应使用该值计算分页条页数。
     * </p>
     */
    private Integer pageSize;

    /**
     * 兼容历史调用：仅返回 total 与 rows。
     *
     * @param total 总条数
     * @param rows  当前页数据
     */
    public PageResult(long total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }

    /**
     * 返回 total、rows 以及本次生效的 pageSize。
     *
     * @param total    总条数
     * @param rows     当前页数据
     * @param pageSize 本次生效分页大小
     */
    public PageResult(long total, List<T> rows, Integer pageSize) {
        this.total = total;
        this.rows = rows;
        this.pageSize = pageSize;
    }
}
