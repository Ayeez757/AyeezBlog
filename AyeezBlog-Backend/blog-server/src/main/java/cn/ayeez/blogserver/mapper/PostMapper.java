package cn.ayeez.blogserver.mapper;

import cn.ayeez.blogpojo.dto.request.PostBody;
import cn.ayeez.blogpojo.dto.request.PostQueryParam;
import cn.ayeez.blogpojo.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 文章Mapper接口
 */
@Mapper
public interface PostMapper {

    /**
     * 分页查询文章
     */
    List<Post> pages(PostQueryParam queryParam);

    /**
     * 根据ID查询文章
     */
    Post get(String id);

    /**
     * 添加文章
     */
    void add(PostBody postBody);

    /**
     * 更新文章
     */
    void update(PostBody postBody);

    /**
     * 删除文章
     */
    void delete(String id);

    /**
     * 按分类ID查询文章简要列表
     */
    List<Post> listByCategoryId(Long categoryId);

    /**
     * 按标签ID查询文章简要列表
     */
    List<Post> listByTagId(Long tagId);
}
