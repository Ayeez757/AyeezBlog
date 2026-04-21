package cn.ayeez.blogserver.controller.admin;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogcommon.util.LoginAttemptGuard;
import cn.ayeez.blogpojo.bo.Auth;
import cn.ayeez.blogpojo.dto.response.LoginInfo;
import cn.ayeez.blogserver.service.postServer.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台认证相关接口。
 */
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminAuthController {

    @Autowired
    private AuthService authServer;

    /**
     * 管理员登录接口。
     *
     * @param auth 登录参数（账号、密码）
     * @return 登录成功返回用户信息与 token，失败返回错误信息
     */
    @RequestMapping("/login")
    public Result login(@RequestBody Auth auth, HttpServletRequest request) {
        String username = auth == null ? null : auth.getUsername();
        if (username == null || username.isBlank()) {
            return Result.error(400, "账号密码错误");
        }
        String loginKey = buildLoginGuardKey(username, resolveClientIp(request));
        LoginAttemptGuard.LockStatus lockStatus = LoginAttemptGuard.getLockStatus(loginKey);
        if (lockStatus.locked()) {
            return Result.error(429, "登录失败次数过多，请 " + lockStatus.lockRemainingSeconds() + " 秒后再试");
        }
        log.info("用户登录，账号：{}", username);
        LoginInfo loginInfo = authServer.login(auth);
        if(loginInfo!=null){
            LoginAttemptGuard.recordSuccess(loginKey);
            return Result.success(loginInfo);
        }else{
            LoginAttemptGuard.FailureResult failureResult = LoginAttemptGuard.recordFailure(loginKey);
            if (failureResult.lockedNow()) {
                return Result.error(429, "登录失败次数过多，账号已临时锁定 " + failureResult.lockSeconds() + " 秒");
            }
            return Result.error(403, "账号密码错误，还可尝试 " + failureResult.remainingAttempts() + " 次");
        }
    }

    /**
     * 管理员登出接口：吊销当前 token。
     */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || token.isBlank()) {
            return Result.error(401, "未登录");
        }
        boolean revoked = authServer.logout(token);
        if (!revoked) {
            return Result.error(400, "登出失败");
        }
        return Result.success("登出成功");
    }

    private static String buildLoginGuardKey(String username, String clientIp) {
        return username.trim().toLowerCase() + "|" + clientIp;
    }

    private static String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String[] ips = xff.split(",");
            if (ips.length > 0 && !ips[0].isBlank()) {
                return ips[0].trim();
            }
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr == null || remoteAddr.isBlank() ? "unknown" : remoteAddr;
    }




}
