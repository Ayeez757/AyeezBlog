package cn.ayeez.blogserver.controller.admin;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.dto.request.PostBody;
import cn.ayeez.blogpojo.dto.request.PostQueryParam;
import cn.ayeez.blogpojo.dto.response.PageResult;
import cn.ayeez.blogpojo.entity.Post;
import cn.ayeez.blogserver.service.postServer.PostServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理文章的controller
 */
@Slf4j
@RequestMapping("/admin/post")
@RestController
public class AdminPostController {

    @Autowired
    private PostServer postServer;

    /**
     * 管理端添加文章
     * @param postBody
     * {
     *   "title": "Obsidian+七牛云+PicGo配置教程，实现图片自动上传，分享笔记不再丢图",
     *   "tags": [
     *     "笔记",
     *     "图床",
     *     "博客",
     *     "七牛云"
     *   ],
     *   "category": "学习,博客",
     *   "description": "Obsidian分享笔记图片全裂？七牛云+PicGo+Image Auto Upload，搭建个人图床，粘贴即上传，图片自动转在线链接，发博客、发文件永不丢图。",
     *   "cover": "https://blog.ayeez.cn/imgs/bg/p4.jpg",
     *   "abbrlink": "f50487ff",
     *   "date": "2026-02-13",
     *   "updated": "2026-02-13",
     *   "content": "..."
     *   }
     */
    @PostMapping("/add")
    public Result add(@RequestBody PostBody postBody){
        log.info("添加文章，参数：{}",postBody);
        postServer.add(postBody);
        return Result.success();
    }


    //TODO 管理端删除文章接口待开发
    @DeleteMapping("/delete")
    public Result delete(String id){
        log.info("删除文章，id：{}",id);
        postServer.delete(id);
        return Result.success();
    }


    //TODO 管理端修改文章接口待开发


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
