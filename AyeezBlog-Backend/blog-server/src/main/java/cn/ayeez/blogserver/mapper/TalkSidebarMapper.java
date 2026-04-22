package cn.ayeez.blogserver.mapper;

import cn.ayeez.blogpojo.entity.TalkSidebar;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TalkSidebarMapper {

    TalkSidebar get(@Param("id") Long id);

    void insert(TalkSidebar sidebar);

    void update(TalkSidebar sidebar);
}

