package cn.ayeez.blogcommon.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * JWT 工具类
 */
public class JwtUtil {


    // 建议使用至少256位（32字节）的密钥。优先读取环境变量 HM_JWT_SECRET_KEY，
    // 其次读取 JVM 参数 -Dhm.jwt.secret-key=...
    private static final String ENV_SECRET_KEY = "HM_JWT_SECRET_KEY";
    private static final String PROPERTY_SECRET_KEY = "hm.jwt.secret-key";
    private static final SecretKey SECRET_KEY = loadSecretKey();

    // 默认过期时间：24小时（单位：毫秒）
    private static final long EXPIRATION = 60 * 60 * 1000L; // 1小时

    private static SecretKey loadSecretKey() {
        String secret = System.getenv(ENV_SECRET_KEY);
        if (secret == null || secret.isBlank()) {
            secret = System.getProperty(PROPERTY_SECRET_KEY);
        }
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT secret is missing. Please set environment variable " + ENV_SECRET_KEY
                            + " or JVM property -D" + PROPERTY_SECRET_KEY);
        }
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        // 检查密钥长度
        if (secretBytes.length < 32) {
            throw new IllegalStateException("JWT secret is too short. Use at least 32 bytes.");
        }
        return Keys.hmacShaKeyFor(secretBytes);
    }

    /**
     * 生成 JWT Token
     *
     * @param claims 要放入 token 的自定义载荷（如用户ID、用户名等）
     * @return JWT 字符串
     */
    public static String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 JWT Token，获取 Claims
     *
     * @param token JWT 字符串
     * @return Claims 对象，包含载荷数据
     * @throws io.jsonwebtoken.ExpiredJwtException     token 过期
     * @throws io.jsonwebtoken.UnsupportedJwtException 不支持的 token 格式
     * @throws io.jsonwebtoken.MalformedJwtException   token 结构损坏
     * @throws io.jsonwebtoken.SignatureException      签名验证失败
     * @throws IllegalArgumentException                token 为空或无效
     */
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 解析 JWT 并返回完整 Jws 对象（含 Header + Claims）。
     */
    public static Jws<Claims> parseTokenJws(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token);
    }
}

