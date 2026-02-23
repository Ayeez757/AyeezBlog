package cn.ayeez.blogserver.mapper;

import cn.ayeez.blogpojo.dto.request.PostQueryParam;
import cn.ayeez.blogpojo.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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
}
