package cn.ayeez.blogserver.controller.user;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.entity.FriendLinkGroup;
import cn.ayeez.blogserver.service.postServer.LinksService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 兼容路径：用于仅放行 /post/** 的反向代理配置。
 */
@Slf4j
@RestController
@RequestMapping("/post/links")
public class LinksCompatController {

    @Autowired
    private LinksService linksService;

    @RequestMapping("/list")
    public Result list() {
        log.info("获取友链列表信息（兼容路径）");
        List<FriendLinkGroup> linkList = linksService.list();
        return Result.success(linkList);
    }
}
