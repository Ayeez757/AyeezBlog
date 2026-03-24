package cn.ayeez.blogserver.service.postServer;

import cn.ayeez.blogpojo.dto.request.PostBody;
import cn.ayeez.blogpojo.dto.request.PostQueryParam;
import cn.ayeez.blogpojo.dto.response.PageResult;
import cn.ayeez.blogpojo.entity.Post;

public interface PostService {

    /**
     * 获取文章列表（详细）
     * 包括：文章标题、作者、发布时间、更新时间、分类、标签、阅读数、点赞数、评论数
     *
     * @param queryParam 分页与筛选参数
     * @return 文章分页结果
     */
    public PageResult<Post> listDetail(PostQueryParam queryParam);

    /**
     * 根据ID获取文章
     *
     * @param id 文章 ID
     * @return 文章详情
     */
    Post get(String id);

    /**
     * 添加文章
     *
     * @param postBody 文章新增参数
     */
    void add(PostBody postBody);

    /**
     * 更新文章
     *
     * @param postBody 文章更新参数
     */
    void update(PostBody postBody);

    /**
     * 删除文章
     *
     * @param id 文章 ID
     */
    void delete(String id);
}
