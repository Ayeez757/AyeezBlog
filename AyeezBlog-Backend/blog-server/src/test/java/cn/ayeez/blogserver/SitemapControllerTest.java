package cn.ayeez.blogserver;

import cn.ayeez.blogpojo.entity.Post;
import cn.ayeez.blogserver.controller.user.SitemapController;
import cn.ayeez.blogserver.mapper.PostMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SitemapControllerTest {

    @Test
    void generatesValidSitemapXmlWithPostsAndStaticPages() throws Exception {
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
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("<loc>https://blog.ayeez.cn/</loc>"));
        assertTrue(body.contains("<loc>https://blog.ayeez.cn/archive</loc>"));
        assertTrue(body.contains("<loc>https://blog.ayeez.cn/about</loc>"));
        assertTrue(body.contains("<loc>https://blog.ayeez.cn/posts/mysql-notes</loc>"));
        assertTrue(body.contains("<lastmod>2026-11-10T12:30:00+08:00</lastmod>"));
        assertTrue(body.contains("<loc>https://blog.ayeez.cn/posts/a%26b</loc>"));
        assertFalse(body.contains("/posts/   "));
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
