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

    /**
     * 记录访问并更新统计。
     * <p>
     * 每次请求都会累计 PV；若访客首次出现则累计 UV，否则仅刷新最近访问时间。
     *
     * @param visitorKey 访客唯一标识
     * @param ipAddress  客户端 IP
     * @param userAgent  客户端 User-Agent
     * @param visitPath  访问路径
     */
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

    /**
     * 获取当前站点统计数据。
     *
     * @return 站点访问统计（空值会兜底为 0）
     */
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
