package cn.ayeez.blogserver.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtSecretBootstrap {

    @Value("${hm.jwt.secret-key:}")
    private String jwtSecret;

    @PostConstruct
    public void init() {
        String envSecret = System.getenv("HM_JWT_SECRET_KEY");
        if (envSecret == null || envSecret.isBlank()) {
            if (jwtSecret != null && !jwtSecret.isBlank()) {
                System.setProperty("hm.jwt.secret-key", jwtSecret);
            }
        }
    }
}
