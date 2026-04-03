package cn.ayeez.blogserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 火山引擎即梦（智能视觉 CVProcess）文生图封面配置。
 */
@Data
@ConfigurationProperties(prefix = "blog.ai.cover")
public class ArticleCoverImageProperties {

    private boolean enabled = false;

    /**
     * 火山引擎访问密钥（控制台「访问控制」→ API 访问密钥），与 Secret 成对使用；非百炼 sk-。
     */
    private String accessKey = "";

    private String secretKey = "";

    /** 智能视觉域名，仅 host 用于签名；完整请求为 https://{host}/ */
    private String host = "visual.volcengineapi.com";

    private String action = "CVProcess";

    private String version = "2022-08-31";

    /** 区域，签名用，常见 cn-north-1 */
    private String region = "cn-north-1";

    /** 签名服务名，智能视觉固定 cv */
    private String service = "cv";

    /**
     * 即梦文生图能力标识，以控制台/文档为准（示例：jimeng_high_aes_general_v21_L）。
     */
    private String reqKey = "jimeng_high_aes_general_v21_L";

    /** 输出宽、高（像素），需与所选 req_key 能力范围一致 */
    private int width = 1664;

    private int height = 928;

    /**
     * -1 表示随机种子；亦可设为固定正整数。
     */
    private int seed = -1;

    /** 为 true 时响应内返回可下载的临时 URL（再转存七牛） */
    private boolean returnUrl = true;

    /** 从简介中提取供模型理解主题的摘要长度上限 */
    private int maxContentExcerptChars = 2000;

    /** 正向提示词总长度上限（字符，含风格说明、用户补充、标题与正文摘要） */
    private int maxTotalPromptChars = 1200;

    /** 管理端「生图补充说明」单段长度上限 */
    private int maxUserPromptChars = 300;

    /**
     * 是否走前置 LLM 扩写/改写 prompt。为 true 时常与「固定系统风格」冲突，封面建议保持 false。
     */
    private boolean usePreLlm = false;

    /**
     * 为 true 时在 INFO 输出即梦 HTTP 完整请求 URL、请求体 JSON、响应状态与完整响应体（不含密钥）。
     */
    private boolean verboseHttpLog = false;
}
