package cn.ayeez.blogserver.controller.admin;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogserver.config.ArticleCoverImageProperties;
import cn.ayeez.blogserver.config.ArticleSummaryAiProperties;
import cn.ayeez.blogserver.service.ai.ArticleCoverImageService;
import cn.ayeez.blogserver.service.ai.ArticleSummaryAiService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端 AI 能力（文章简介等）。
 */
@Slf4j
@RestController
@RequestMapping("/admin/ai")
@RequiredArgsConstructor
public class AdminAiController {

    private final ArticleSummaryAiService articleSummaryAiService;
    private final ArticleSummaryAiProperties articleSummaryAiProperties;
    private final ArticleCoverImageService articleCoverImageService;
    private final ArticleCoverImageProperties articleCoverImageProperties;

    /**
     * 根据标题与正文生成文章简介（不保存数据库）。
     */
    @PostMapping("/article-description")
    public Result<Map<String, String>> generateArticleDescription(@RequestBody ArticleDescriptionRequest body) {
        if (!articleSummaryAiProperties.isEnabled()) {
            return Result.error(400, "未开启 AI 简介生成（请配置 blog.ai.summary.enabled 与 DeepSeek API 密钥）");
        }
        if (body == null || !StringUtils.hasText(body.getContent())) {
            return Result.error(400, "正文 content 不能为空");
        }
        log.debug(
                "POST /admin/ai/article-description titleLen={}, contentLen={}",
                body.getTitle() != null ? body.getTitle().length() : 0,
                body.getContent().length());
        try {
            String description = articleSummaryAiService.generate(body.getTitle(), body.getContent());
            if (!StringUtils.hasText(description)) {
                return Result.error(500, "模型未返回有效简介");
            }
            return Result.success(Map.of("description", description));
        } catch (Exception e) {
            log.warn("生成文章简介失败", e);
            return Result.error(500, "生成失败：" + e.getMessage());
        }
    }

    /**
     * 火山引擎即梦（智能视觉）文生图生成封面并转存七牛，返回持久封面 URL。
     */
    @PostMapping("/article-cover")
    public Result<Map<String, String>> generateArticleCover(@RequestBody ArticleCoverRequest body) {
        if (!articleCoverImageProperties.isEnabled()) {
            return Result.error(400,
                    "未开启封面 AI：请设置 hm.volcengine.cover-enabled=true（或 blog.ai.cover.enabled）");
        }
        if (!StringUtils.hasText(articleCoverImageProperties.getAccessKey())
                || !StringUtils.hasText(articleCoverImageProperties.getSecretKey())) {
            return Result.error(400,
                    "未配置火山引擎密钥：请在 hm.volcengine.access-key / hm.volcengine.secret-key 填入控制台 API 访问密钥");
        }
        if (body == null || !StringUtils.hasText(body.getDescription())) {
            return Result.error(400, "简介 description 不能为空");
        }
        log.debug(
                "POST /admin/ai/article-cover titleLen={}, descriptionLen={}, coverPromptLen={}",
                body.getTitle() != null ? body.getTitle().length() : 0,
                body.getDescription().length(),
                body.getCoverPrompt() != null ? body.getCoverPrompt().length() : 0);
        try {
            String coverUrl = articleCoverImageService.generateAndUploadCover(
                    body.getTitle(), body.getDescription(), body.getCoverPrompt());
            if (!StringUtils.hasText(coverUrl)) {
                return Result.error(500, "未获得有效封面地址");
            }
            return Result.success(Map.of("coverUrl", coverUrl));
        } catch (Exception e) {
            log.warn("生成文章封面失败", e);
            return Result.error(500, "生成封面失败：" + e.getMessage());
        }
    }

    @Data
    public static class ArticleDescriptionRequest {
        private String title;
        private String content;
    }

    @Data
    public static class ArticleCoverRequest {
        private String title;
        private String description;
        /** 可选：用户对封面构图、配色、主体等的额外要求，拼入正向提示词 */
        private String coverPrompt;
    }
}
