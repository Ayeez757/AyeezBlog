package cn.ayeez.blogserver.service.postServer;

import cn.ayeez.blogpojo.dto.request.TalkQueryParam;
import cn.ayeez.blogpojo.dto.response.PageResult;
import cn.ayeez.blogpojo.entity.Talk;

public interface TalkService {
    PageResult<Talk> list(TalkQueryParam queryParam);

    Talk get(Long id);

    void add(Talk talk);

    void update(Talk talk);

    void delete(Long id);
}

