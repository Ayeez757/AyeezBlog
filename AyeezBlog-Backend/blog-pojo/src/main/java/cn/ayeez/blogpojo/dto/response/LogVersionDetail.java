package cn.ayeez.blogpojo.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 日志版本详情（用于前台/管理端展示）
 */
@Data
public class LogVersionDetail {
    private Long id;
    private String version;
    private LocalDate date;
    private List<String> changes;
    private Boolean current;
}

