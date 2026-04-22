package cn.ayeez.blogserver.controller.user;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.dto.request.TalkQueryParam;
import cn.ayeez.blogpojo.dto.response.PageResult;
import cn.ayeez.blogpojo.entity.Talk;
import cn.ayeez.blogserver.service.postServer.TalkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台说说
 */
@Slf4j
@RestController
@RequestMapping("/talk")
public class TalkController {

    @Autowired
    private TalkService talkService;

    /**
     * 前台列表：只返回已发布
     */
    @GetMapping("/list")
    public Result list(TalkQueryParam queryParam) {
        if (queryParam == null) queryParam = new TalkQueryParam();
        queryParam.setPublished(1);
        PageResult<Talk> result = talkService.list(queryParam);
        return Result.success(result);
    }

    /**
     * 前台详情：仅允许读取已发布
     */
    @GetMapping("/get")
    public Result get(@RequestParam Long id) {
        Talk talk = talkService.get(id);
        if (talk == null || talk.getPublished() == null || talk.getPublished() != 1) {
            return Result.error(404, "说说不存在");
        }
        return Result.success(talk);
    }
}

