package cn.ayeez.blogserver.controller.admin;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.entity.TalkSidebar;
import cn.ayeez.blogserver.service.postServer.TalkSidebarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端：说说侧边栏配置
 */
@RestController
@RequestMapping("/admin/talk/sidebar")
public class AdminTalkSidebarController {

    @Autowired
    private TalkSidebarService talkSidebarService;

    @GetMapping("/get")
    public Result get() {
        return Result.success(talkSidebarService.get());
    }

    @PutMapping("/update")
    public Result update(@RequestBody TalkSidebar sidebar) {
        talkSidebarService.update(sidebar);
        return Result.success();
    }
}

