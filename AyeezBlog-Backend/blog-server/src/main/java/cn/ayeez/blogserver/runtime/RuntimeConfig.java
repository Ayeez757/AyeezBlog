package cn.ayeez.blogserver.runtime;

/**
 * RuntimeConfig 表示可在进程运行期间动态切换的配置快照。
 * <p>
 * 这里可以把它理解为“某一时刻的完整配置照片”：
 * <ul>
 *     <li>重载成功时，不是修改旧对象里的字段，而是创建一个新对象整体替换。</li>
 *     <li>业务线程读取的永远是当前快照，避免“读到一半被改值”的并发问题。</li>
 * </ul>
 * </p>
 */
public class RuntimeConfig {

    /**
     * 用户侧列表接口默认分页大小。
     */
    private int postPageSize = 10;

    /**
     * 是否开启严格模式开关，用于演示配置对业务行为的影响。
     */
    private boolean strictModeEnabled = false;

    /**
     * 返回默认分页大小。
     * <p>
     * 业务代码会通过 RuntimeConfigManager#getCurrent() 拿到当前快照，再调用本方法读取值。
     * </p>
     *
     * @return 默认分页大小
     */
    public int getPostPageSize() {
        return postPageSize;
    }

     /**
     * 设置默认分页大小。
     * <p>
     * 该方法主要用于“配置文件反序列化”阶段，运行时不建议业务代码主动修改。
     * 正确做法是：创建新 RuntimeConfig 后，通过 AtomicReference 一次性替换。
     * </p>
     *
     * @param postPageSize 默认分页大小
     */
    public void setPostPageSize(int postPageSize) {
        this.postPageSize = postPageSize;
    }

    /**
     * 返回严格模式开关状态。
     * <p>
     * 这个字段用于演示“配置改变会影响业务行为”。
     * </p>
     *
     * @return 开关是否开启
     */
    public boolean isStrictModeEnabled() {
        return strictModeEnabled;
    }

    /**
     * 设置严格模式开关状态。
     * <p>
     * 同样主要用于配置加载阶段，不建议在业务线程中直接改当前对象。
     * </p>
     *
     * @param strictModeEnabled 开关状态
     */
    public void setStrictModeEnabled(boolean strictModeEnabled) {
        this.strictModeEnabled = strictModeEnabled;
    }
}
