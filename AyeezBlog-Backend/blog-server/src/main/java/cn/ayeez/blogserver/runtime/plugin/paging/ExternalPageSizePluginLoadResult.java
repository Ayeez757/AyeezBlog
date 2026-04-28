package cn.ayeez.blogserver.runtime.plugin.paging;

import cn.ayeez.blogserver.runtime.plugin.RulePlugin;

import java.net.URLClassLoader;

/**
 * ExternalPageSizePluginLoadResult 表示一次外部分页插件加载结果。
 */
public class ExternalPageSizePluginLoadResult {

    private final RulePlugin<PageSizeRuleInput, PageSizeRuleOutput> plugin;
    private final URLClassLoader classLoader;
    private final String jarPath;
    private final String className;

    /**
     * 创建外部分页插件加载结果。
     *
     * @param plugin 外部插件实例
     * @param classLoader 对应类加载器
     * @param jarPath 插件 jar 路径
     * @param className 插件主类名
     */
    public ExternalPageSizePluginLoadResult(RulePlugin<PageSizeRuleInput, PageSizeRuleOutput> plugin,
                                            URLClassLoader classLoader,
                                            String jarPath,
                                            String className) {
        this.plugin = plugin;
        this.classLoader = classLoader;
        this.jarPath = jarPath;
        this.className = className;
    }

    /**
     * 返回外部插件实例。
     *
     * @return 外部插件实例
     */
    public RulePlugin<PageSizeRuleInput, PageSizeRuleOutput> getPlugin() {
        return plugin;
    }

    /**
     * 返回外部插件类加载器。
     *
     * @return 外部插件类加载器
     */
    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * 返回插件 jar 路径。
     *
     * @return 插件 jar 路径
     */
    public String getJarPath() {
        return jarPath;
    }

    /**
     * 返回插件主类名。
     *
     * @return 插件主类名
     */
    public String getClassName() {
        return className;
    }
}
