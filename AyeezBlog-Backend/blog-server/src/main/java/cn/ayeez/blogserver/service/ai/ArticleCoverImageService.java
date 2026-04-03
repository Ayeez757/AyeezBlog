package cn.ayeez.blogserver.service.ai;


import cn.ayeez.blogserver.config.ArticleCoverImageProperties;

import cn.ayeez.blogserver.config.QiniuProperties;

import cn.ayeez.blogserver.util.VolcengineVisualSigner;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.qiniu.http.Response;

import com.qiniu.storage.Configuration;

import com.qiniu.storage.UploadManager;

import com.qiniu.util.Auth;

import com.qiniu.util.StringMap;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpEntity;

import org.springframework.http.HttpHeaders;

import org.springframework.http.MediaType;

import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;

import org.springframework.web.client.RestTemplate;


import java.time.LocalDate;

import java.time.format.DateTimeFormatter;

import java.util.LinkedHashMap;

import java.util.Map;

import java.util.TreeMap;

import java.util.UUID;


/**
 * 火山引擎即梦（智能视觉 CVProcess）文生图生成封面，并转存七牛得到持久 URL。
 */

@Service

@Slf4j

@RequiredArgsConstructor

public class ArticleCoverImageService {


    private static final String STYLE_BLOCK = """
            
            【博客文章封面】根据下列技术文章主题生成一张封面图。
            要求：简洁美观；背景为中等饱和度的纯色或极轻微渐变，无复杂纹理与场景；
            画面主体为一个与主题匹配的主流技术或内容图标（采用该技术常见、可辨识的扁平化/矢量图标风格），
            图标居中、尺度大、边缘清晰、主体突出。整体为图形插画风，非摄影写实。
            不要大段文字、标题字、二维码、真实人脸、杂乱装饰。""";

    /**
     * 置于提示词末尾，减轻「长上下文」时模型对首段系统风格遗忘的问题；不参与截断时优先保留简介摘要，必要时可省略本段。
     */
    private static final String STYLE_TAIL =
            "\n\n（请严格遵守首段「博客文章封面」：扁平/矢量主图标、非写实、无真人脸与密集文字。）";

    private static final String NEGATIVE_PROMPT =

            "低分辨率，模糊，畸形，肢体畸形，杂乱背景，过多文字，画面水印，分割条，多个人物，写实照片人脸，过度3D渲染，霓虹杂乱，灰闷无层次，高噪点，蜡像感，AI廉价感";


    private final ArticleCoverImageProperties coverProperties;

    private final QiniuProperties qiniuProperties;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * 生成封面并上传七牛，返回可长期访问的封面 URL；失败抛异常由 Controller 转 Result。
     */

    public String generateAndUploadCover(String title, String description, String coverPrompt) throws Exception {

        log.debug("ArticleCoverImage: 开始生成封面, titleLen={}, descriptionLen={}, coverPromptLen={}",

                title != null ? title.length() : 0,

                description != null ? description.length() : 0,

                coverPrompt != null ? coverPrompt.length() : 0);

        if (!coverProperties.isEnabled()) {

            throw new IllegalStateException("未开启封面 AI（blog.ai.cover.enabled）");

        }

        if (!StringUtils.hasText(coverProperties.getAccessKey())

                || !StringUtils.hasText(coverProperties.getSecretKey())) {

            throw new IllegalStateException(

                    "未配置火山引擎密钥：请在 hm.volcengine.access-key / hm.volcengine.secret-key 填入控制台 API 访问密钥");

        }

        if (!qiniuConfigured()) {

            throw new IllegalStateException("七牛配置不完整，无法保存封面图");

        }


        String prompt = buildPrompt(title, description, coverPrompt);

        log.debug("ArticleCoverImage: 提示词就绪, promptLen={}, reqKey={}, size={}×{}, host={}",

                prompt.length(),

                coverProperties.getReqKey(),

                coverProperties.getWidth(),

                coverProperties.getHeight(),

                coverProperties.getHost());


        String tempImageUrl = callVolcengineJimeng(prompt);

        log.debug("ArticleCoverImage: 即梦返回临时图 URL host={}", hostOnly(tempImageUrl));


        byte[] imageBytes = downloadImage(tempImageUrl);

        log.debug("ArticleCoverImage: 已下载图片, bytes={}", imageBytes.length);


        String publicUrl = uploadToQiniuPng(imageBytes);

        log.debug("ArticleCoverImage: 七牛上传完成, publicUrlHost={}", hostOnly(publicUrl));

        return publicUrl;

    }


    private boolean qiniuConfigured() {

        return StringUtils.hasText(qiniuProperties.getAccessKey())

                && StringUtils.hasText(qiniuProperties.getSecretKey())

                && StringUtils.hasText(qiniuProperties.getBucket())

                && StringUtils.hasText(qiniuProperties.getDomain());

    }


    private String buildPrompt(String title, String description, String coverPrompt) {

        String safeTitle = title != null ? title.trim() : "";

        // 封面生成只需要标题与简介信息；对简介做轻量清洗，尽量避免残留 Markdown 影响模型理解。
        String excerpt = ArticleSummaryAiService.simplifyMarkdownForPrompt(description);

        int maxExcerptConfig = Math.max(200, coverProperties.getMaxContentExcerptChars());

        if (excerpt.length() > maxExcerptConfig) {

            excerpt = excerpt.substring(0, maxExcerptConfig);

        }

        int maxUser = Math.max(0, coverProperties.getMaxUserPromptChars());

        String userExtra = coverPrompt != null ? coverPrompt.trim() : "";

        if (userExtra.length() > maxUser) {

            userExtra = userExtra.substring(0, maxUser);

        }

        String style = STYLE_BLOCK.strip();

        String userBlock = StringUtils.hasText(userExtra) ? "\n\n【作者补充要求】" + userExtra : "";

        int maxTotal =

                Math.max(coverProperties.getMaxTotalPromptChars(), style.length() + 64);

        String contextPrefix =
                "\n\n文章标题：" + safeTitle + "\n简介要点（仅供理解题材，勿在图中还原文字）：";

        while (style.length() + contextPrefix.length() > maxTotal && safeTitle.length() > 0) {

            safeTitle = safeTitle.substring(0, safeTitle.length() - 1);

            contextPrefix =
                    "\n\n文章标题：" + safeTitle + "\n简介要点（仅供理解题材，勿在图中还原文字）：";

        }

        String tail = STYLE_TAIL;

        // 禁止对整串做头部截断：否则会砍掉「博客文章封面」系统说明。只压缩摘要或用户补充，必要时去掉尾句 reinforcement。
        while (true) {

            int excerptLimit = fitExcerptLength(style, userBlock, contextPrefix, excerpt, tail, maxTotal);

            String ex = excerpt.substring(0, excerptLimit);

            String full = style + userBlock + contextPrefix + ex + tail;

            if (full.length() <= maxTotal) {

                return full;

            }

            if (StringUtils.hasText(tail)) {

                tail = "";

                continue;

            }

            if (StringUtils.hasText(userExtra)) {

                userExtra = userExtra.substring(0, userExtra.length() - 1);

                userBlock = StringUtils.hasText(userExtra) ? "\n\n【作者补充要求】" + userExtra : "";

                continue;

            }

            // 仅系统在 + 简介仍超限（maxTotal 过小）：仍保证 style 完整，硬截简介

            int fallback = maxTotal - style.length() - contextPrefix.length();

            int take = Math.max(0, Math.min(excerpt.length(), fallback));

            return style + contextPrefix + excerpt.substring(0, take);

        }

    }

    /**
     * 在固定「系统段 + 用户段 + 前缀 + 尾句」下，取不超过 maxTotal 的最大摘要长度。
     */
    private static int fitExcerptLength(

            String style,

            String userBlock,

            String contextPrefix,

            String excerpt,

            String tail,

            int maxTotal) {

        int low = 0;

        int high = excerpt.length();

        while (low < high) {

            int mid = (low + high + 1) >>> 1;

            String candidate = style + userBlock + contextPrefix + excerpt.substring(0, mid) + tail;

            if (candidate.length() <= maxTotal) {

                low = mid;

            } else {

                high = mid - 1;

            }

        }

        return low;

    }


    private static String hostOnly(String url) {

        if (!StringUtils.hasText(url)) {

            return "";

        }

        try {

            java.net.URI u = java.net.URI.create(url.startsWith("http") ? url : "https://" + url);

            return u.getHost() != null ? u.getHost() : url.substring(0, Math.min(48, url.length()));

        } catch (Exception e) {

            return "(unparsed)";

        }

    }


    private String callVolcengineJimeng(String prompt) throws Exception {

        Map<String, Object> bodyMap = new LinkedHashMap<>();

        bodyMap.put("req_key", coverProperties.getReqKey());

        bodyMap.put("prompt", prompt);

        bodyMap.put("return_url", coverProperties.isReturnUrl());

        bodyMap.put("use_pre_llm", coverProperties.isUsePreLlm());

        bodyMap.put("width", coverProperties.getWidth());

        bodyMap.put("height", coverProperties.getHeight());

        bodyMap.put("seed", coverProperties.getSeed());

        bodyMap.put("negative_prompt", NEGATIVE_PROMPT);


        String json = objectMapper.writeValueAsString(bodyMap);

        if (coverProperties.isVerboseHttpLog()) {

            log.info(

                    "[AI-COVER] 即梦文生图 请求参数（完整 JSON，已脱敏密钥）\n{}",

                    prettyJsonForLog(json));

        }


        Map<String, String> query = new TreeMap<>();

        query.put("Action", coverProperties.getAction());

        query.put("Version", coverProperties.getVersion());


        VolcengineVisualSigner.SignedRequest signed = VolcengineVisualSigner.sign(

                "POST",

                coverProperties.getHost().trim(),

                "/",

                query,

                json,

                coverProperties.getAccessKey().trim(),

                coverProperties.getSecretKey().trim(),

                coverProperties.getRegion(),

                coverProperties.getService()

        );


        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("X-Date", signed.xDate());

        headers.set("Authorization", signed.authorization());

        headers.set("X-Content-Sha256", signed.contentSha256());


        HttpEntity<String> entity = new HttpEntity<>(signed.body(), headers);

        if (coverProperties.isVerboseHttpLog()) {

            log.info("[AI-COVER] 即梦 HTTP 请求 URL（含 Query，无密钥）: {}", signed.url());

            log.info(

                    "[AI-COVER] 即梦 HTTP 请求头: Content-Type={}, X-Date={}, X-Content-Sha256={}, Authorization=<HMAC 已省略>",

                    MediaType.APPLICATION_JSON,

                    signed.xDate(),

                    signed.contentSha256());

        } else {

            log.debug("ArticleCoverImage: POST 即梦文生图, urlHost={}, bodyLen={}",

                    coverProperties.getHost(), json.length());

        }


        ResponseEntity<String> response = restTemplate.postForEntity(

                signed.url(),

                entity,

                String.class

        );


        String respBody = response.getBody();

        if (coverProperties.isVerboseHttpLog()) {

            log.info(

                    "[AI-COVER] 即梦 HTTP 响应 status={}, 响应体（完整）:\n{}",

                    response.getStatusCode().value(),

                    respBody != null ? respBody : "(null)");

        } else {

            log.debug("ArticleCoverImage: 火山 HTTP status={}, respBodyLen={}",

                    response.getStatusCode().value(),

                    respBody != null ? respBody.length() : 0);

        }

        if (!StringUtils.hasText(respBody)) {

            throw new IllegalStateException("火山即梦返回空响应");

        }


        JsonNode root = objectMapper.readTree(respBody);

        assertVolcengineOk(root);


        String url = firstImageUrl(root);

        if (StringUtils.hasText(url)) {

            return url;

        }

        throw new IllegalStateException("火山即梦响应中未找到图片 URL: " + respBody.substring(0, Math.min(500, respBody.length())));

    }


    private static void assertVolcengineOk(JsonNode root) {

        JsonNode apiErr = root.path("ResponseMetadata").path("Error");

        if (!apiErr.isMissingNode() && !apiErr.isNull() && apiErr.has("Code")) {

            String c = apiErr.path("Code").asText("");

            String m = apiErr.path("Message").asText("");

            throw new IllegalStateException("火山 API 错误 " + c + ": " + m);

        }

        JsonNode codeNode = root.get("code");

        if (codeNode == null || codeNode.isNull()) {

            return;

        }

        int code;

        if (codeNode.isIntegralNumber()) {

            code = codeNode.intValue();

        } else {

            try {

                code = Integer.parseInt(codeNode.asText().trim());

            } catch (NumberFormatException e) {

                return;

            }

        }

        if (code != 10000 && code != 0) {

            String msg = root.path("message").asText("");

            throw new IllegalStateException("火山即梦业务错误 code=" + code + ": " + msg);

        }

    }


    private static String firstImageUrl(JsonNode root) {

        JsonNode urls = root.path("data").path("image_urls");

        if (urls.isArray()) {

            for (JsonNode u : urls) {

                if (u != null && u.isTextual()) {

                    String s = u.asText("");

                    if (StringUtils.hasText(s)) {

                        return s;

                    }

                }

            }

        }

        JsonNode single = root.path("data").path("image_url");

        if (single.isTextual()) {

            String s = single.asText("");

            if (StringUtils.hasText(s)) {

                return s;

            }

        }

        return null;

    }


    private String prettyJsonForLog(String json) {

        try {

            return objectMapper.writerWithDefaultPrettyPrinter()

                    .writeValueAsString(objectMapper.readTree(json));

        } catch (Exception e) {

            return json;

        }

    }


    private byte[] downloadImage(String url) {

        byte[] data = restTemplate.getForObject(url, byte[].class);

        if (data == null || data.length == 0) {

            throw new IllegalStateException("下载生成图失败");

        }

        return data;

    }


    private String uploadToQiniuPng(byte[] data) throws Exception {

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        String uuid = UUID.randomUUID().toString().replace("-", "");

        String key = "covers/" + datePath + "/ai-" + uuid + ".png";


        Auth auth = Auth.create(qiniuProperties.getAccessKey(), qiniuProperties.getSecretKey());

        long expires = qiniuProperties.getTokenExpires() != null ? qiniuProperties.getTokenExpires() : 3600;

        String token = auth.uploadToken(qiniuProperties.getBucket(), key, expires, new StringMap());


        log.debug("ArticleCoverImage: 上传七牛, key={}, dataBytes={}", key, data.length);

        UploadManager uploadManager = new UploadManager(new Configuration());

        Response qnResp = uploadManager.put(data, key, token);

        if (!qnResp.isOK()) {

            log.debug("ArticleCoverImage: 七牛失败 statusCode={}, body={}", qnResp.statusCode, qnResp.bodyString());

            throw new IllegalStateException("七牛上传失败: " + qnResp.bodyString());

        }


        String domain = normalizeDomain(qiniuProperties.getDomain());

        return domain + "/" + key;

    }


    private String normalizeDomain(String domain) {

        String d = domain.trim();

        while (d.endsWith("/")) {

            d = d.substring(0, d.length() - 1);

        }

        if (d.startsWith("http://") || d.startsWith("https://")) {

            return d;

        }

        return "https://" + d;

    }

}

