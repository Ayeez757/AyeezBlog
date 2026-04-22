package cn.ayeez.blogserver.controller.admin;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.dto.request.TalkQueryParam;
import cn.ayeez.blogpojo.dto.response.PageResult;
import cn.ayeez.blogpojo.entity.Talk;
import cn.ayeez.blogserver.service.postServer.TalkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/talk")
public class AdminTalkController {

    @Autowired
    private TalkService talkService;

    @GetMapping("/list")
    public Result list(TalkQueryParam queryParam) {
        PageResult<Talk> result = talkService.list(queryParam);
        return Result.success(result);
    }

    @GetMapping("/get")
    public Result get(@RequestParam Long id) {
        Talk talk = talkService.get(id);
        if (talk == null) {
            return Result.error(404, "说说不存在");
        }
        return Result.success(talk);
    }

    @PostMapping("/add")
    public Result add(@RequestBody Talk talk) {
        talkService.add(talk);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody Talk talk) {
        talkService.update(talk);
        return Result.success();
    }

    @DeleteMapping("/delete")
    public Result delete(@RequestParam Long id) {
        talkService.delete(id);
        return Result.success();
    }
}

