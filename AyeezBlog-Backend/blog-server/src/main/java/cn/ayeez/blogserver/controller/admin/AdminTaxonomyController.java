package cn.ayeez.blogserver.controller.admin;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.entity.BlogCategory;
import cn.ayeez.blogpojo.entity.BlogTag;
import cn.ayeez.blogpojo.entity.Post;
import cn.ayeez.blogserver.mapper.BlogCategoryMapper;
import cn.ayeez.blogserver.mapper.BlogTagMapper;
import cn.ayeez.blogserver.mapper.PostMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminTaxonomyController {

    @Autowired
    private BlogCategoryMapper blogCategoryMapper;

    @Autowired
    private BlogTagMapper blogTagMapper;

    @Autowired
    private PostMapper postMapper;

    /**
     * 查看分类列表
     * @param keyword
     * @return
     */
    @GetMapping("/category/list")
    public Result listCategory(@RequestParam(required = false) String keyword) {
        List<BlogCategory> list = blogCategoryMapper.list(keyword);
        return Result.success(list);
    }

    /**
     * 添加分类
     * @param category
     * @return
     */
    @PostMapping("/category/add")
    @Transactional
    public Result addCategory(@RequestBody BlogCategory category) {
        if (category == null || category.getName() == null || category.getName().trim().isEmpty()) {
            return Result.error(400, "分类名称不能为空");
        }
        BlogCategory existing = blogCategoryMapper.findByParentAndName(category.getParentId(), category.getName().trim());
        if (existing != null) {
            return Result.error(400, "同级分类名称已存在");
        }
        category.setName(category.getName().trim());
        if (category.getSort() == null) {
            category.setSort(0);
        }
        blogCategoryMapper.insert(category);
        return Result.success();
    }

    /**
     * 修改分类
     * @param category
     * @return
     */
    @PutMapping("/category/update")
    @Transactional
    public Result updateCategory(@RequestBody BlogCategory category) {
        if (category == null || category.getId() == null) {
            return Result.error(400, "分类ID不能为空");
        }
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            return Result.error(400, "分类名称不能为空");
        }
        category.setName(category.getName().trim());
        blogCategoryMapper.update(category);
        return Result.success();
    }

    /**
     * 删除分类
     * @param id
     * @return
     */
    @DeleteMapping("/category/delete")
    @Transactional
    public Result deleteCategory(@RequestParam Long id) {
        if (id == null) {
            return Result.error(400, "分类ID不能为空");
        }
        blogCategoryMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 查看标签列表
     * @param keyword
     * @return
     */
    @GetMapping("/tag/list")
    public Result listTag(@RequestParam(required = false) String keyword) {
        List<BlogTag> list = blogTagMapper.list(keyword);
        return Result.success(list);
    }

    /**
     * 查看分类下的文章列表
     * @param id
     * @return
     */
    @GetMapping("/category/posts")
    public Result listCategoryPosts(@RequestParam Long id) {
        if (id == null) {
            return Result.error(400, "分类ID不能为空");
        }
        List<Post> posts = postMapper.listByCategoryId(id);
        return Result.success(posts);
    }

    /**
     * 查看标签下的文章列表
     * @param id
     * @return
     */
    @GetMapping("/tag/posts")
    public Result listTagPosts(@RequestParam Long id) {
        if (id == null) {
            return Result.error(400, "标签ID不能为空");
        }
        List<Post> posts = postMapper.listByTagId(id);
        return Result.success(posts);
    }

    /**
     *
     * @param tag
     * @return
     */
    @PostMapping("/tag/add")
    @Transactional
    public Result addTag(@RequestBody BlogTag tag) {
        if (tag == null || tag.getName() == null || tag.getName().trim().isEmpty()) {
            return Result.error(400, "标签名称不能为空");
        }
        BlogTag existing = blogTagMapper.findByName(tag.getName().trim());
        if (existing != null) {
            return Result.error(400, "标签名称已存在");
        }
        tag.setName(tag.getName().trim());
        blogTagMapper.insert(tag);
        return Result.success();
    }

    /**
     * 修改标签
     * @param tag
     * @return
     */
    @PutMapping("/tag/update")
    @Transactional
    public Result updateTag(@RequestBody BlogTag tag) {
        if (tag == null || tag.getId() == null) {
            return Result.error(400, "标签ID不能为空");
        }
        if (tag.getName() == null || tag.getName().trim().isEmpty()) {
            return Result.error(400, "标签名称不能为空");
        }
        tag.setName(tag.getName().trim());
        blogTagMapper.update(tag);
        return Result.success();
    }

    /**
     * 删除标签
     * @param id
     * @return
     */
    @DeleteMapping("/tag/delete")
    @Transactional
    public Result deleteTag(@RequestParam Long id) {
        if (id == null) {
            return Result.error(400, "标签ID不能为空");
        }
        blogTagMapper.deleteById(id);
        return Result.success();
    }
}
