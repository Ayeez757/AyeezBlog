package cn.ayeez.blogserver.controller.admin;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.dto.request.LogVersionBody;
import cn.ayeez.blogpojo.dto.request.SetCurrentLogBody;
import cn.ayeez.blogpojo.dto.response.LogVersionDetail;
import cn.ayeez.blogserver.service.postServer.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端：日志版本管理
 */
@Slf4j
@RestController
@RequestMapping("/admin/logs")
public class AdminLogsController {

    @Autowired
    private LogService logService;

    @GetMapping("/list")
    public Result list(@RequestParam(required = false) String keyword) {
        log.info("获取日志版本列表，keyword={}", keyword);
        List<LogVersionDetail> result = logService.list(keyword);
        return Result.success(result);
    }

    @GetMapping("/current")
    public Result current() {
        LogVersionDetail detail = logService.current();
        return Result.success(detail);
    }

    @PostMapping("/add")
    public Result add(@RequestBody LogVersionBody body) {
        log.info("新增日志版本，body={}", body);
        return logService.add(body);
    }

    @PutMapping("/update")
    public Result update(@RequestBody LogVersionBody body) {
        log.info("更新日志版本，body={}", body);
        return logService.update(body);
    }

    @DeleteMapping("/delete")
    public Result delete(@RequestParam Long id) {
        log.info("删除日志版本，id={}", id);
        return logService.delete(id);
    }

    @PostMapping("/set-current")
    public Result setCurrent(@RequestBody SetCurrentLogBody body) {
        log.info("设置当前日志版本，id={}", body == null ? null : body.getId());
        if (body == null) {
            return Result.error(400, "参数不能为空");
        }
        return logService.setCurrent(body.getId());
    }
}

