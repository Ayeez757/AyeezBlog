package cn.ayeez.blogserver.runtime;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Optional;

/**
 * RuntimeConfigWatchService 使用 WatchService 自动监听 runtime-config.yml 变更并触发重载。
 */
@Component
public class RuntimeConfigWatchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeConfigWatchService.class);

    /**
     * 文件保存时常出现短时间多次修改事件，设置防抖窗口避免重复重载。
     */
    private static final long RELOAD_DEBOUNCE_MS = 800L;

    private final RuntimeConfigLoader runtimeConfigLoader;
    private final RuntimeConfigManager runtimeConfigManager;

    private WatchService watchService;
    private Thread watchThread;
    private volatile boolean running;
    private volatile long lastReloadAt = 0L;

    /**
     * 创建配置文件监听器。
     *
     * @param runtimeConfigLoader 运行时配置加载器
     * @param runtimeConfigManager 运行时配置管理器
     */
    public RuntimeConfigWatchService(RuntimeConfigLoader runtimeConfigLoader,
                                     RuntimeConfigManager runtimeConfigManager) {
        this.runtimeConfigLoader = runtimeConfigLoader;
        this.runtimeConfigManager = runtimeConfigManager;
    }

    /**
     * 应用启动后初始化 WatchService 监听线程。
     */
    @PostConstruct
    public void startWatching() {
        Optional<Path> watchableConfigPath = runtimeConfigLoader.getWatchableConfigPath();
        if (watchableConfigPath.isEmpty()) {
            LOGGER.warn("Runtime config auto-reload disabled: no watchable filesystem config path found.");
            return;
        }

        Path configPath = watchableConfigPath.get();
        Path configDir = configPath.getParent();
        if (configDir == null || !Files.exists(configDir)) {
            LOGGER.warn("Runtime config auto-reload disabled: config directory not found. path={}", configPath);
            return;
        }

        try {
            watchService = FileSystems.getDefault().newWatchService();
            configDir.register(watchService,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE);
        } catch (IOException ex) {
            LOGGER.error("Runtime config auto-reload disabled: failed to initialize WatchService.", ex);
            return;
        }

        running = true;
        watchThread = new Thread(() -> watchLoop(configPath), "runtime-config-watch-thread");
        watchThread.setDaemon(true);
        watchThread.start();
        LOGGER.info("Runtime config watch started. path={}", configPath);
    }

    /**
     * WatchService 事件循环。
     *
     * @param configPath 需要监听的目标配置文件
     */
    private void watchLoop(Path configPath) {
        String fileName = configPath.getFileName().toString();
        while (running) {
            WatchKey watchKey;
            try {
                watchKey = watchService.take();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                LOGGER.error("Runtime config watch loop terminated unexpectedly.", ex);
                return;
            }

            boolean shouldReload = false;
            for (WatchEvent<?> event : watchKey.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }
                Path changed = (Path) event.context();
                if (changed != null && fileName.equals(changed.toString())) {
                    shouldReload = true;
                    break;
                }
            }

            boolean valid = watchKey.reset();
            if (!valid) {
                LOGGER.warn("Runtime config watch key is no longer valid, stop watching.");
                break;
            }

            if (shouldReload) {
                triggerReloadWithDebounce(configPath);
            }
        }
    }

    /**
     * 带防抖的自动重载触发逻辑。
     *
     * @param configPath 配置文件路径
     */
    private void triggerReloadWithDebounce(Path configPath) {
        long now = System.currentTimeMillis();
        if (now - lastReloadAt < RELOAD_DEBOUNCE_MS) {
            LOGGER.debug("Skip runtime config reload due to debounce window. path={}", configPath);
            return;
        }
        lastReloadAt = now;
        boolean success = runtimeConfigManager.reload();
        if (success) {
            LOGGER.info("Runtime config auto-reload succeeded. path={}", configPath);
        } else {
            LOGGER.warn("Runtime config auto-reload failed, keep previous config. path={}", configPath);
        }
    }

    /**
     * 应用关闭时停止监听线程并释放 WatchService。
     */
    @PreDestroy
    public void stopWatching() {
        running = false;
        if (watchThread != null) {
            watchThread.interrupt();
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException ex) {
                LOGGER.warn("Failed to close WatchService cleanly.", ex);
            }
        }
    }
}
