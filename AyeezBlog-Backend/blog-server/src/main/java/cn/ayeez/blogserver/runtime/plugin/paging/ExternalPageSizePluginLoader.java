package cn.ayeez.blogserver.runtime.plugin.paging;

import cn.ayeez.blogserver.runtime.plugin.RulePlugin;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ExternalPageSizePluginLoader 负责从外部 jar 动态加载分页规则插件。
 */
@Component
public class ExternalPageSizePluginLoader {

    private final ExternalPluginDescriptorReader descriptorReader;

    public ExternalPageSizePluginLoader(ExternalPluginDescriptorReader descriptorReader) {
        this.descriptorReader = descriptorReader;
    }

    /**
     * 从指定 jar 与主类加载外部分页插件。
     *
     * @param jarPath 插件 jar 路径
     * @param className 插件主类全限定名
     * @return 外部插件加载结果
     */
    @SuppressWarnings("unchecked")
    public ExternalPageSizePluginLoadResult load(String jarPath, String className) {
        // 基本入参校验
        if (jarPath == null || jarPath.trim().isEmpty()) {
            throw new IllegalArgumentException("插件 jar 路径不能为空。");
        }
        if (className == null || className.trim().isEmpty()) {
            throw new IllegalArgumentException("插件主类名不能为空。");
        }

        // 统一使用绝对路径，避免不同启动目录下解析不一致。
        Path pluginJarPath = Paths.get(jarPath.trim()).toAbsolutePath().normalize();

        if (!Files.exists(pluginJarPath)) {
            throw new IllegalStateException("未找到外部插件 jar 文件：" + pluginJarPath);
        }

        try {
            URL jarUrl = pluginJarPath.toUri().toURL();

            // 以主系统中的 RulePlugin 类加载器作为父加载器，保证接口类型一致。
            URLClassLoader classLoader = new URLClassLoader(
                    new URL[]{jarUrl},
                    RulePlugin.class.getClassLoader()
            );

            Class<?> pluginClass = classLoader.loadClass(className.trim());

            // 只允许加载实现了统一插件接口的类。
            if (!RulePlugin.class.isAssignableFrom(pluginClass)) {
                closeQuietly(classLoader);
                throw new IllegalStateException("目标类未实现 RulePlugin 接口：" + className);
            }

            Object pluginObject = pluginClass.getDeclaredConstructor().newInstance();
            RulePlugin<PageSizeRuleInput, PageSizeRuleOutput> plugin =
                    (RulePlugin<PageSizeRuleInput, PageSizeRuleOutput>) pluginObject;

            // 这里只负责加载，init 和切换由上层引擎控制。
            return new ExternalPageSizePluginLoadResult(plugin, classLoader, pluginJarPath.toString(), className.trim());
        } catch (Exception ex) {
            throw new IllegalStateException("外部分页插件加载失败：" + ex.getMessage(), ex);
        }
    }

    /**
     * 从插件 jar 读取元信息并加载外部分页插件。
     *
     * @param jarPath 插件 jar 路径
     * @return 外部插件加载结果
     */
    public ExternalPageSizePluginLoadResult loadFromJar(String jarPath) {
        ExternalPluginDescriptor descriptor = descriptorReader.read(jarPath);
        return load(jarPath, descriptor.getClassName());
    }

    /**
     * 安静地关闭类加载器。
     *
     * @param classLoader 待关闭类加载器
     */
    private void closeQuietly(URLClassLoader classLoader) {
        // 清理失败不应覆盖主流程异常。
        try {
            classLoader.close();
        } catch (IOException ignored) {
        }
    }
}
