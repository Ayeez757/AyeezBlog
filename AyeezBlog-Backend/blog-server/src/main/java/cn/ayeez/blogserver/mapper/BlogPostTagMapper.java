package cn.ayeez.blogserver.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BlogPostTagMapper {

    void deleteByPostId(@Param("postId") String postId);

    void insertBatch(@Param("postId") String postId,
                     @Param("tagIds") List<Long> tagIds);
}

