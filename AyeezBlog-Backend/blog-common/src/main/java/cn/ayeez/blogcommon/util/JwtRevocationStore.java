package cn.ayeez.blogcommon.util;

import io.jsonwebtoken.Claims;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT 吊销存储（进程内）。
 * key 使用 jti，value 使用过期时间毫秒时间戳。
 */
public final class JwtRevocationStore {

    private static final Map<String, Long> REVOKED_JTI_EXPIRES_AT = new ConcurrentHashMap<>();

    private JwtRevocationStore() {
    }

    /**
     * 吊销 token：记录其 jti，直到 token 自身过期。
     */
    public static void revokeToken(String token) {
        Claims claims = JwtUtil.parseToken(token);
        String jti = claims.getId();
        Date expiration = claims.getExpiration();
        if (jti == null || jti.isBlank() || expiration == null) {
            return;
        }
        REVOKED_JTI_EXPIRES_AT.put(jti, expiration.getTime());
        cleanupExpired();
    }

    /**
     * 判断 token 是否已被吊销。
     */
    public static boolean isRevoked(String token) {
        Claims claims = JwtUtil.parseToken(token);
        String jti = claims.getId();
        if (jti == null || jti.isBlank()) {
            return false;
        }
        Long expiresAt = REVOKED_JTI_EXPIRES_AT.get(jti);
        if (expiresAt == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (expiresAt <= now) {
            REVOKED_JTI_EXPIRES_AT.remove(jti);
            return false;
        }
        return true;
    }

    private static void cleanupExpired() {
        long now = System.currentTimeMillis();
        REVOKED_JTI_EXPIRES_AT.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue() <= now);
    }
}
