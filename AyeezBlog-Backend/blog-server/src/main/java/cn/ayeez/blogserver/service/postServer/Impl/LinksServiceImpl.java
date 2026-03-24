package cn.ayeez.blogserver.service.postServer.Impl;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.entity.FriendLinkClass;
import cn.ayeez.blogpojo.entity.FriendLinkGroup;
import cn.ayeez.blogpojo.entity.Link;
import cn.ayeez.blogserver.mapper.LinksMapper;
import cn.ayeez.blogserver.service.postServer.LinksService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LinksServiceImpl implements LinksService {

    @Autowired
    private LinksMapper linksMapper;

    /**
     * 获取前台友链分组列表。
     * 将 mapper 查询出的扁平行数据按 classId 聚合为分组结构。
     *
     * @return 分组后的友链列表
     */
    @Override
    public List<FriendLinkGroup> list() {
        // Mapper返回的是“分类+友链”的扁平行数据
        List<Link> rows = linksMapper.list();
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用LinkedHashMap保留SQL查询得到的顺序
        Map<Long, FriendLinkGroup> groupMap = new LinkedHashMap<>();
        for (Link row : rows) {
            Long classId = row.getClassId();
            if (classId == null) {
                continue;
            }

            // 首次出现该分类时创建分组对象
            FriendLinkGroup group = groupMap.computeIfAbsent(classId, key -> {
                FriendLinkGroup item = new FriendLinkGroup();
                item.setClassId(row.getClassId());
                item.setClassName(row.getClassName());
                item.setClassDesc(row.getClassDesc());
                item.setSort(row.getClassSort());
                return item;
            });

            // LEFT JOIN场景下，分类可能暂无友链（l.id为空）
            if (row.getId() != null) {
                Link link = new Link();
                link.setId(row.getId());
                link.setClassId(row.getClassId());
                link.setName(row.getName());
                link.setLink(row.getLink());
                link.setAvatar(row.getAvatar());
                link.setDescr(row.getDescr());
                link.setRss(row.getRss());
                link.setSort(row.getSort());
                link.setCreatedAt(row.getCreatedAt());
                link.setUpdatedAt(row.getUpdatedAt());
                group.getLinkList().add(link);
            }
        }
        // 最终返回分组数组，便于前端直接按组渲染
        return new ArrayList<>(groupMap.values());
    }

    /**
     * 获取管理端友链分类列表。
     *
     * @param keyword 分类关键字（可选）
     * @return 分类列表
     */
    @Override
    public List<FriendLinkClass> listClasses(String keyword) {
        return linksMapper.listClasses(keyword);
    }

    /**
     * 新增友链分类。
     *
     * @param friendLinkClass 分类信息
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result addClass(FriendLinkClass friendLinkClass) {
        if (friendLinkClass == null || friendLinkClass.getClassName() == null || friendLinkClass.getClassName().trim().isEmpty()) {
            return Result.error(400, "分类名称不能为空");
        }
        friendLinkClass.setClassName(friendLinkClass.getClassName().trim());
        if (friendLinkClass.getSort() == null) {
            friendLinkClass.setSort(0);
        }
        linksMapper.insertClass(friendLinkClass);
        return Result.success();
    }

    /**
     * 更新友链分类。
     *
     * @param friendLinkClass 分类信息（需包含ID）
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result updateClass(FriendLinkClass friendLinkClass) {
        if (friendLinkClass == null || friendLinkClass.getId() == null) {
            return Result.error(400, "分类ID不能为空");
        }
        if (friendLinkClass.getClassName() == null || friendLinkClass.getClassName().trim().isEmpty()) {
            return Result.error(400, "分类名称不能为空");
        }
        friendLinkClass.setClassName(friendLinkClass.getClassName().trim());
        if (friendLinkClass.getSort() == null) {
            friendLinkClass.setSort(0);
        }
        linksMapper.updateClass(friendLinkClass);
        return Result.success();
    }

    /**
     * 删除友链分类。
     *
     * @param id 分类ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result deleteClass(Long id) {
        if (id == null) {
            return Result.error(400, "分类ID不能为空");
        }
        linksMapper.deleteClassById(id);
        return Result.success();
    }

    /**
     * 获取管理端友链列表。
     *
     * @param classId 分类ID（可选）
     * @param keyword 友链名称/链接关键字（可选）
     * @return 友链列表
     */
    @Override
    public List<Link> listLinks(Long classId, String keyword) {
        return linksMapper.listLinks(classId, keyword);
    }

    /**
     * 新增友链。
     *
     * @param link 友链信息
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result addLink(Link link) {
        Result validate = validateLink(link, false);
        if (validate != null) {
            return validate;
        }
        linksMapper.insertLink(link);
        return Result.success();
    }

    /**
     * 更新友链。
     *
     * @param link 友链信息（需包含ID）
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result updateLink(Link link) {
        Result validate = validateLink(link, true);
        if (validate != null) {
            return validate;
        }
        linksMapper.updateLink(link);
        return Result.success();
    }

    /**
     * 删除友链。
     *
     * @param id 友链ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result deleteLink(Long id) {
        if (id == null) {
            return Result.error(400, "友链ID不能为空");
        }
        linksMapper.deleteLinkById(id);
        return Result.success();
    }

    /**
     * 友链参数校验与清洗
     *
     * @param link      友链参数
     * @param requireId 是否要求必须传ID（更新场景）
     * @return 校验失败返回错误结果；成功返回null
     */
    private Result validateLink(Link link, boolean requireId) {
        if (link == null) {
            return Result.error(400, "友链参数不能为空");
        }
        if (requireId && link.getId() == null) {
            return Result.error(400, "友链ID不能为空");
        }
        if (link.getClassId() == null) {
            return Result.error(400, "友链分类不能为空");
        }
        if (link.getName() == null || link.getName().trim().isEmpty()) {
            return Result.error(400, "友链名称不能为空");
        }
        if (link.getLink() == null || link.getLink().trim().isEmpty()) {
            return Result.error(400, "友链地址不能为空");
        }
        link.setName(link.getName().trim());
        link.setLink(link.getLink().trim());
        if (link.getAvatar() != null) {
            link.setAvatar(link.getAvatar().trim());
        }
        if (link.getDescr() != null) {
            link.setDescr(link.getDescr().trim());
        }
        if (link.getRss() != null) {
            link.setRss(link.getRss().trim());
        }
        if (link.getSort() == null) {
            link.setSort(0);
        }
        return null;
    }
}
