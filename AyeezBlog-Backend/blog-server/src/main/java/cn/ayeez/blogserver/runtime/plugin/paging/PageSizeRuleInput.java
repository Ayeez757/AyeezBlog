package cn.ayeez.blogserver.runtime.plugin.paging;

/**
 * PageSizeRuleInput 封装分页规则计算所需输入。
 */
public class PageSizeRuleInput {

    private final Integer requestedPageSize;

    /**
     * 创建分页规则输入参数。
     *
     * @param requestedPageSize 前端请求的分页大小
     */
    public PageSizeRuleInput(Integer requestedPageSize) {
        this.requestedPageSize = requestedPageSize;

    }

    /**
     * 返回前端请求分页大小。
     *
     * @return 前端请求分页大小
     */
    public Integer getRequestedPageSize() {
        return requestedPageSize;
    }
}
