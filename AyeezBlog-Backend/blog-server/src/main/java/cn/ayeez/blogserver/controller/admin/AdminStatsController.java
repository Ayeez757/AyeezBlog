package cn.ayeez.blogserver.controller.admin;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.dto.response.SiteTrafficDashboard;
import cn.ayeez.blogpojo.dto.response.SiteTrafficHistoryPoint;
import cn.ayeez.blogpojo.dto.response.SiteVisitStats;
import cn.ayeez.blogserver.service.postServer.SiteStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端控制台：站点流量数据。
 */
@Slf4j
@RestController
@RequestMapping("/admin/stats")
public class AdminStatsController {

    @Autowired
    private SiteStatsService siteStatsService;

    /**
     * 管理端首页仪表盘数据（PV/UV + 最近 N 天曲线）。
     *
     * @param days 历史天数（包含当天）
     */
    @GetMapping("/dashboard")
    public Result<SiteTrafficDashboard> dashboard(@RequestParam(required = false, defaultValue = "14") Integer days) {
        int safeDays = (days == null) ? 14 : days;
        // 防止误传导致大查询
        safeDays = Math.max(1, Math.min(safeDays, 90));

        log.info("获取管理端仪表盘流量数据，days={}", safeDays);

        SiteVisitStats totals = siteStatsService.getStats();
        List<SiteTrafficHistoryPoint> history = siteStatsService.getDailyHistory(safeDays);

        SiteTrafficDashboard dashboard = new SiteTrafficDashboard();
        dashboard.setPageViews(totals == null ? 0L : totals.getPageViews());
        dashboard.setUniqueVisitors(totals == null ? 0L : totals.getUniqueVisitors());
        dashboard.setHistory(history);

        return Result.success(dashboard);
    }
}

