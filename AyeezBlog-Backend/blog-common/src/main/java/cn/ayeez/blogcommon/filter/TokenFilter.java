package cn.ayeez.blogcommon.filter;


import cn.ayeez.blogcommon.util.JwtUtil;
import cn.ayeez.blogcommon.util.JwtRevocationStore;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@WebFilter(urlPatterns = "/admin/*")
public class TokenFilter implements Filter {

    private static final String ADMIN_LOGIN_PATH = "/admin/login";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        //强转
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String requestUri = request.getRequestURI();
        String servletPath = normalizePath(request, requestUri);

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
        if (ADMIN_LOGIN_PATH.equals(servletPath)) {
            log.info("登录接口放行：{}", servletPath);
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
            if (JwtRevocationStore.isRevoked(token)) {
                log.info("token已吊销，请重新登录");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } catch (Exception e) {
            log.info("token解析失败，请重新登录");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        log.info("token解析成功，放行");
        filterChain.doFilter(request, response);
        return;
    }

    /**
     * 防止误判，比如说防止拦截/admin/login;1这样的路径,本质上还是登录接口，但是不是equals了，所以用处理后的路径来匹配
     */
    private static String normalizePath(HttpServletRequest request, String requestUri) {
        if (requestUri == null || requestUri.isEmpty()) {
            return "";
        }

        String path = requestUri;
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        int semicolonIdx = path.indexOf(';');
        if (semicolonIdx >= 0) {
            path = path.substring(0, semicolonIdx);
        }

        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }
}
