package cn.ayeez.blogserver.service.postServer.Impl;

import cn.ayeez.blogpojo.entity.TalkSidebar;
import cn.ayeez.blogserver.mapper.TalkSidebarMapper;
import cn.ayeez.blogserver.service.postServer.TalkSidebarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TalkSidebarServiceImpl implements TalkSidebarService {

    private static final Long SINGLETON_ID = 1L;

    @Autowired
    private TalkSidebarMapper talkSidebarMapper;

    @Override
    public TalkSidebar get() {
        TalkSidebar sidebar = talkSidebarMapper.get(SINGLETON_ID);
        if (sidebar != null) return sidebar;

        // 首次初始化一条默认记录
        TalkSidebar init = new TalkSidebar();
        init.setId(SINGLETON_ID);
        init.setStatus("在线");
        init.setMood("平静");
        init.setDoing("写代码 / 写文章");
        init.setNotes("[\"随手记录：想到什么就发什么\",\"今天也要保持好心情\",\"欢迎来评论区打个招呼\"]");
        init.setTodos("[\"把今天的想法记在说说\",\"整理一篇博客草稿\",\"保持运动/早睡\"]");
        talkSidebarMapper.insert(init);
        return talkSidebarMapper.get(SINGLETON_ID);
    }

    @Override
    public void update(TalkSidebar sidebar) {
        if (sidebar == null) {
            throw new IllegalArgumentException("sidebar 不能为空");
        }
        sidebar.setId(SINGLETON_ID);

        TalkSidebar exist = talkSidebarMapper.get(SINGLETON_ID);
        if (exist == null) {
            talkSidebarMapper.insert(sidebar);
            return;
        }
        talkSidebarMapper.update(sidebar);
    }
}

