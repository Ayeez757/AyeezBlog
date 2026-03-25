package cn.ayeez.blogserver.mapper;

import cn.ayeez.blogpojo.entity.BlogLogVersion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 日志版本数据访问层：blog_log_version
 */
public interface BlogLogVersionMapper {

    int insert(BlogLogVersion version);

    int update(BlogLogVersion version);

    int deleteById(@Param("id") Long id);

    BlogLogVersion selectById(@Param("id") Long id);

    List<BlogLogVersion> listAll(@Param("keyword") String keyword);

    BlogLogVersion selectCurrent();

    BlogLogVersion selectLatest();

    int resetCurrent();

    int setCurrentById(@Param("id") Long id);
}

