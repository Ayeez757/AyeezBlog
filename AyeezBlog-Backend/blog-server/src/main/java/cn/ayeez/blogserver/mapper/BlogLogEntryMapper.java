package cn.ayeez.blogserver.mapper;

import cn.ayeez.blogpojo.entity.BlogLogEntry;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 日志条目数据访问层：blog_log_entry
 */
public interface BlogLogEntryMapper {

    int deleteByVersionId(@Param("versionId") Long versionId);

    int insertEntries(@Param("versionId") Long versionId, @Param("changes") List<String> changes);

    List<BlogLogEntry> listEntriesByVersionId(@Param("versionId") Long versionId);
}

