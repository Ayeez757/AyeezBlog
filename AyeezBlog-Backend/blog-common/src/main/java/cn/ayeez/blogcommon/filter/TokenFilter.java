package cn.ayeez.blogcommon.filter;


import cn.ayeez.blogcommon.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@WebFilter(urlPatterns = "/admin/*")
public class TokenFilter implements Filter {


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        //强转
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String url = request.getRequestURI();

        // CORS 预检不携带 token，必须放行，否则浏览器会拦截后续 POST（表现为按钮无反应或控制台 CORS/网络错误）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

//        if (!url.contains("/admin")) {
//            log.info("用户端操作，放行");
//            filterChain.doFilter(request, response);
//            return;
//        }
//
        if (url.contains("login")) {
            log.info("登录操作，放行");
            filterChain.doFilter(request, response);
            return;
        }


        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            log.info("未登录，请先登录");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            JwtUtil.parseToken(token);
        } catch (Exception e) {
            log.info("token解析失败，请重新登录");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        log.info("token解析成功，放行");
        filterChain.doFilter(request, response);
        return;
    }
}
