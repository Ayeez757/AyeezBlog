package cn.ayeez.blogserver.runtime.plugin;

/**
 * RulePlugin 定义可替换业务规则的统一生命周期。
 *
 * @param <I> 输入参数类型
 * @param <O> 输出结果类型
 */
public interface RulePlugin<I, O> {

    /**
     * 返回插件唯一标识。
     *
     * @return 插件 ID
     */
    String id();

    /**
     * 插件初始化，在插件切换为当前生效版本时调用。
     *
     * @param context 主系统传入的受控上下文
     */
    void init(PluginContext context);

    /**
     * 执行业务规则。
     *
     * @param input 输入参数
     * @return 规则执行结果
     */
    O execute(I input);

    /**
     * 插件下线时释放资源。
     */
    void dispose();
}
