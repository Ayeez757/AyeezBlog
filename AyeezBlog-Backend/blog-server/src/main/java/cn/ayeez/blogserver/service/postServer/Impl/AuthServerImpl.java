package cn.ayeez.blogserver.service.postServer.Impl;

import cn.ayeez.blogcommon.util.JwtRevocationStore;
import cn.ayeez.blogpojo.bo.Auth;
import cn.ayeez.blogpojo.dto.response.LoginInfo;
import cn.ayeez.blogserver.mapper.AuthMapper;
import cn.ayeez.blogserver.service.postServer.AuthService;
import cn.ayeez.blogcommon.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AuthServerImpl implements AuthService {

    @Autowired
    private AuthMapper authMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static boolean looksLikeBcrypt(String stored) {
        return stored != null
                && stored.length() == 60
                && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"));
    }

    /**
     * 执行管理员登录校验，并在成功后签发 JWT。
     *
     * @param auth 登录参数
     * @return 登录信息（含 token）；认证失败返回 null
     */
    @Override
    public LoginInfo login(Auth auth) {
        if (auth.getUsername() == null || auth.getPassword() == null) {
            return null;
        }
        String rawPassword = auth.getPassword();
        cn.ayeez.blogpojo.po.Auth user = authMapper.findByUsername(auth.getUsername().trim());
        if (user == null) {
            return null;
        }
        String stored = user.getPassword();
        boolean passwordOk;
        if (looksLikeBcrypt(stored)) {
            passwordOk = passwordEncoder.matches(rawPassword, stored);
        } else {
            passwordOk = rawPassword.equals(stored);
            if (passwordOk && user.getId() != null) {
                String hashed = passwordEncoder.encode(rawPassword);
                authMapper.updatePassword(user.getId().longValue(), hashed);
                log.info("用户 {} 的密码已由明文升级为 BCrypt", user.getUsername());
            }
        }
        if (!passwordOk) {
            return null;
        }
        log.info("登录成功，用户：{}", user.getUsername());
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        String jwt = JwtUtil.generateToken(claims);
        return new LoginInfo(user.getId(), user.getUsername(), user.getNickname(), jwt);
    }

    @Override
    public boolean logout(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        JwtRevocationStore.revokeToken(token);
        return true;
    }
}
