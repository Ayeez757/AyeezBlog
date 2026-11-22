package cn.ayeez.blogserver.controller.user;

import cn.ayeez.blogpojo.entity.Post;
import cn.ayeez.blogserver.mapper.PostMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 动态站点地图（sitemap.xml），供 Google 等搜索引擎发现全部正式文章。
 * 由 Nginx 将 GET /sitemap.xml 反代到本接口。
 */
@Slf4j
@RestController
public class SitemapController {

    private static final DateTimeFormatter W3C_DATETIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+08:00'");

    @Autowired
    private PostMapper postMapper;

    @Value("${blog.site-url:https://blog.ayeez.cn}")
    private String siteUrl;

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemap() {
        String baseUrl = normalizeBaseUrl(siteUrl);
        List<Post> posts = postMapper.listForSitemap();

        StringBuilder xml = new StringBuilder(4096);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        LocalDateTime latestUpdate = null;
        if (posts != null && !posts.isEmpty()) {
            Post first = posts.get(0);
            latestUpdate = first.getUpdateTime() != null ? first.getUpdateTime() : first.getCreateTime();
        }

        appendUrl(xml, baseUrl + "/", latestUpdate, "daily", "1.0");
        appendUrl(xml, baseUrl + "/archive", latestUpdate, "daily", "0.8");
        appendUrl(xml, baseUrl + "/about", null, "monthly", "0.6");

        if (posts != null) {
            for (Post post : posts) {
                if (post == null || post.getId() == null || post.getId().trim().isEmpty()) {
                    continue;
                }
                String encodedId = URLEncoder.encode(post.getId().trim(), StandardCharsets.UTF_8)
                        .replace("+", "%20");
                LocalDateTime lastMod = post.getUpdateTime() != null ? post.getUpdateTime() : post.getCreateTime();
                appendUrl(xml, baseUrl + "/posts/" + encodedId, lastMod, "weekly", "0.9");
            }
        }

        xml.append("</urlset>\n");

        return ResponseEntity.ok()
                .contentType(new MediaType("application", "xml", StandardCharsets.UTF_8))
                .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES).cachePublic())
                .body(xml.toString());
    }

    private String normalizeBaseUrl(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "https://blog.ayeez.cn";
        }
        String trimmed = raw.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private void appendUrl(
            StringBuilder xml,
            String loc,
            LocalDateTime lastMod,
            String changeFreq,
            String priority
    ) {
        xml.append("  <url>\n");
        xml.append("    <loc>").append(escapeXml(loc)).append("</loc>\n");
        if (lastMod != null) {
            xml.append("    <lastmod>")
                    .append(lastMod.atOffset(ZoneOffset.ofHours(8)).format(W3C_DATETIME))
                    .append("</lastmod>\n");
        }
        if (changeFreq != null && !changeFreq.isEmpty()) {
            xml.append("    <changefreq>").append(changeFreq).append("</changefreq>\n");
        }
        if (priority != null && !priority.isEmpty()) {
            xml.append("    <priority>").append(priority).append("</priority>\n");
        }
        xml.append("  </url>\n");
    }

    private String escapeXml(String input) {
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
