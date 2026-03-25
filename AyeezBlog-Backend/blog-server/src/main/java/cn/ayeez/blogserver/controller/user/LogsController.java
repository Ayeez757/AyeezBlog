package cn.ayeez.blogserver.controller.user;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.dto.response.LogVersionDetail;
import cn.ayeez.blogserver.service.postServer.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 日志查询（前台展示使用）
 */
@Slf4j
@RestController
@RequestMapping("/logs")
public class LogsController {

    @Autowired
    private LogService logService;

    /**
     * 获取日志版本列表（包含当前标记）
     */
    @GetMapping
    public Result list(@RequestParam(required = false) String keyword) {
        log.info("获取日志列表，keyword={}", keyword);
        List<LogVersionDetail> result = logService.list(keyword);
        return Result.success(result);
    }

    /**
     * 获取当前生效版本
     */
    @GetMapping("/current")
    public Result current() {
        LogVersionDetail detail = logService.current();
        return Result.success(detail);
    }
}

