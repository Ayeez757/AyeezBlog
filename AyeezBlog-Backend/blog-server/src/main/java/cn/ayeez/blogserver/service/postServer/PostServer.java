package cn.ayeez.blogserver.service.postServer;

import cn.ayeez.blogpojo.dto.request.PostQueryParam;
import cn.ayeez.blogpojo.dto.response.PageResult;
import cn.ayeez.blogpojo.entity.Post;

public interface PostServer {

    /**
     * 获取文章列表（详细）
     * 包括：文章标题、作者、发布时间、更新时间、分类、标签、阅读数、点赞数、评论数
     */
    public PageResult<Post> listDetail(PostQueryParam queryParam);

    /**
     * 根据ID获取文章
     */
    Post get(String id);
}
