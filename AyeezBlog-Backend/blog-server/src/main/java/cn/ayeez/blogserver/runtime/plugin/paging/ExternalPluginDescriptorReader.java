package cn.ayeez.blogserver.runtime.plugin.paging;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.jar.JarFile;

/**
 * ExternalPluginDescriptorReader 从插件 jar 内读取 plugin.properties 元信息。
 */
@Component
public class ExternalPluginDescriptorReader {

    private static final String DESCRIPTOR_PATH = "plugin.properties";

    public ExternalPluginDescriptor read(String jarPath) {
        if (jarPath == null || jarPath.trim().isEmpty()) {
            throw new IllegalArgumentException("插件 jar 路径不能为空。");
        }
        Path path = Paths.get(jarPath.trim()).toAbsolutePath().normalize();
        if (!Files.exists(path)) {
            throw new IllegalStateException("未找到外部插件 jar 文件：" + path);
        }

        try (JarFile jarFile = new JarFile(path.toFile())) {
            var entry = jarFile.getJarEntry(DESCRIPTOR_PATH);
            if (entry == null) {
                throw new IllegalStateException("外部插件 jar 缺少 " + DESCRIPTOR_PATH + " 元信息文件。");
            }
            Properties properties = new Properties();
            try (InputStream inputStream = jarFile.getInputStream(entry)) {
                properties.load(inputStream);
            }
            String pluginId = trimToNull(properties.getProperty("plugin.id"));
            String className = trimToNull(properties.getProperty("plugin.class"));
            String version = trimToNull(properties.getProperty("plugin.version"));
            if (pluginId == null) {
                throw new IllegalStateException("外部插件元信息缺少 plugin.id。");
            }
            if (className == null) {
                throw new IllegalStateException("外部插件元信息缺少 plugin.class。");
            }
            return new ExternalPluginDescriptor(pluginId, className, version);
        } catch (Exception ex) {
            throw new IllegalStateException("读取外部插件元信息失败：" + ex.getMessage(), ex);
        }
    }

    private String trimToNull(String raw) {
        if (raw == null) {
            return null;
        }
        String v = raw.trim();
        return v.isEmpty() ? null : v;
    }
}

