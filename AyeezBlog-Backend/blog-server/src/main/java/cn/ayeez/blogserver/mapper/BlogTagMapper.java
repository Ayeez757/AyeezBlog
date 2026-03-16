package cn.ayeez.blogserver.mapper;

import cn.ayeez.blogpojo.entity.BlogTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BlogTagMapper {

    BlogTag findByName(@Param("name") String name);

    void insert(BlogTag tag);

    List<BlogTag> findByNames(@Param("names") List<String> names);
}

