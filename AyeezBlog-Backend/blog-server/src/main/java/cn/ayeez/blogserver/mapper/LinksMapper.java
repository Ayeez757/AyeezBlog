package cn.ayeez.blogserver.mapper;

import cn.ayeez.blogpojo.entity.Link;
import cn.ayeez.blogpojo.entity.FriendLinkClass;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LinksMapper {

    /**
     * 查询友链与分类的扁平结果集
     * 说明：Service层会将结果按 classId 聚合成分组结构
     */
    List<Link> list();

    /**
     * 查询友链分类列表（管理端）
     */
    List<FriendLinkClass> listClasses(@Param("keyword") String keyword);

    /**
     * 新增友链分类
     */
    void insertClass(FriendLinkClass friendLinkClass);

    /**
     * 更新友链分类
     */
    void updateClass(FriendLinkClass friendLinkClass);

    /**
     * 删除友链分类
     */
    void deleteClassById(@Param("id") Long id);

    /**
     * 查询友链列表（管理端）
     */
    List<Link> listLinks(@Param("classId") Long classId, @Param("keyword") String keyword);

    /**
     * 新增友链
     */
    void insertLink(Link link);

    /**
     * 更新友链
     */
    void updateLink(Link link);

    /**
     * 删除友链
     */
    void deleteLinkById(@Param("id") Long id);
}
