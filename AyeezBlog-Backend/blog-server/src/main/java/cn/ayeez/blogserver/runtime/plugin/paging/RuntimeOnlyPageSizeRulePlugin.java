package cn.ayeez.blogserver.runtime.plugin.paging;

import cn.ayeez.blogserver.runtime.RuntimeConfig;
import cn.ayeez.blogserver.runtime.RuntimeConfigManager;
import cn.ayeez.blogserver.runtime.plugin.PluginContext;
import cn.ayeez.blogserver.runtime.plugin.RulePlugin;
import org.springframework.stereotype.Component;

/**
 * RuntimeOnlyPageSizeRulePlugin 始终使用运行时配置中的分页大小。
 */
@Component
public class RuntimeOnlyPageSizeRulePlugin implements RulePlugin<PageSizeRuleInput, PageSizeRuleOutput> {

    private RuntimeConfigManager runtimeConfigManager;

    @Override
    public String id() {
        return "runtime-only-page-size-rule";
    }

    @Override
    public void init(PluginContext context) {
        this.runtimeConfigManager = context.getRuntimeConfigManager();
    }

    @Override
    public PageSizeRuleOutput execute(PageSizeRuleInput input) {
        if (runtimeConfigManager == null) {
            throw new IllegalStateException("分页规则插件尚未初始化，请检查插件引擎初始化流程。");
        }
        RuntimeConfig runtimeConfig = runtimeConfigManager.getCurrent();
        return new PageSizeRuleOutput(runtimeConfig.getPostPageSize(), "运行时优先插件：始终使用运行时配置");
    }

    @Override
    public void dispose() {
        this.runtimeConfigManager = null;
    }  
}
