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
 * 前台友链接口
 */
@Slf4j
@RestController
@RequestMapping("/links")
public class LinksController {

    @Autowired
    private LinksService linksService;

    /**
     * 获取友链分组列表
     * 返回结构：[{className, classDesc, linkList:[...]}]
     *
     * @return 友链分组列表
     */
    @RequestMapping("/list")
    public Result list() {
        log.info("获取友链列表信息");
        List<FriendLinkGroup> linkList = linksService.list();
        return Result.success(linkList);
    }

}
