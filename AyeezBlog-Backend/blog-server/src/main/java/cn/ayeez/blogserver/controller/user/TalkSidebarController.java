package cn.ayeez.blogserver.controller.user;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.entity.TalkSidebar;
import cn.ayeez.blogserver.service.postServer.TalkSidebarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台：说说页侧边栏配置
 */
@RestController
@RequestMapping("/talk/sidebar")
public class TalkSidebarController {

    @Autowired
    private TalkSidebarService talkSidebarService;

    @GetMapping("/get")
    public Result get() {
        TalkSidebar sidebar = talkSidebarService.get();
        return Result.success(sidebar);
    }
}

