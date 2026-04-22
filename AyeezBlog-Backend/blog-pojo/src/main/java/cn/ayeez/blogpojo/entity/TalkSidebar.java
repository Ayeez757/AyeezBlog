package cn.ayeez.blogpojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 说说页侧边栏配置（仅一行记录）
 * 对应数据库表：blog_talk_sidebar
 */
@Data
public class TalkSidebar {
    private Long id;

    /** 我的状态，例如：在线/忙碌/离线 */
    private String status;

    /** 心情 */
    private String mood;

    /** 在做 */
    private String doing;

    /** 碎碎念（JSON 数组字符串） */
    private String notes;

    /** 待办（JSON 数组字符串） */
    private String todos;

    private LocalDateTime updatedAt;
}

