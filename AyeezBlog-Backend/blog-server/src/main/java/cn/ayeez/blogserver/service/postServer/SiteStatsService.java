package cn.ayeez.blogserver.service.postServer;

import cn.ayeez.blogpojo.dto.response.SiteVisitStats;

public interface SiteStatsService {
    /**
     * 记录一次站点访问行为。
     *
     * @param visitorKey 访客唯一标识
     * @param ipAddress  客户端 IP
     * @param userAgent  客户端 User-Agent
     * @param visitPath  访问路径
     */
    void trackVisit(String visitorKey, String ipAddress, String userAgent, String visitPath);

    /**
     * 获取站点访问统计信息。
     *
     * @return 站点访问统计
     */
    SiteVisitStats getStats();
}
