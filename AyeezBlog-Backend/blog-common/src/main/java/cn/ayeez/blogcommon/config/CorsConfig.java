package cn.ayeez.blogcommon.config;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS跨域配置
 * 放在common模块中，供所有微服务共享使用
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 只允许你的站点来源（按需增删）
        // 说明：CORS 校验的是浏览器发起请求时的 Origin（协议+域名+端口），与 Docker network 无关。
        config.setAllowedOrigins(Arrays.asList(
                "https://blog.ayeez.cn",
                "http://blog.ayeez.cn"
        ));

        // 允许的请求头（按需增删；当前管理端用自定义 token 请求头）
        config.setAllowedHeaders(Arrays.asList(
                "Content-Type",
                "Authorization",
                "token"
        ));

        // 允许的请求方法
        config.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        // 是否允许携带凭证（cookies 等）。你的鉴权走 header token，一般无需开启。
        config.setAllowCredentials(false);

        // 预检请求的有效期（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 只给管理端 API 开 CORS（同域访问时浏览器不会触发 CORS，这里主要用于防止外站跨域调用）
        source.registerCorsConfiguration("/admin/**", config);

        return new CorsFilter(source);
    }
}
