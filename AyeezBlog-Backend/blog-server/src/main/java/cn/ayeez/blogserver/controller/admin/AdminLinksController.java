package cn.ayeez.blogserver.controller.admin;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.entity.FriendLinkClass;
import cn.ayeez.blogpojo.entity.Link;
import cn.ayeez.blogserver.service.postServer.LinksService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端友链管理接口
 * 包含：友链分类管理、友链明细管理
 */
@Slf4j
@RestController
@RequestMapping("/admin/links")
public class AdminLinksController {

    @Autowired
    private LinksService linksService;

    /**
     * 获取友链分类列表
     *
     * @param keyword 分类关键字（可选）
     * @return 分类列表
     */
    @GetMapping("/class/list")
    public Result listClass(@RequestParam(required = false) String keyword) {
        return Result.success(linksService.listClasses(keyword));
    }

    /**
     * 新增友链分类
     *
     * @param friendLinkClass 分类信息
     * @return 操作结果
     */
    @PostMapping("/class/add")
    public Result addClass(@RequestBody FriendLinkClass friendLinkClass) {
        return linksService.addClass(friendLinkClass);
    }

    /**
     * 更新友链分类
     *
     * @param friendLinkClass 分类信息（需包含ID）
     * @return 操作结果
     */
    @PutMapping("/class/update")
    public Result updateClass(@RequestBody FriendLinkClass friendLinkClass) {
        return linksService.updateClass(friendLinkClass);
    }

    /**
     * 删除友链分类
     *
     * @param id 分类ID
     * @return 操作结果
     */
    @DeleteMapping("/class/delete")
    public Result deleteClass(@RequestParam Long id) {
        return linksService.deleteClass(id);
    }

    /**
     * 获取友链列表（管理端）
     *
     * @param classId 分类ID（可选）
     * @param keyword 名称/链接关键字（可选）
     * @return 友链列表
     */
    @GetMapping("/link/list")
    public Result listLink(@RequestParam(required = false) Long classId,
                           @RequestParam(required = false) String keyword) {
        return Result.success(linksService.listLinks(classId, keyword));
    }

    /**
     * 新增友链
     *
     * @param link 友链信息
     * @return 操作结果
     */
    @PostMapping("/link/add")
    public Result addLink(@RequestBody Link link) {
        return linksService.addLink(link);
    }

    /**
     * 更新友链
     *
     * @param link 友链信息（需包含ID）
     * @return 操作结果
     */
    @PutMapping("/link/update")
    public Result updateLink(@RequestBody Link link) {
        return linksService.updateLink(link);
    }

    /**
     * 删除友链
     *
     * @param id 友链ID
     * @return 操作结果
     */
    @DeleteMapping("/link/delete")
    public Result deleteLink(@RequestParam Long id) {
        return linksService.deleteLink(id);
    }
}
