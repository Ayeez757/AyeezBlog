package cn.ayeez.blogserver.service.postServer.Impl;

import cn.ayeez.blogpojo.dto.response.SiteTrafficHistoryPoint;
import cn.ayeez.blogpojo.dto.response.SiteVisitStats;
import cn.ayeez.blogserver.mapper.SiteStatsMapper;
import cn.ayeez.blogserver.service.postServer.SiteStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        LocalDate statDate = LocalDate.now();

        siteStatsMapper.initStatsRow();
        siteStatsMapper.increasePageViews();
        int inserted = siteStatsMapper.insertVisitor(visitorKey, ipAddress, userAgent, visitPath);
        if (inserted == 0) {
            siteStatsMapper.touchVisitor(visitorKey);
        }

        // 每次访问都累积当日 PV/UV（UV：同一天内首次出现的 visitorKey 记为一次）
        siteStatsMapper.increasePageViewsDaily(statDate);
        int insertedDaily = siteStatsMapper.insertVisitorDaily(visitorKey, statDate);
        if (insertedDaily > 0) {
            siteStatsMapper.increaseUniqueVisitorsDaily(statDate);
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

    @Override
    public List<SiteTrafficHistoryPoint> getDailyHistory(int days) {
        if (days <= 0) {
            days = 1;
        }

        List<SiteTrafficHistoryPoint> dbRows = siteStatsMapper.getDailyHistory(days);
        Map<String, SiteTrafficHistoryPoint> map = new HashMap<>();
        if (dbRows != null) {
            for (SiteTrafficHistoryPoint p : dbRows) {
                if (p != null && p.getDate() != null) {
                    map.put(p.getDate(), p);
                }
            }
        }

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1L);

        List<SiteTrafficHistoryPoint> out = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = start.plusDays((long) i);
            String date = d.toString();
            SiteTrafficHistoryPoint p = map.get(date);
            if (p == null) {
                out.add(new SiteTrafficHistoryPoint(date, 0L, 0L));
            } else {
                if (p.getPageViews() == null) p.setPageViews(0L);
                if (p.getUniqueVisitors() == null) p.setUniqueVisitors(0L);
                out.add(p);
            }
        }
        return out;
    }
}
