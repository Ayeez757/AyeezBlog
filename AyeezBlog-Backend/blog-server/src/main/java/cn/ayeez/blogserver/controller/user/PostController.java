package cn.ayeez.blogserver.controller.user;


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
 * @author: 阿叶Ayeez
 * 博客文章controller
 */
@Slf4j
@RestController
@RequestMapping("/post")
public class PostController {

    // 注入服务
    @Autowired
    private PostServer postServer;

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

    /**
     * 根据id获取文章
     */
    @GetMapping("/get")
    public Result get(String id) {
        log.info("获取id为{}的文章",id);
        Post post = postServer.get(id);
        return Result.success(post);
    }

}
