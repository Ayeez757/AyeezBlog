package cn.ayeez.blogserver;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 校验建表脚本中默认管理员密码哈希与明文 {@code admin} 一致。
 */
class AdminDefaultPasswordHashTest {

    /** 与仓库根目录 {@code AyeezBlog建表.sql} 中默认管理员插入语句保持一致 */
    static final String DEFAULT_ADMIN_BCRYPT =
            "$2a$10$eBH3YL6wptUNHJidoKGlzeVue1QUxgDEpHOfr0zefa6eM1IxuNfga";

    @Test
    void defaultAdminSeedMatchesPlainPassword() {
        assertTrue(new BCryptPasswordEncoder().matches("admin", DEFAULT_ADMIN_BCRYPT));
    }
}
