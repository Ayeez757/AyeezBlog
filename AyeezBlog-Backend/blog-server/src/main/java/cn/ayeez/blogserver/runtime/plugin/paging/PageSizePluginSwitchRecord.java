package cn.ayeez.blogserver.runtime.plugin.paging;

import java.time.LocalDateTime;

/**
 * PageSizePluginSwitchRecord 记录一次插件切换/加载事件（成功或失败）。
 */
public class PageSizePluginSwitchRecord {

    private final LocalDateTime at;
    private final boolean success;
    private final String source;
    private final String origin;
    private final String pluginId;
    private final String jarPath;
    private final String className;
    private final String message;

    public PageSizePluginSwitchRecord(LocalDateTime at,
                                      boolean success,
                                      String source,
                                      String origin,
                                      String pluginId,
                                      String jarPath,
                                      String className,
                                      String message) {
        this.at = at;
        this.success = success;
        this.source = source;
        this.origin = origin;
        this.pluginId = pluginId;
        this.jarPath = jarPath;
        this.className = className;
        this.message = message;
    }

    public LocalDateTime getAt() {
        return at;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getSource() {
        return source;
    }

    public String getOrigin() {
        return origin;
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getJarPath() {
        return jarPath;
    }

    public String getClassName() {
        return className;
    }

    public String getMessage() {
        return message;
    }
}

