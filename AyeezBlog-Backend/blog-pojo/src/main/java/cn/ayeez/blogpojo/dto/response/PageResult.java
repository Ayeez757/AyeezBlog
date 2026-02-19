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
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    private long total;
    private List<T> rows;
}
