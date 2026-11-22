package cn.ayeez.blogserver;

import cn.ayeez.blogpojo.entity.Post;
import cn.ayeez.blogserver.controller.user.SitemapController;
import cn.ayeez.blogserver.mapper.PostMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SitemapControllerTest {

    @Test
    void generatesBothSitemapFormatsWithPostsAndStaticPages() throws Exception {
        Post p1 = new Post();
        p1.setId("mysql-notes");
        p1.setUpdateTime(LocalDateTime.of(2026, 11, 10, 12, 30, 0));

        Post p2 = new Post();
        p2.setId("a&b");
        p2.setCreateTime(LocalDateTime.of(2026, 10, 1, 8, 0, 0));

        Post empty = new Post();
        empty.setId("   ");

        List<Post> stubPosts = Arrays.asList(p1, p2, empty);
        PostMapper mapperStub = (PostMapper) Proxy.newProxyInstance(
                PostMapper.class.getClassLoader(),
                new Class<?>[]{PostMapper.class},
                (proxy, method, args) -> {
                    if ("listForSitemap".equals(method.getName())) {
                        return stubPosts;
                    }
                    throw new UnsupportedOperationException("Unexpected call: " + method.getName());
                }
        );

        SitemapController controller = new SitemapController();
        setField(controller, "postMapper", mapperStub);
        setField(controller, "siteUrl", "https://blog.ayeez.cn/");

        ResponseEntity<String> response = controller.sitemap();
        assertEquals(200, response.getStatusCode().value());
        assertEquals(new MediaType(MediaType.APPLICATION_XML, StandardCharsets.UTF_8),
                response.getHeaders().getContentType());
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("<loc>https://blog.ayeez.cn/</loc>"));
        assertTrue(body.contains("<loc>https://blog.ayeez.cn/archive</loc>"));
        assertTrue(body.contains("<loc>https://blog.ayeez.cn/about</loc>"));
        assertTrue(body.contains("<loc>https://blog.ayeez.cn/posts/mysql-notes</loc>"));
        assertTrue(body.contains("<lastmod>2026-11-10T12:30:00+08:00</lastmod>"));
        assertTrue(body.contains("<loc>https://blog.ayeez.cn/posts/a%26b</loc>"));
        assertFalse(body.contains("/posts/   "));

        ResponseEntity<String> text = controller.textSitemap();
        assertEquals(200, text.getStatusCode().value());
        assertEquals(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8),
                text.getHeaders().getContentType());
        assertEquals("""
                https://blog.ayeez.cn/
                https://blog.ayeez.cn/archive
                https://blog.ayeez.cn/about
                https://blog.ayeez.cn/posts/mysql-notes
                https://blog.ayeez.cn/posts/a%26b
                """, text.getBody());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
