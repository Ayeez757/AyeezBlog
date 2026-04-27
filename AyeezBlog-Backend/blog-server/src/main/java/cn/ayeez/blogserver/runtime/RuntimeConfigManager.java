package cn.ayeez.blogserver.runtime;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

/**
 * RuntimeConfigManager 维护“当前正在生效”的运行时配置，并提供原子切换能力。
 * <p>
 * 你可以把它理解为“配置总闸门”：
 * <ul>
 *     <li>业务线程通过 getCurrent() 读取当前配置</li>
 *     <li>管理线程通过 reload() 尝试加载新配置并切换</li>
 * </ul>
 * 为了并发安全，这里使用 AtomicReference 持有配置引用。
 * </p>
 */
@Component
public class RuntimeConfigManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeConfigManager.class);

    private final RuntimeConfigLoader runtimeConfigLoader;

    private final AtomicReference<RuntimeConfig> currentConfig = new AtomicReference<>();

    /**
     * 创建运行时配置管理器。
     *
     * @param runtimeConfigLoader 配置读取与校验器
     */
    public RuntimeConfigManager(RuntimeConfigLoader runtimeConfigLoader) {
        this.runtimeConfigLoader = runtimeConfigLoader;
    }

    /**
     * 在应用启动时加载初始配置，确保服务启动后立即具备可读配置。
     */
    @PostConstruct
    public void initialize() {
        // 启动阶段必须先拿到一份可用配置，否则运行中无法保证行为确定性。
        RuntimeConfig initialConfig = runtimeConfigLoader.load();
        // 原子设置当前配置快照。
        currentConfig.set(initialConfig);
        LOGGER.info("Runtime config initialized. postPageSize={}, strictModeEnabled={}",
                initialConfig.getPostPageSize(), initialConfig.isStrictModeEnabled());
    }

    /**
     * 获取当前生效的配置快照。
     *
     * @return 当前配置
     */
    public RuntimeConfig getCurrent() {
        // AtomicReference#get 是无锁读，适合高频请求场景。
        RuntimeConfig config = currentConfig.get();
        if (config == null) {
            // 正常流程不会触发：这里只是防御性保护，避免返回 null 给业务。
            throw new IllegalStateException("Runtime config is not initialized.");
        }
        return config;
    }

    /**
     * 重新加载 runtime-config.yml，并在成功后原子切换到新版本。
     * <p>
     * 失败时保留旧配置，避免服务因错误配置进入不可用状态。
     * </p>
     *
     * @return 重载是否成功
     */
    public boolean reload() {
        try {
            // 第一步：先加载并校验新配置（若失败，直接走 catch，不影响旧配置）。
            RuntimeConfig newConfig = runtimeConfigLoader.load();
            // 第二步：加载成功后再原子替换当前引用（切换动作是“一步完成”）。
            currentConfig.set(newConfig);
            LOGGER.info("Runtime config reloaded successfully. postPageSize={}, strictModeEnabled={}",
                    newConfig.getPostPageSize(), newConfig.isStrictModeEnabled());
            return true;
        } catch (Exception ex) {
            // 关键点：失败时不改 currentConfig，旧配置继续服务（这就是“失败回退”）。
            LOGGER.error("Runtime config reload failed. Keep previous config in use.", ex);
            return false;
        }
    }
}
