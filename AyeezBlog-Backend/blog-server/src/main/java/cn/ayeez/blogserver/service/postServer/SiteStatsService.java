package cn.ayeez.blogserver.service.postServer;

import cn.ayeez.blogpojo.dto.response.SiteVisitStats;

public interface SiteStatsService {
    void trackVisit(String visitorKey, String ipAddress, String userAgent, String visitPath);

    SiteVisitStats getStats();
}
