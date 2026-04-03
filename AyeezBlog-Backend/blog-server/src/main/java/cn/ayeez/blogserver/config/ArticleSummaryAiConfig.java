package cn.ayeez.blogserver.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ArticleSummaryAiProperties.class, ArticleCoverImageProperties.class})
public class ArticleSummaryAiConfig {
}
