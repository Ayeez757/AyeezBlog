package cn.ayeez.blogplugindemo.paging;

import cn.ayeez.blogserver.runtime.RuntimeConfig;
import cn.ayeez.blogserver.runtime.plugin.PluginContext;
import cn.ayeez.blogserver.runtime.plugin.RulePlugin;
import cn.ayeez.blogserver.runtime.plugin.paging.PageSizeRuleInput;
import cn.ayeez.blogserver.runtime.plugin.paging.PageSizeRuleOutput;

/**
 * DoublePageSizeRulePlugin 是 Day6 外部插件演示实现。
 */
public class DoublePageSizeRulePlugin implements RulePlugin<PageSizeRuleInput, PageSizeRuleOutput> {

    private PluginContext pluginContext;

    @Override
    public String id() {
        return "double-page-size-rule";
    }

    @Override
    public void init(PluginContext context) {
        if (context == null || context.getRuntimeConfigManager() == null) {
            throw new IllegalArgumentException("插件上下文不能为空。");
        }
        this.pluginContext = context;
    }

    @Override
    public PageSizeRuleOutput execute(PageSizeRuleInput input) {
        if (pluginContext == null) {
            throw new IllegalStateException("外部分页插件尚未初始化，不能执行规则。");
        }
        RuntimeConfig runtimeConfig = pluginContext.getRuntimeConfigManager().getCurrent();
        int effectivePageSize = runtimeConfig.getPostPageSize() * 2;
        return new PageSizeRuleOutput(effectivePageSize, "外部插件生效：分页大小固定为 runtime-config 的 2 倍");
    }

    @Override
    public void dispose() {
        this.pluginContext = null;
    }
}
