package cn.ayeez.blogserver.service.postServer.Impl;

import cn.ayeez.blogpojo.dto.request.PostQueryParam;
import cn.ayeez.blogpojo.dto.response.PageResult;
import cn.ayeez.blogpojo.entity.Post;
import cn.ayeez.blogserver.mapper.PostMapper;
import cn.ayeez.blogserver.service.postServer.PostServer;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostServerImpl implements PostServer {

    @Autowired
    private PostMapper postMapper;

    /**
     * 获取文章列表（详细）
     * 包括：文章标题、作者、发布时间、更新时间、分类、标签、阅读数、点赞数、评论数、封面
     */
    @Override
    public PageResult<Post> listDetail(PostQueryParam queryParam) {
        // 开启分页
        PageHelper.startPage(queryParam.getPage(), queryParam.getPageSize());

        // 根据查询条件执行查询（这里简化处理，实际应该根据参数动态构建SQL）
        List<Post> postList = postMapper.pages(queryParam);

        // 获取分页信息
        Page<Post> p = (Page<Post>) postList;

        // 构造返回结果
        return new PageResult<>(p.getTotal(), p.getResult());
    }
}




