package cn.ayeez.blogserver.runtime.plugin.paging;

import cn.ayeez.blogserver.runtime.RuntimeConfig;
import cn.ayeez.blogserver.runtime.RuntimeConfigManager;
import cn.ayeez.blogserver.runtime.plugin.PluginContext;
import cn.ayeez.blogserver.runtime.plugin.RulePlugin;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.net.URLClassLoader;

/**
 * PageSizeRuleEngineService 负责委托分页规则插件执行。
 */
@Service
public class PageSizeRuleEngineService {

    private static final String DEFAULT_PLUGIN_ID = "default-page-size-rule";

    private final RuntimeConfigManager runtimeConfigManager;
    private final Map<String, RulePlugin<PageSizeRuleInput, PageSizeRuleOutput>> pageSizeRulePlugins;
    private PluginContext pluginContext;
    private final AtomicReference<RulePlugin<PageSizeRuleInput, PageSizeRuleOutput>> currentPlugin = new AtomicReference<>();
    private volatile LocalDateTime lastSwitchedAt;
    private volatile String lastSwitchSource = "startup";
    private volatile String currentPluginOrigin = "built-in";
    private volatile String currentExternalJarPath;
    private volatile String currentExternalClassName;
    private volatile URLClassLoader currentExternalClassLoader;
    private final Deque<PageSizePluginSwitchRecord> switchHistory = new ArrayDeque<>();
    private static final int SWITCH_HISTORY_MAX = 50;

    /**
     * 创建分页规则引擎服务。
     *
     * @param runtimeConfigManager 运行时配置管理器
     * @param pageSizeRulePlugins 分页规则插件实现列表
     */
    public PageSizeRuleEngineService(RuntimeConfigManager runtimeConfigManager,
                                     List<RulePlugin<PageSizeRuleInput, PageSizeRuleOutput>> pageSizeRulePlugins) {
        this.runtimeConfigManager = runtimeConfigManager;
        this.pageSizeRulePlugins = new LinkedHashMap<>();
        for (RulePlugin<PageSizeRuleInput, PageSizeRuleOutput> plugin : pageSizeRulePlugins) {
            this.pageSizeRulePlugins.put(plugin.id(), plugin);
        }
    }

    /**
     * 启动时初始化插件。
     */
    @PostConstruct
    public void init() {
        pluginContext = new PluginContext(runtimeConfigManager);
        RuntimeConfig runtimeConfig = runtimeConfigManager.getCurrent();
        switchPlugin(runtimeConfig.getPageSizeRulePluginId(), "startup");
    }

    /**
     * 计算本次请求的生效分页大小。
     *
     * @param requestedPageSize 前端请求分页大小
     * @return 分页规则输出
     */
    public PageSizeRuleOutput resolvePageSize(Integer requestedPageSize) {
        // 外部插件一旦手动接管，就保持当前外部实现，避免被 runtime-config 每次请求自动切回。
        if (!"external".equals(currentPluginOrigin)) {
            RuntimeConfig runtimeConfig = runtimeConfigManager.getCurrent();
            switchPlugin(runtimeConfig.getPageSizeRulePluginId(), "runtime-config");
        }
        RulePlugin<PageSizeRuleInput, PageSizeRuleOutput> plugin = currentPlugin.get();
        if (plugin == null) {
            throw new IllegalStateException("当前没有可用的分页规则插件。");
        }
        return plugin.execute(new PageSizeRuleInput(requestedPageSize));
    }

    /**
     * 返回当前生效插件 ID。
     *
     * @return 当前插件 ID
     */
    public String getCurrentPluginId() {
        RulePlugin<PageSizeRuleInput, PageSizeRuleOutput> plugin = currentPlugin.get();
        return plugin == null ? null : plugin.id();
    }

    /**
     * 返回当前可用插件列表。
     *
     * @return 插件 ID 列表
     */
    public List<String> getAvailablePluginIds() {
        return List.copyOf(pageSizeRulePlugins.keySet());
    }

    /**
     * 返回最近一次插件切换时间。
     *
     * @return 最近切换时间
     */
    public LocalDateTime getLastSwitchedAt() {
        return lastSwitchedAt;
    }

    /**
     * 返回最近一次插件切换来源。
     *
     * @return 切换来源
     */
    public String getLastSwitchSource() {
        return lastSwitchSource;
    }

    /**
     * 返回当前插件来源类型。
     *
     * @return 插件来源类型
     */
    public String getCurrentPluginOrigin() {
        return currentPluginOrigin;
    }

    /**
     * 返回当前外部插件 jar 路径。
     *
     * @return 外部插件 jar 路径
     */
    public String getCurrentExternalJarPath() {
        return currentExternalJarPath;
    }

    /**
     * 返回当前外部插件主类名。
     *
     * @return 外部插件主类名
     */
    public String getCurrentExternalClassName() {
        return currentExternalClassName;
    }

    /**
     * 返回最近 N 次插件切换记录（按时间倒序）。
     *
     * @param limit 最大条数
     * @return 切换记录列表
     */
    public synchronized List<PageSizePluginSwitchRecord> getSwitchHistory(int limit) {
        int size = Math.max(1, Math.min(limit, SWITCH_HISTORY_MAX));
        return switchHistory.stream().limit(size).toList();
    }

    /**
     * 切换当前生效插件。
     *
     * @param pluginId 目标插件 ID
     */
    public synchronized void switchPlugin(String pluginId) {
        switchPlugin(pluginId, "manual");
    }

    /**
     * 切换当前生效插件。
     * <p>
     * 采用“先初始化新插件，再替换当前引用，最后释放旧插件”的顺序，
     * 目的是避免新插件初始化失败时影响当前仍在服务的旧插件。
     * </p>
     *
     * @param pluginId 目标插件 ID
     * @param source 切换来源
     */
    public synchronized void switchPlugin(String pluginId, String source) {
        String targetPluginId = pluginId;
        if (targetPluginId == null || targetPluginId.trim().isEmpty()) {
            targetPluginId = DEFAULT_PLUGIN_ID;
        }
        RulePlugin<PageSizeRuleInput, PageSizeRuleOutput> targetPlugin = pageSizeRulePlugins.get(targetPluginId);
        if (targetPlugin == null) {
            throw new IllegalStateException("未找到分页规则插件：" + targetPluginId);
        }
        RulePlugin<PageSizeRuleInput, PageSizeRuleOutput> oldPlugin = currentPlugin.get();
        URLClassLoader oldExternalClassLoader = currentExternalClassLoader;
        if (oldPlugin != null && oldPlugin.id().equals(targetPluginId)) {
            return;
        }

        // 先初始化新插件，确保其可用后再替换当前引用。
        targetPlugin.init(pluginContext);
        currentPlugin.set(targetPlugin);
        lastSwitchedAt = LocalDateTime.now();
        lastSwitchSource = source;
        currentPluginOrigin = "built-in";
        currentExternalJarPath = null;
        currentExternalClassName = null;
        currentExternalClassLoader = null;
        recordSwitch(true, source, "built-in", targetPlugin.id(), null, null, "切换到内置插件成功");

        // 旧插件在新插件生效后再释放，避免切换中间窗口没有可用插件。
        if (oldPlugin != null) {
            oldPlugin.dispose();
        }
        closeClassLoaderQuietly(oldExternalClassLoader);
    }

    /**
     * 切换到外部分页规则插件。
     *
     * @param loadResult 外部插件加载结果
     * @param source 切换来源
     */
    public synchronized void switchToExternalPlugin(ExternalPageSizePluginLoadResult loadResult, String source) {
        if (loadResult == null || loadResult.getPlugin() == null) {
            throw new IllegalArgumentException("外部插件加载结果不能为空。");
        }
        RulePlugin<PageSizeRuleInput, PageSizeRuleOutput> newPlugin = loadResult.getPlugin();
        RulePlugin<PageSizeRuleInput, PageSizeRuleOutput> oldPlugin = currentPlugin.get();
        URLClassLoader oldExternalClassLoader = currentExternalClassLoader;

        // 先初始化新插件，失败时保留旧插件继续服务。
        newPlugin.init(pluginContext);
        currentPlugin.set(newPlugin);
        lastSwitchedAt = LocalDateTime.now();
        lastSwitchSource = source;
        currentPluginOrigin = "external";
        currentExternalJarPath = loadResult.getJarPath();
        currentExternalClassName = loadResult.getClassName();
        currentExternalClassLoader = loadResult.getClassLoader();
        recordSwitch(true, source, "external", newPlugin.id(), currentExternalJarPath, currentExternalClassName,
                "切换到外部插件成功");

        if (oldPlugin != null) {
            oldPlugin.dispose();
        }
        closeClassLoaderQuietly(oldExternalClassLoader);
    }

    /**
     * 从“外部接管模式”回退到当前配置指定的内置插件。
     */
    public synchronized void revertToConfiguredBuiltIn(String source) {
        RuntimeConfig runtimeConfig = runtimeConfigManager.getCurrent();
        switchPlugin(runtimeConfig.getPageSizeRulePluginId(), source);
    }

    /**
     * 停止时释放插件资源。
     */
    @PreDestroy
    public void destroy() {
        RulePlugin<PageSizeRuleInput, PageSizeRuleOutput> plugin = currentPlugin.get();
        if (plugin != null) {
            plugin.dispose();
        }
        closeClassLoaderQuietly(currentExternalClassLoader);
    }

    /**
     * 安静地关闭旧类加载器。
     *
     * @param classLoader 旧类加载器
     */
    private void closeClassLoaderQuietly(URLClassLoader classLoader) {
        if (classLoader == null) {
            return;
        }
        try {
            classLoader.close();
        } catch (IOException ignored) {
        }
    }

    private void recordSwitch(boolean success,
                              String source,
                              String origin,
                              String pluginId,
                              String jarPath,
                              String className,
                              String message) {
        switchHistory.addFirst(new PageSizePluginSwitchRecord(LocalDateTime.now(), success, source, origin,
                pluginId, jarPath, className, message));
        while (switchHistory.size() > SWITCH_HISTORY_MAX) {
            switchHistory.removeLast();
        }
    }
}
