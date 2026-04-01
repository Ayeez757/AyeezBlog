package cn.ayeez.blogserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "qiniu")
public class QiniuProperties {
    /**
     * 七牛 AccessKey
     */
    private String accessKey;

    /**
     * 七牛 SecretKey
     */
    private String secretKey;

    /**
     * 存储空间 Bucket
     */
    private String bucket;

    /**
     * 外链域名（最终拼接成公开访问 URL）
     */
    private String domain;

    /**
     * 上传入口（前端直传使用）
     */
    private String uploadUrl;

    /**
     * 上传 token 有效期（秒）
     */
    private Integer tokenExpires = 1800;
}

