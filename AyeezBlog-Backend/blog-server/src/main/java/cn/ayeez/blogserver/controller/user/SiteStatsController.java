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

    /**
     * 记录一次页面访问。
     * <p>
     * 优先使用请求头中的访客标识；若缺失则使用 IP + UA 兜底生成。
     *
     * @param visitorKey 前端传入的访客唯一标识
     * @param path       当前访问路径
     * @param request    HTTP 请求对象
     * @return 统一返回结果
     */
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

    /**
     * 获取站点访问统计信息。
     *
     * @return 包含浏览量与访客数的统计结果
     */
    @GetMapping
    public Result<SiteVisitStats> stats() {
        return Result.success(siteStatsService.getStats());
    }

    /**
     * 解析客户端真实 IP。
     * <p>
     * 依次尝试 X-Forwarded-For、X-Real-IP，最后回退到 remoteAddr。
     *
     * @param request HTTP 请求对象
     * @return 客户端 IP 地址
     */
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

    /**
     * 基于请求信息构建访客标识兜底值。
     *
     * @param request HTTP 请求对象
     * @return 由 IP 与 User-Agent 组成的访客标识
     */
    private String buildFallbackVisitorKey(HttpServletRequest request) {
        String ip = resolveClientIp(request);
        String ua = request.getHeader("User-Agent");
        if (ua == null) {
            ua = "unknown-agent";
        }
        return (ip + "|" + ua).trim();
    }
}
