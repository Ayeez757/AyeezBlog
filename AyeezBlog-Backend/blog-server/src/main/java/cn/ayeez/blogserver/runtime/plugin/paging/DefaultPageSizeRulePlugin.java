package cn.ayeez.blogserver.runtime.plugin.paging;

import cn.ayeez.blogserver.runtime.RuntimeConfig;
import cn.ayeez.blogserver.runtime.RuntimeConfigManager;
import cn.ayeez.blogserver.runtime.plugin.PluginContext;
import cn.ayeez.blogserver.runtime.plugin.RulePlugin;
import org.springframework.stereotype.Component;

/**
 * DefaultPageSizeRulePlugin 是默认分页规则实现。
 * <p>
 * 该插件用于将“分页大小（pageSize）如何确定”的规则从具体业务服务中抽离出来，
 * 使得后续替换规则时无需修改 Controller 与核心业务流程代码。
 * </p>
 *
 * <h3>规则说明</h3>
 * <ul>
 *     <li><b>严格模式（strictModeEnabled=true）</b>：强制使用运行时配置中的 pageSize，用于演示/统一线上行为。</li>
 *     <li><b>非严格模式（strictModeEnabled=false）</b>：优先使用请求传入的 pageSize；若请求值为空或非法，则回退到运行时配置。</li>
 * </ul>
 */
@Component
public class DefaultPageSizeRulePlugin implements RulePlugin<PageSizeRuleInput, PageSizeRuleOutput> {

    private RuntimeConfigManager runtimeConfigManager;

    @Override
    public String id() {
        return "default-page-size-rule";
    }

    /**
     * init 是插件生命周期的“上线阶段”。
     * <p>
     * 这里不直接依赖 Spring 容器，而是通过 PluginContext 获取主系统提供的受控依赖，
     * 这样做是为了在后续支持热替换/卸载插件时，减少耦合与资源泄露风险。
     * </p>
     */
    @Override
    public void init(PluginContext context) {

        this.runtimeConfigManager = context.getRuntimeConfigManager();
    }


    /**
     * execute 是插件生命周期的“执行阶段”，会被每次业务请求调用。
     * <p>
     * 本方法必须是无副作用或副作用可控的纯规则计算逻辑：
     * 只根据输入参数与当前运行时配置，计算并返回最终生效的 pageSize。
     * </p>
     *
     * <p>
     * 返回的 reason 字段用于日志/调试，可帮助你判断本次命中了哪条规则路径。
     * </p>
     */
    @Override
    public PageSizeRuleOutput execute(PageSizeRuleInput input) {

        if (runtimeConfigManager == null) {
            throw new IllegalStateException("分页规则插件尚未初始化，请检查插件引擎初始化流程。");
        }
        RuntimeConfig runtimeConfig = runtimeConfigManager.getCurrent();
        Integer requestedPageSize = input.getRequestedPageSize();
        int runtimePageSize = runtimeConfig.getPostPageSize();
        boolean strictModeEnabled = runtimeConfig.isStrictModeEnabled();

        if (strictModeEnabled) {
            return new PageSizeRuleOutput(runtimePageSize, "严格模式：强制使用运行时配置");
        }
        if (requestedPageSize == null || requestedPageSize <= 0) {
            return new PageSizeRuleOutput(runtimePageSize, "请求 pageSize 非法：回退到运行时配置");
        }
        return new PageSizeRuleOutput(requestedPageSize, "使用请求 pageSize");
    }

    @Override
    public void dispose() {
        /**
         * dispose 是插件生命周期的“下线阶段”。
         * <p>
         * 当前插件仅持有一个 RuntimeConfigManager 引用，因此置空即可。
         * 若未来插件创建了线程池、定时器、监听器、文件句柄等资源，应在此处统一释放。
         * </p>
         */
        this.runtimeConfigManager = null;
    }
}
