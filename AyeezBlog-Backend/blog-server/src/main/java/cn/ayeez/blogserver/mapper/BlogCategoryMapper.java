package cn.ayeez.blogserver.mapper;

import cn.ayeez.blogpojo.entity.BlogCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BlogCategoryMapper {

    BlogCategory findByParentAndName(@Param("parentId") Long parentId,
                                     @Param("name") String name);

    void insert(BlogCategory category);

    List<BlogCategory> list(@Param("keyword") String keyword);

    void update(BlogCategory category);

    void deleteById(@Param("id") Long id);
}

