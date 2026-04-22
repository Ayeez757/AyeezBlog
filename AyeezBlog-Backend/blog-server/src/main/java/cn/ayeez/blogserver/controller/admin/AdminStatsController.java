package cn.ayeez.blogserver.controller.admin;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.dto.response.SiteTrafficDashboard;
import cn.ayeez.blogpojo.dto.response.SiteTrafficHistoryPoint;
import cn.ayeez.blogpojo.dto.response.SiteVisitStats;
import cn.ayeez.blogserver.service.postServer.SiteStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
    public Result<SiteTrafficDashboard> dashboard(@RequestParam(required = false, defaultValue = "14") Integer days,
                                                 @RequestParam(required = false) String startDate,
                                                 @RequestParam(required = false) String endDate) {
        List<SiteTrafficHistoryPoint> history;

        if (StringUtils.hasText(startDate) && StringUtils.hasText(endDate)) {
            try {
                LocalDate start = LocalDate.parse(startDate.trim());
                LocalDate end = LocalDate.parse(endDate.trim());
                if (start.isAfter(end)) {
                    LocalDate tmp = start;
                    start = end;
                    end = tmp;
                }
                // 防止误传导致大查询（最多 366 天）
                long spanDays = start.datesUntil(end.plusDays(1)).count();
                if (spanDays <= 0) spanDays = 1;
                if (spanDays > 366) {
                    return Result.error(400, "时间跨度过大（最多支持 366 天）");
                }
                log.info("获取管理端仪表盘流量数据，startDate={}, endDate={}", start, end);
                history = siteStatsService.getDailyHistory(start, end);
            } catch (DateTimeParseException e) {
                return Result.error(400, "日期格式错误，请使用 yyyy-MM-dd");
            }
        } else {
            int safeDays = (days == null) ? 14 : days;
            // 防止误传导致大查询
            safeDays = Math.max(1, Math.min(safeDays, 90));
            log.info("获取管理端仪表盘流量数据，days={}", safeDays);
            history = siteStatsService.getDailyHistory(safeDays);
        }

        SiteVisitStats totals = siteStatsService.getStats();

        SiteTrafficDashboard dashboard = new SiteTrafficDashboard();
        dashboard.setPageViews(totals == null ? 0L : totals.getPageViews());
        dashboard.setUniqueVisitors(totals == null ? 0L : totals.getUniqueVisitors());
        dashboard.setHistory(history);

        return Result.success(dashboard);
    }
}

