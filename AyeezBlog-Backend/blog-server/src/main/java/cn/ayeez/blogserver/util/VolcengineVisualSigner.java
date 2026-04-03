package cn.ayeez.blogserver.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 火山引擎 OpenAPI 签名（与官方 HTTP 示例一致：HMAC-SHA256，service=cv，智能视觉 visual.volcengineapi.com）。
 */
public final class VolcengineVisualSigner {

    private static final DateTimeFormatter AMZ_DATE_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter DATE_STAMP_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);

    private VolcengineVisualSigner() {
    }

    public static SignedRequest sign(
            String method,
            String host,
            String canonicalUri,
            Map<String, String> queryParams,
            String bodyUtf8,
            String accessKey,
            String secretKey,
            String region,
            String service
    ) throws Exception {
        String sortedQuery = queryParams.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        String payloadHash = sha256Hex(bodyUtf8);

        Instant now = Instant.now();
        String xDate = AMZ_DATE_FMT.format(now);
        String dateStamp = DATE_STAMP_FMT.format(now);

        String contentType = "application/json";
        String canonicalHeaders =
                "content-type:" + contentType + "\n"
                        + "host:" + host + "\n"
                        + "x-content-sha256:" + payloadHash + "\n"
                        + "x-date:" + xDate + "\n";

        String signedHeaders = "content-type;host;x-content-sha256;x-date";

        String canonicalRequest = method + "\n"
                + canonicalUri + "\n"
                + sortedQuery + "\n"
                + canonicalHeaders + "\n"
                + signedHeaders + "\n"
                + payloadHash;

        String algorithm = "HMAC-SHA256";
        String credentialScope = dateStamp + "/" + region + "/" + service + "/request";
        String stringToSign = algorithm + "\n"
                + xDate + "\n"
                + credentialScope + "\n"
                + sha256Hex(canonicalRequest);

        byte[] signingKey = getSignatureKey(secretKey, dateStamp, region, service);
        String signature = hmacHex(signingKey, stringToSign);

        String authorization = algorithm + " Credential=" + accessKey + "/" + credentialScope + ", "
                + "SignedHeaders=" + signedHeaders + ", "
                + "Signature=" + signature;

        return new SignedRequest(
                "https://" + host + canonicalUri + "?" + sortedQuery,
                xDate,
                authorization,
                payloadHash,
                contentType,
                bodyUtf8
        );
    }

    private static byte[] getSignatureKey(String secretKey, String dateStamp, String region, String service)
            throws Exception {
        byte[] kSecret = secretKey.getBytes(StandardCharsets.UTF_8);
        byte[] kDate = hmac(kSecret, dateStamp);
        byte[] kRegion = hmac(kDate, region);
        byte[] kService = hmac(kRegion, service);
        return hmac(kService, "request");
    }

    private static byte[] hmac(byte[] key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private static String hmacHex(byte[] key, String data) throws Exception {
        byte[] raw = hmac(key, data);
        return toHex(raw);
    }

    private static String sha256Hex(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
        return toHex(d);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public record SignedRequest(
            String url,
            String xDate,
            String authorization,
            String contentSha256,
            String contentType,
            String body
    ) {
    }
}
