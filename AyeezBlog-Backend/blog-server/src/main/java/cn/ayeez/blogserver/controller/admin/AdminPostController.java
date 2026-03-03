package cn.ayeez.blogserver.controller.admin;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.dto.request.PostQueryParam;
import cn.ayeez.blogpojo.dto.response.PageResult;
import cn.ayeez.blogpojo.entity.Post;
import cn.ayeez.blogserver.service.postServer.PostServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理文章的controller
 */
@Slf4j
@RequestMapping("/admin/post")
@RestController
public class AdminPostController {

    @Autowired
    private PostServer postServer;

    //TODO 管理端增加文章接口待开发

    //TODO 管理端删除文章接口待开发

    //TODO 管理端修改文章接口待开发

    //TODO 管理端查询文章列表接口待开发
    /**
     * 获取文章列表（详细）
     * 包括：文章标题、作者、发布时间、更新时间、分类、标签、阅读数、点赞数、评论数、封面
     */
    @GetMapping("/list")
    public Result list(PostQueryParam queryParam) {
        log.info("获取文章列表，参数：{}", queryParam);
        PageResult<Post> result = postServer.listDetail(queryParam);
        return Result.success(result);
    }

    //TODO 管理端查询文章详情接口待开发

}
