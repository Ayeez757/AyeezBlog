package cn.ayeez.blogplugindemo.paging;

import cn.ayeez.blogserver.runtime.RuntimeConfig;
import cn.ayeez.blogserver.runtime.plugin.PluginContext;
import cn.ayeez.blogserver.runtime.plugin.RulePlugin;
import cn.ayeez.blogserver.runtime.plugin.paging.PageSizeRuleInput;
import cn.ayeez.blogserver.runtime.plugin.paging.PageSizeRuleOutput;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用于“副作用与状态残留”验证的外部插件：
 * 1) 有状态：execute 次数会影响返回值；
 * 2) 有副作用：启动心跳任务并写入本地文件；
 * 3) 是否回收由主程序 runtime-config.yml 的 pluginCleanupEnabled 控制，便于 A/B 对照测试。
 *
 * 默认 pluginCleanupEnabled=true（平台执行统一回收，避免残留风险）。
 * 如需验证“故意不清理”，将 runtime-config.yml 中 pluginCleanupEnabled 改为 false。
 */
public class StatefulSideEffectPageSizeRulePlugin implements RulePlugin<PageSizeRuleInput, PageSizeRuleOutput> {

    private static final AtomicInteger ACTIVE_HEARTBEAT_TASKS = new AtomicInteger(0);
    private static final Set<String> ACTIVE_PLUGIN_INSTANCES = ConcurrentHashMap.newKeySet();

    private final AtomicInteger executeCount = new AtomicInteger(0);
    private final String instanceId = UUID.randomUUID().toString();
    private final Path heartbeatLog = Paths.get("plugins", "page-size", "side-effect-heartbeat.log")
            .toAbsolutePath().normalize();

    private volatile PluginContext pluginContext;
    private volatile ScheduledExecutorService scheduler;
    private final AtomicBoolean released = new AtomicBoolean(false);

    @Override
    public String id() {
        return "stateful-side-effect-page-size-rule";
    }

    @Override
    public void init(PluginContext context) {
        this.pluginContext = context;
        ACTIVE_PLUGIN_INSTANCES.add(instanceId);

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "stateful-side-effect-heartbeat-" + instanceId);
            t.setDaemon(true);
            return t;
        });
        // 平台统一回收入口：登记该插件实例的兜底清理动作。
        // 这样即使插件作者忘记在 dispose 中完全释放，平台也能在下线时尝试兜底回收。
        context.registerCleanupAction(this::releaseResources);

        ACTIVE_HEARTBEAT_TASKS.incrementAndGet();
        scheduler.scheduleAtFixedRate(this::writeHeartbeat, 0, 2, TimeUnit.SECONDS);
    }

    @Override
    public PageSizeRuleOutput execute(PageSizeRuleInput input) {
        int currentCount = executeCount.incrementAndGet();
        RuntimeConfig runtimeConfig = pluginContext.getRuntimeConfigManager().getCurrent();

        // 有状态：同一插件实例内，执行次数会影响最终值（最多 +3，避免过大扰动）
        int stateBonus = Math.min(currentCount, 3);
        int effectivePageSize = runtimeConfig.getPostPageSize() * 2 + stateBonus;

        String reason = "stateful-side-effect plugin active, executeCount=" + currentCount
                + ", activeInstances=" + ACTIVE_PLUGIN_INSTANCES.size()
                + ", activeHeartbeatTasks=" + ACTIVE_HEARTBEAT_TASKS.get()
                + ", cleanupFlag=" + pluginContext.isPlatformCleanupEnabled();

        return new PageSizeRuleOutput(effectivePageSize, reason);
    }

    @Override
    public void dispose() {
        // 平台回收开启时：dispose 与 registerCleanupAction 双路径收敛到同一幂等逻辑，
        // 避免仅依赖登记 Runnable（文档推荐的做法）。
        // A/B 对照（pluginCleanupEnabled=false）时不在 dispose 里释放，便于观察「跳过平台回收」的残留。
        PluginContext ctx = pluginContext;
        if (ctx != null && ctx.isPlatformCleanupEnabled()) {
            releaseResources();
        }
    }

    /**
     * 释放心跳调度器并清理本实例在静态记账中的登记；多次调用安全。
     */
    private void releaseResources() {
        if (!released.compareAndSet(false, true)) {
            return;
        }
        ScheduledExecutorService s = scheduler;
        scheduler = null;
        if (s != null) {
            s.shutdownNow();
        }
        ACTIVE_HEARTBEAT_TASKS.updateAndGet(v -> Math.max(0, v - 1));
        ACTIVE_PLUGIN_INSTANCES.remove(instanceId);
        pluginContext = null;
    }

    private void writeHeartbeat() {
        try {
            Path parent = heartbeatLog.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            String line = LocalDateTime.now() + " instance=" + instanceId
                    + " executeCount=" + executeCount.get()
                    + " activeInstances=" + ACTIVE_PLUGIN_INSTANCES.size()
                    + System.lineSeparator();
            Files.write(heartbeatLog, line.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
        }
    }
}
