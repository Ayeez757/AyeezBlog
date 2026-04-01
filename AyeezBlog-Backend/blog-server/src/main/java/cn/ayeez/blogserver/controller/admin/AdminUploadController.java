package cn.ayeez.blogserver.controller.admin;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogserver.config.QiniuProperties;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/admin/upload")
public class AdminUploadController {

    @Autowired
    private QiniuProperties qiniuProperties;

    /**
     * 管理端直传七牛：下发 uploadToken + 建议 key + 域名/上传入口
     *
     * GET /admin/upload/qiniu/token?filename=xx.jpg&dir=covers
     */
    @GetMapping("/qiniu/token")
    public Result<Map<String, Object>> qiniuUploadToken(String filename, String dir) {
        if (!StringUtils.hasText(filename)) {
            return Result.error(400, "filename 不能为空");
        }
        if (!StringUtils.hasText(qiniuProperties.getAccessKey())
                || !StringUtils.hasText(qiniuProperties.getSecretKey())
                || !StringUtils.hasText(qiniuProperties.getBucket())
                || !StringUtils.hasText(qiniuProperties.getDomain())
                || !StringUtils.hasText(qiniuProperties.getUploadUrl())) {
            return Result.error(500, "七牛配置未完成，请检查 qiniu.* 配置项");
        }

        String safeDir = StringUtils.hasText(dir) ? dir.trim() : "covers";
        safeDir = safeDir.replace("\\", "/");
        while (safeDir.startsWith("/")) safeDir = safeDir.substring(1);
        while (safeDir.endsWith("/")) safeDir = safeDir.substring(0, safeDir.length() - 1);
        if (!StringUtils.hasText(safeDir)) safeDir = "covers";

        String ext = "";
        int dot = filename.lastIndexOf('.');
        if (dot >= 0 && dot < filename.length() - 1) {
            ext = filename.substring(dot).toLowerCase(Locale.ROOT);
            if (ext.length() > 10) ext = ""; // 简单限制，避免异常扩展名
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String key = safeDir + "/" + datePath + "/" + uuid + ext;

        Auth auth = Auth.create(qiniuProperties.getAccessKey(), qiniuProperties.getSecretKey());
        long expires = qiniuProperties.getTokenExpires() != null ? qiniuProperties.getTokenExpires() : 1800;
        String uploadToken = auth.uploadToken(qiniuProperties.getBucket(), key, expires, new StringMap());

        Map<String, Object> data = new HashMap<>();
        data.put("uploadToken", uploadToken);
        data.put("key", key);
        data.put("bucket", qiniuProperties.getBucket());
        data.put("domain", normalizeDomain(qiniuProperties.getDomain()));
        data.put("uploadUrl", qiniuProperties.getUploadUrl());
        data.put("expires", expires);
        return Result.success(data);
    }

    private String normalizeDomain(String domain) {
        if (!StringUtils.hasText(domain)) return "";
        String d = domain.trim();
        while (d.endsWith("/")) d = d.substring(0, d.length() - 1);
        return d;
    }
}

