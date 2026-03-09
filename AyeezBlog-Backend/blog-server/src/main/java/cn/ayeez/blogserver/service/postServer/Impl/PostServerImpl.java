package cn.ayeez.blogserver.service.postServer.Impl;

import cn.ayeez.blogcommon.util.AbbrlinkUtil;
import cn.ayeez.blogpojo.dto.request.PostBody;
import cn.ayeez.blogpojo.dto.request.PostQueryParam;
import cn.ayeez.blogpojo.dto.response.PageResult;
import cn.ayeez.blogpojo.entity.Post;
import cn.ayeez.blogserver.mapper.PostMapper;
import cn.ayeez.blogserver.service.postServer.PostServer;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
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

    /**
     * 根据ID获取文章
     * @param id
     * @return
     */
    @Override
    public Post get(String id) {
        Post post = postMapper.get(id);
        return post;
    }

    /**
     * 添加文章
     * @param postBody
     */
    @Override
    public void add(PostBody postBody) {
        log.info("添加文章前查询id(abbrlink)是否存在，参数：{}",postBody);
        if(postMapper.get(postBody.getId())!=null && !postBody.getId().isEmpty()){
            postMapper.add(postBody);
            return;
        }else{
            //TODO 反复查数据库性能低下，后续可用redis优化（不过优先度不高，因为uuid一般不撞）
            int retryCount = 0;
            String id = AbbrlinkUtil.getShortUrl();
            while (postMapper.get(id) != null && retryCount < 10) {
                retryCount++;
            }
            if (retryCount >= 10) {
                throw new RuntimeException("生成短链接失败，请重试");
            }
            log.info("生成短链接成功，id(abbrlink)：{}",id);
            postBody.setId(id);
            postMapper.add(postBody);
        }
    }

    @Override
    public void delete(String id) {
        postMapper.delete(id);
    }
}




