package cn.ayeez.blogserver.service.ai;

import cn.ayeez.blogpojo.dto.request.PostBody;
import cn.ayeez.blogserver.config.ArticleSummaryAiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 使用 Spring AI（OpenAI 兼容协议）调用 DeepSeek，根据标题与正文生成中文简介。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleSummaryAiService {

    private static final String SYSTEM = """
            你是技术博客的编辑助手。根据用户提供的文章标题与正文，用中文写一段用于文章列表或卡片展示的简短描述。
            要求：纯叙述性文字；不要标题、引号、Markdown、列表符号；不要「本文介绍了」等套话；语气自然。""";

    private final ChatClient.Builder chatClientBuilder;
    private final ArticleSummaryAiProperties properties;
    private final Environment environment;

    private final Object chatClientLock = new Object();
    private volatile ChatClient chatClient;

    /**
     * 若 {@link ArticleSummaryAiProperties#isEnabled()} 且当前描述为空，则尝试生成并写入 {@link PostBody#setDescription(String)}。
     */
    public void fillDescriptionIfBlank(PostBody postBody) {
        if (!properties.isEnabled()) {
            log.debug("ArticleSummaryAi: 跳过自动生成简介，summary.enabled=false");
            return;
        }
        if (postBody == null) {
            log.debug("ArticleSummaryAi: 跳过自动生成简介，postBody=null");
            return;
        }
        if (StringUtils.hasText(postBody.getDescription())) {
            log.debug("ArticleSummaryAi: 跳过自动生成简介，已有 description");
            return;
        }
        String content = postBody.getContent();
        if (!StringUtils.hasText(content)) {
            log.debug("ArticleSummaryAi: 跳过自动生成简介，正文为空");
            return;
        }
        log.debug("ArticleSummaryAi: 保存文章时自动生成简介开始, contentLen={}", content.length());
        try {
            String summary = generate(postBody.getTitle(), content);
            if (StringUtils.hasText(summary)) {
                postBody.setDescription(clampDescription(summary));
                log.debug("ArticleSummaryAi: 自动生成简介完成, resultLen={}", postBody.getDescription().length());
            } else {
                log.debug("ArticleSummaryAi: 自动生成简介得到空结果");
            }
        } catch (Exception e) {
            log.warn("保存文章时自动生成简介失败: {}", e.getMessage());
        }
    }

    /**
     * 根据标题与 Markdown 正文生成简介（管理端可单独调用）。
     */
    public String generate(String title, String markdownContent) {
        if (!StringUtils.hasText(markdownContent)) {
            log.debug("ArticleSummaryAi.generate: markdownContent 为空");
            return null;
        }
        String excerpt = simplifyMarkdownForPrompt(markdownContent);
        int maxChars = Math.max(500, properties.getMaxContentChars());
        if (excerpt.length() > maxChars) {
            excerpt = excerpt.substring(0, maxChars);
        }
        String safeTitle = title != null ? title.trim() : "";
        int maxDesc = Math.min(255, Math.max(50, properties.getMaxDescriptionLength()));

        String userPrompt = """
                标题：%s

                正文（已做简单清理，可能仍含少量 Markdown 标记）：
                %s

                请输出一段简介：中文，单行或一段即可，总长度不超过 %d 个字符，不要任何前缀说明。""".formatted(safeTitle, excerpt, maxDesc);

        log.debug(
                "ArticleSummaryAi.generate: 调用 DeepSeek, titleLen={}, excerptLen={}, userPromptLen={}, maxDesc={}",
                safeTitle.length(), excerpt.length(), userPrompt.length(), maxDesc);

        if (properties.isVerboseLog()) {
            log.info(
                    "[AI-SUMMARY] Spring AI 配置 baseUrl={}, model={}, temperature={}",
                    environment.getProperty("spring.ai.openai.base-url", ""),
                    environment.getProperty("spring.ai.openai.chat.options.model", ""),
                    environment.getProperty("spring.ai.openai.chat.options.temperature", ""));
            log.info("[AI-SUMMARY] 系统提示词（完整）:\n{}", SYSTEM.strip());
            log.info("[AI-SUMMARY] 用户提示词（完整）:\n{}", userPrompt);
        }

        String raw = chatClient().prompt()
                .user(userPrompt)
                .call()
                .content();

        if (properties.isVerboseLog()) {
            log.info("[AI-SUMMARY] 模型原始输出（完整，未截断）:\n{}", raw != null ? raw : "(null)");
        }

        if (!StringUtils.hasText(raw)) {
            log.debug("ArticleSummaryAi.generate: 模型返回空 content");
            return null;
        }
        String out = clampDescription(raw.trim());
        log.debug("ArticleSummaryAi.generate: 完成, rawLen={}, clampedLen={}", raw.length(), out.length());
        return out;
    }

    private String clampDescription(String text) {
        int max = Math.min(255, Math.max(1, properties.getMaxDescriptionLength()));
        String t = text.replace('\n', ' ').replace('\r', ' ').trim();
        if (t.length() <= max) {
            return t;
        }
        return t.substring(0, max);
    }

    private ChatClient chatClient() {
        ChatClient existing = chatClient;
        if (existing != null) {
            return existing;
        }
        synchronized (chatClientLock) {
            if (chatClient == null) {
                chatClient = chatClientBuilder.defaultSystem(SYSTEM).build();
            }
            return chatClient;
        }
    }

    public static String simplifyMarkdownForPrompt(String md) {
        if (md == null) {
            return "";
        }
        String s = md.replace("\r\n", "\n");
        s = s.replaceAll("(?s)```.*?```", " ");
        s = s.replaceAll("`+[^`\\n]*`+", " ");
        s = s.replaceAll("!\\[[^\\]]*]\\([^)]*\\)", " ");
        s = s.replaceAll("\\[[^\\]]*]\\([^)]*\\)", " ");
        s = s.replaceAll("^#+\\s*", "");
        s = s.replaceAll("(?m)^#+\\s*", " ");
        s = s.replaceAll("\\s+", " ");
        return s.trim();
    }
}
