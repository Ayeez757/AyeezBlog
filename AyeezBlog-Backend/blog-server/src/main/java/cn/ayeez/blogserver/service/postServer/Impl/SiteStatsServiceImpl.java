package cn.ayeez.blogserver.service.postServer.Impl;

import cn.ayeez.blogpojo.dto.response.SiteVisitStats;
import cn.ayeez.blogserver.mapper.SiteStatsMapper;
import cn.ayeez.blogserver.service.postServer.SiteStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SiteStatsServiceImpl implements SiteStatsService {

    @Autowired
    private SiteStatsMapper siteStatsMapper;

    @Override
    @Transactional
    public void trackVisit(String visitorKey, String ipAddress, String userAgent, String visitPath) {
        siteStatsMapper.initStatsRow();
        siteStatsMapper.increasePageViews();
        int inserted = siteStatsMapper.insertVisitor(visitorKey, ipAddress, userAgent, visitPath);
        if (inserted == 0) {
            siteStatsMapper.touchVisitor(visitorKey);
        }
    }

    @Override
    public SiteVisitStats getStats() {
        siteStatsMapper.initStatsRow();
        SiteVisitStats stats = siteStatsMapper.getStats();
        if (stats == null) {
            return new SiteVisitStats(0L, 0L);
        }
        if (stats.getPageViews() == null) {
            stats.setPageViews(0L);
        }
        if (stats.getUniqueVisitors() == null) {
            stats.setUniqueVisitors(0L);
        }
        return stats;
    }
}
