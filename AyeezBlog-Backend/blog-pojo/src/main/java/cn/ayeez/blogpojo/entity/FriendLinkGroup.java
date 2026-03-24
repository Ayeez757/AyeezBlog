package cn.ayeez.blogpojo.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 友链分组实体
 * 对应表：friend_link_class
 */
@Data
public class FriendLinkGroup {

    /** 分类ID */
    private Long classId;

    /** 分类名称 */
    private String className;

    /** 分类描述 */
    private String classDesc;

    /** 分类排序 */
    private Integer sort;

    /** 当前分类下的友链列表 */
    private List<Link> linkList = new ArrayList<>();
}
