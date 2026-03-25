package cn.ayeez.blogpojo.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 管理端：日志版本新增/编辑请求体
 */
@Data
public class LogVersionBody {

    /**
     * 编辑时传；新增时可不传
     */
    private Long id;

    /**
     * 版本号，例如 v1.3.0
     */
    private String version;

    /**
     * 版本日期（格式：yyyy-MM-dd）
     */
    private LocalDate date;

    /**
     * 变更内容列表
     */
    private List<String> changes;
}

