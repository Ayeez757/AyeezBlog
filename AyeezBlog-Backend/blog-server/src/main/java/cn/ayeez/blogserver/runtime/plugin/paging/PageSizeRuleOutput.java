package cn.ayeez.blogserver.runtime.plugin.paging;

/**
 * PageSizeRuleOutput 封装分页规则计算结果。
 */
public class PageSizeRuleOutput {

    private final int effectivePageSize;
    private final String reason;

    /**
     * 创建分页规则输出参数。
     *
     * @param effectivePageSize 最终生效分页大小
     * @param reason 规则命中原因
     */
    public PageSizeRuleOutput(int effectivePageSize, String reason) {
        this.effectivePageSize = effectivePageSize;
        this.reason = reason;
    }

    /**
     * 返回最终生效分页大小。
     *
     * @return 生效分页大小
     */
    public int getEffectivePageSize() {
        return effectivePageSize;
    }

    /**
     * 返回规则命中原因。
     *
     * @return 规则命中原因
     */
    public String getReason() {
        return reason;
    }
}
