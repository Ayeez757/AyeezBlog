package cn.ayeez.blogserver.runtime.plugin;

import cn.ayeez.blogserver.runtime.RuntimeConfigManager;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * PluginContext 是主系统提供给规则插件的受控依赖边界。
 */
public class PluginContext {

    private final RuntimeConfigManager runtimeConfigManager;
    // 以插件实例为 key 维护清理动作，保证“谁创建资源，谁被回收”。
    private final Map<Object, List<Runnable>> cleanupActions = new IdentityHashMap<>();
    // 仅在插件 init 阶段设置，便于插件通过上下文登记本实例资源。
    private final ThreadLocal<Object> initOwner = new ThreadLocal<>();

    /**
     * 创建插件上下文。
     *
     * @param runtimeConfigManager 运行时配置管理器
     */
    public PluginContext(RuntimeConfigManager runtimeConfigManager) {
        this.runtimeConfigManager = runtimeConfigManager;
    }

    /**
     * 返回运行时配置管理器。
     *
     * @return 运行时配置管理器
     */
    public RuntimeConfigManager getRuntimeConfigManager() {
        return runtimeConfigManager;
    }

    /**
     * 返回当前运行时配置是否启用平台统一资源回收。
     *
     * @return true 表示启用平台回收
     */
    public boolean isPlatformCleanupEnabled() {
        return runtimeConfigManager.getCurrent().isPluginCleanupEnabled();
    }

    /**
     * 标记“当前正在初始化的插件实例”。
     * <p>
     * 引擎在调用插件 init 前后成对调用 begin/end，使插件无需自己传 owner 也能登记资源。
     * </p>
     *
     * @param pluginOwner 插件实例
     */
    public void beginPluginInitScope(Object pluginOwner) {
        initOwner.set(pluginOwner);
    }

    /**
     * 结束当前插件初始化作用域。
     */
    public void endPluginInitScope() {
        initOwner.remove();
    }

    /**
     * 为“当前正在初始化的插件实例”登记一个清理动作。
     * <p>
     * 该动作由平台在插件下线时统一执行，作为 dispose() 的兜底回收机制。
     * </p>
     *
     * @param action 清理动作
     */
    public synchronized void registerCleanupAction(Runnable action) {
        Object owner = initOwner.get();
        if (owner == null) {
            throw new IllegalStateException("registerCleanupAction must be called during plugin init scope.");
        }
        cleanupActions.computeIfAbsent(owner, k -> new ArrayList<>()).add(action);
    }

    /**
     * 执行并移除某个插件实例已登记的清理动作。
     *
     * @param pluginOwner 插件实例
     */
    public synchronized void cleanupRegisteredResources(Object pluginOwner) {
        // 是否执行兜底回收由主程序 runtime-config.yml 决定，而不是插件自行决定。
        if (!isPlatformCleanupEnabled()) {
            cleanupActions.remove(pluginOwner);
            return;
        }
        List<Runnable> actions = cleanupActions.remove(pluginOwner);
        if (actions == null || actions.isEmpty()) {
            return;
        }
        for (Runnable action : actions) {
            try {
                action.run();
            } catch (Exception ignored) {
                // 单个动作失败不应阻断其它清理动作。
            }
        }
    }
}
