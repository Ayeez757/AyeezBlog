package cn.ayeez.blogserver.service.postServer;


import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.entity.FriendLinkClass;
import cn.ayeez.blogpojo.entity.FriendLinkGroup;
import cn.ayeez.blogpojo.entity.Link;

import java.util.List;

public interface LinksService {


    /**
     * 获取友链分组列表（包含每组下的友链集合）
     *
     * @return 友链分组列表
     */
    List<FriendLinkGroup> list();

    /**
     * 获取友链分类列表（管理端）
     *
     * @param keyword 分类关键字（可选）
     * @return 分类列表
     */
    List<FriendLinkClass> listClasses(String keyword);

    /**
     * 新增友链分类（管理端）
     *
     * @param friendLinkClass 分类信息
     * @return 操作结果
     */
    Result addClass(FriendLinkClass friendLinkClass);

    /**
     * 更新友链分类（管理端）
     *
     * @param friendLinkClass 分类信息（需包含ID）
     * @return 操作结果
     */
    Result updateClass(FriendLinkClass friendLinkClass);

    /**
     * 删除友链分类（管理端）
     *
     * @param id 分类ID
     * @return 操作结果
     */
    Result deleteClass(Long id);

    /**
     * 获取友链列表（管理端）
     *
     * @param classId 分类ID（可选）
     * @param keyword 友链名称/链接关键字（可选）
     * @return 友链列表
     */
    List<Link> listLinks(Long classId, String keyword);

    /**
     * 新增友链（管理端）
     *
     * @param link 友链信息
     * @return 操作结果
     */
    Result addLink(Link link);

    /**
     * 更新友链（管理端）
     *
     * @param link 友链信息（需包含ID）
     * @return 操作结果
     */
    Result updateLink(Link link);

    /**
     * 删除友链（管理端）
     *
     * @param id 友链ID
     * @return 操作结果
     */
    Result deleteLink(Long id);
}
