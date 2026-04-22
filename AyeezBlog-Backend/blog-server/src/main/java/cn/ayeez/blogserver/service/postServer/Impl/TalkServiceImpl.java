package cn.ayeez.blogserver.service.postServer.Impl;

import cn.ayeez.blogpojo.dto.request.TalkQueryParam;
import cn.ayeez.blogpojo.dto.response.PageResult;
import cn.ayeez.blogpojo.entity.Talk;
import cn.ayeez.blogserver.mapper.TalkMapper;
import cn.ayeez.blogserver.service.postServer.TalkService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TalkServiceImpl implements TalkService {

    @Autowired
    private TalkMapper talkMapper;

    @Override
    public PageResult<Talk> list(TalkQueryParam queryParam) {
        if (queryParam == null) {
            queryParam = new TalkQueryParam();
        }
        if (queryParam.getPage() == null || queryParam.getPage() <= 0) {
            queryParam.setPage(1);
        }
        if (queryParam.getPageSize() == null || queryParam.getPageSize() <= 0) {
            queryParam.setPageSize(10);
        }
        if (queryParam.getKeyword() != null) {
            queryParam.setKeyword(queryParam.getKeyword().trim());
        }

        PageHelper.startPage(queryParam.getPage(), queryParam.getPageSize());
        List<Talk> list = talkMapper.pages(queryParam);
        Page<Talk> p = (Page<Talk>) list;
        return new PageResult<>(p.getTotal(), p.getResult());
    }

    @Override
    public Talk get(Long id) {
        if (id == null) return null;
        return talkMapper.get(id);
    }

    @Override
    public void add(Talk talk) {
        if (talk == null) {
            throw new IllegalArgumentException("talk 不能为空");
        }
        if (talk.getPublished() == null) {
            talk.setPublished(0);
        }
        talkMapper.insert(talk);
    }

    @Override
    public void update(Talk talk) {
        if (talk == null || talk.getId() == null) {
            throw new IllegalArgumentException("更新说说时 id 不能为空");
        }
        talkMapper.update(talk);
    }

    @Override
    public void delete(Long id) {
        if (id == null) return;
        talkMapper.delete(id);
    }
}

