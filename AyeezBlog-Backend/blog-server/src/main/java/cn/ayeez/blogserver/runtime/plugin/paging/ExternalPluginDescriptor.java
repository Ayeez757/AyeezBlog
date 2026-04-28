package cn.ayeez.blogserver.runtime.plugin.paging;

/**
 * ExternalPluginDescriptor 表示外部插件 jar 中的元信息描述。
 */
public class ExternalPluginDescriptor {

    private final String pluginId;
    private final String className;
    private final String version;

    public ExternalPluginDescriptor(String pluginId, String className, String version) {
        this.pluginId = pluginId;
        this.className = className;
        this.version = version;
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getClassName() {
        return className;
    }

    public String getVersion() {
        return version;
    }
}

