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
import java.util.ArrayList;
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

    @GetMapping("/sitemap.xml")
    public ResponseEntity<String> sitemap() {
        StringBuilder xml = new StringBuilder(4096);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        for (SitemapEntry entry : entries()) {
            appendUrl(xml, entry);
        }
        xml.append("</urlset>\n");
        return sitemapResponse(MediaType.APPLICATION_XML, xml.toString());
    }

    @GetMapping("/sitemap.txt")
    public ResponseEntity<String> textSitemap() {
        StringBuilder text = new StringBuilder(4096);
        for (SitemapEntry entry : entries()) {
            text.append(entry.loc()).append('\n');
        }
        return sitemapResponse(MediaType.TEXT_PLAIN, text.toString());
    }

    private ResponseEntity<String> sitemapResponse(MediaType type, String body) {
        return ResponseEntity.ok()
                .contentType(new MediaType(type, StandardCharsets.UTF_8))
                .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES).cachePublic())
                .body(body);
    }

    private List<SitemapEntry> entries() {
        String baseUrl = normalizeBaseUrl(siteUrl);
        List<Post> posts = postMapper.listForSitemap();
        List<SitemapEntry> entries = new ArrayList<>();

        LocalDateTime latestUpdate = null;
        if (posts != null && !posts.isEmpty()) {
            Post first = posts.get(0);
            latestUpdate = first.getUpdateTime() != null ? first.getUpdateTime() : first.getCreateTime();
        }

        entries.add(new SitemapEntry(baseUrl + "/", latestUpdate, "daily", "1.0"));
        entries.add(new SitemapEntry(baseUrl + "/archive", latestUpdate, "daily", "0.8"));
        entries.add(new SitemapEntry(baseUrl + "/about", null, "monthly", "0.6"));

        if (posts != null) {
            for (Post post : posts) {
                if (post == null || post.getId() == null || post.getId().trim().isEmpty()) {
                    continue;
                }
                String encodedId = URLEncoder.encode(post.getId().trim(), StandardCharsets.UTF_8)
                        .replace("+", "%20");
                LocalDateTime lastMod = post.getUpdateTime() != null ? post.getUpdateTime() : post.getCreateTime();
                entries.add(new SitemapEntry(baseUrl + "/posts/" + encodedId, lastMod, "weekly", "0.9"));
            }
        }

        return entries;
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

    private record SitemapEntry(String loc, LocalDateTime lastMod, String changeFreq, String priority) {}

    private void appendUrl(StringBuilder xml, SitemapEntry entry) {
        xml.append("  <url>\n");
        xml.append("    <loc>").append(escapeXml(entry.loc())).append("</loc>\n");
        if (entry.lastMod() != null) {
            xml.append("    <lastmod>")
                    .append(entry.lastMod().atOffset(ZoneOffset.ofHours(8)).format(W3C_DATETIME))
                    .append("</lastmod>\n");
        }
        xml.append("    <changefreq>").append(entry.changeFreq()).append("</changefreq>\n");
        xml.append("    <priority>").append(entry.priority()).append("</priority>\n");
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
