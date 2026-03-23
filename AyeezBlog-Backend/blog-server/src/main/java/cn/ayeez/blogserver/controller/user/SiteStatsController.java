package cn.ayeez.blogserver.controller.user;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.dto.response.SiteVisitStats;
import cn.ayeez.blogserver.service.postServer.SiteStatsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/post/stats")
public class SiteStatsController {

    @Autowired
    private SiteStatsService siteStatsService;

    @PostMapping("/track")
    public Result trackVisit(@RequestHeader(value = "X-Visitor-Key", required = false) String visitorKey,
                             @RequestParam(value = "path", required = false, defaultValue = "/") String path,
                             HttpServletRequest request) {
        if (!StringUtils.hasText(visitorKey)) {
            visitorKey = buildFallbackVisitorKey(request);
        }
        String clientIp = resolveClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        siteStatsService.trackVisit(visitorKey, clientIp, userAgent, path);
        return Result.success();
    }

    @GetMapping
    public Result<SiteVisitStats> stats() {
        return Result.success(siteStatsService.getStats());
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private String buildFallbackVisitorKey(HttpServletRequest request) {
        String ip = resolveClientIp(request);
        String ua = request.getHeader("User-Agent");
        if (ua == null) {
            ua = "unknown-agent";
        }
        return (ip + "|" + ua).trim();
    }
}
