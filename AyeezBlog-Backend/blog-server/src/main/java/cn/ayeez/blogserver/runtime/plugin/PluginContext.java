package cn.ayeez.blogserver.runtime.plugin;

import cn.ayeez.blogserver.runtime.RuntimeConfigManager;

/**
 * PluginContext 是主系统提供给规则插件的受控依赖边界。
 */
public class PluginContext {

    private final RuntimeConfigManager runtimeConfigManager;

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
}
