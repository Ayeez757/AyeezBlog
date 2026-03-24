package cn.ayeez.blogserver.service.postServer.Impl;

import cn.ayeez.blogcommon.util.AbbrlinkUtil;
import cn.ayeez.blogpojo.dto.request.PostBody;
import cn.ayeez.blogpojo.dto.request.PostQueryParam;
import cn.ayeez.blogpojo.dto.response.PageResult;
import cn.ayeez.blogpojo.entity.Post;
import cn.ayeez.blogpojo.entity.BlogCategory;
import cn.ayeez.blogpojo.entity.BlogTag;
import cn.ayeez.blogserver.mapper.BlogCategoryMapper;
import cn.ayeez.blogserver.mapper.BlogPostTagMapper;
import cn.ayeez.blogserver.mapper.BlogTagMapper;
import cn.ayeez.blogserver.mapper.PostMapper;
import cn.ayeez.blogserver.service.postServer.PostService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class PostServerImpl implements PostService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private BlogCategoryMapper blogCategoryMapper;

    @Autowired
    private BlogTagMapper blogTagMapper;

    @Autowired
    private BlogPostTagMapper blogPostTagMapper;

    /**
     * 获取文章列表（详细）
     * 包括：文章标题、作者、发布时间、更新时间、分类、标签、阅读数、点赞数、评论数、封面
     *
     * @param queryParam 分页与筛选参数
     * @return 文章分页结果
     */
    @Override
    public PageResult<Post> listDetail(PostQueryParam queryParam) {
        // 兼容前端传参：优先使用 title；若为空则回退到 keyword
        if (queryParam.getTitle() == null || queryParam.getTitle().trim().isEmpty()) {
            if (queryParam.getKeyword() != null && !queryParam.getKeyword().trim().isEmpty()) {
                queryParam.setTitle(queryParam.getKeyword().trim());
            }
        } else {
            queryParam.setTitle(queryParam.getTitle().trim());
        }

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
     *
     * @param id 文章 ID
     * @return 文章详情
     */
    @Override
    public Post get(String id) {
        Post post = postMapper.get(id);
        return post;
    }

    /**
     * 添加文章
     *
     * @param postBody 文章新增参数
     */
    @Override
    @Transactional
    public void add(PostBody postBody) {
        log.info("添加文章前查询id(abbrlink)是否存在，参数：{}",postBody);
        // 1. 解析并落库分类，得到最终 categoryId
        Long categoryId = resolveCategoryId(postBody.getCategories());
        postBody.setCategoryId(categoryId);

        // 2. 生成或复用文章ID
        if(postMapper.get(postBody.getId())!=null && !postBody.getId().isEmpty()){
            postMapper.add(postBody);
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

        // 3. 处理标签：创建不存在的标签，并写入 blog_post_tag 关联表
        syncPostTags(postBody.getId(), postBody.getTags());
    }

    /**
     * 更新文章主体信息、分类与标签关联。
     *
     * @param postBody 文章更新参数
     */
    @Override
    @Transactional
    public void update(PostBody postBody) {
        log.info("更新文章，参数：{}", postBody);
        if (postBody.getId() == null || postBody.getId().isEmpty()) {
            throw new IllegalArgumentException("更新文章时，id 不能为空");
        }

        // 1. 更新分类
        Long categoryId = resolveCategoryId(postBody.getCategories());
        postBody.setCategoryId(categoryId);

        // 2. 更新文章主体
        postMapper.update(postBody);

        // 3. 同步标签关联
        syncPostTags(postBody.getId(), postBody.getTags());
    }

    /**
     * 删除文章及其标签关联关系。
     *
     * @param id 文章 ID
     */
    @Override
    public void delete(String id) {
        // 先删除标签关联，再删除文章
        blogPostTagMapper.deleteByPostId(id);
        postMapper.delete(id);
    }

    /**
     * 解析分类路径（categories）为最终的分类ID：
     *  - 按顺序依次解析/创建 parent_id + name
     *  - 返回最后一个（叶子节点）的 ID
     *
     * @param categories 分类路径名称集合
     * @return 叶子分类 ID，若为空则返回 null
     */
    private Long resolveCategoryId(java.util.List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }
        Long parentId = null;
        Long currentId = null;
        for (String rawName : categories) {
            if (rawName == null) continue;
            String name = rawName.trim();
            if (name.isEmpty()) continue;

            BlogCategory existing = blogCategoryMapper.findByParentAndName(parentId, name);
            if (existing == null) {
                BlogCategory cat = new BlogCategory();
                cat.setParentId(parentId);
                cat.setName(name);
                cat.setSort(0);
                blogCategoryMapper.insert(cat);
                currentId = cat.getId();
            } else {
                currentId = existing.getId();
            }
            parentId = currentId;
        }
        return currentId;
    }

    /**
     * 同步文章标签：
     *  - 根据名称查找/创建 blog_tag
     *  - 清空原有关联
     *  - 重新插入 blog_post_tag
     *
     * @param postId   文章 ID
     * @param tagNames 标签名称集合
     */
    private void syncPostTags(String postId, java.util.List<String> tagNames) {
        if (postId == null || postId.isEmpty()) {
            return;
        }

        // 删除旧关联
        blogPostTagMapper.deleteByPostId(postId);

        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }

        java.util.List<Long> tagIds = new java.util.ArrayList<>();
        for (String rawName : tagNames) {
            if (rawName == null) continue;
            String name = rawName.trim();
            if (name.isEmpty()) continue;

            BlogTag tag = blogTagMapper.findByName(name);
            if (tag == null) {
                tag = new BlogTag();
                tag.setName(name);
                blogTagMapper.insert(tag);
            }
            if (tag.getId() != null) {
                tagIds.add(tag.getId());
            }
        }

        if (!tagIds.isEmpty()) {
            blogPostTagMapper.insertBatch(postId, tagIds);
        }
    }

//    @Override
//    public void update(PostBody postBody) {
//        if (postBody == null || postBody.getId() == null || postBody.getId().isEmpty()) {
//            throw new RuntimeException("文章ID不能为空");
//        }
//        if (postMapper.get(postBody.getId()) == null) {
//            throw new RuntimeException("文章不存在，无法更新");
//        }
//        postMapper.update(postBody);
//    }
}




