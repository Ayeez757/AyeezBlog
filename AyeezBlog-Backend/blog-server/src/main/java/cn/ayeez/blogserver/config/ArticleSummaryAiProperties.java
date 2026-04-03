package cn.ayeez.blogserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文章简介 AI 生成开关与长度限制（DeepSeek / OpenAI 兼容接口由 spring.ai.openai 配置）。
 */
@Data
@ConfigurationProperties(prefix = "blog.ai.summary")
public class ArticleSummaryAiProperties {

    /**
     * 为 true 时：保存文章若描述为空则调用模型生成；管理端「生成简介」接口可用。
     */
    private boolean enabled = false;

    /**
     * 送入模型的正文最大字符数（超出截断，控制 token）。
     */
    private int maxContentChars = 12000;

    /**
     * 生成简介写入库前的最大长度（需小于等于库表 description 列长度）。
     */
    private int maxDescriptionLength = 240;

    /**
     * 为 true 时在 INFO 输出 DeepSeek 调用：系统提示词、用户提示词、模型原始输出（便于排查）。
     */
    private boolean verboseLog = false;
}
