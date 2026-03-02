package cn.ayeez.blogcommon.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 */
public class JwtUtil {


    // 建议使用至少256位（32字节）的密钥
    private static final String SECRET_KEY_STRING = "6Zi/5Y+25piv5Liq6LaF57qn5aSn5biF6YC8"; // 至少32字符
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());

    // 默认过期时间：24小时（单位：毫秒）
    private static final long EXPIRATION = 60 * 60 * 1000L; // 1小时

    /**
     * 生成 JWT Token
     *
     * @param claims 要放入 token 的自定义载荷（如用户ID、用户名等）
     * @return JWT 字符串
     */
    public static String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
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
}

