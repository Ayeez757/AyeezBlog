package cn.ayeez.blogserver.mapper;

import cn.ayeez.blogpojo.dto.request.TalkQueryParam;
import cn.ayeez.blogpojo.entity.Talk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TalkMapper {
    List<Talk> pages(TalkQueryParam queryParam);

    Talk get(@Param("id") Long id);

    void insert(Talk talk);

    void update(Talk talk);

    void delete(@Param("id") Long id);
}

