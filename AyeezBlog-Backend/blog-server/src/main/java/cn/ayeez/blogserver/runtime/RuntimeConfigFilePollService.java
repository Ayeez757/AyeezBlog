package cn.ayeez.blogserver.runtime;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * 通过周期性检查配置文件最后修改时间，补偿 Docker Desktop（尤其 Windows）下 bind mount 不触发 inotify、
 * {@link RuntimeConfigWatchService} 漏事件的问题。Linux 服务器原生部署时 Watch 通常正常，轮询仅多一次 stat，开销可忽略。
 */
@Component
@ConditionalOnProperty(prefix = "blog.runtime-config", name = "watch-poll-enabled", havingValue = "true", matchIfMissing = true)
public class RuntimeConfigFilePollService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeConfigFilePollService.class);

    private static final long RELOAD_DEBOUNCE_MS = 800L;

    private final RuntimeConfigLoader runtimeConfigLoader;
    private final RuntimeConfigManager runtimeConfigManager;

    private volatile long lastKnownMtimeMillis = Long.MIN_VALUE;
    private volatile long lastReloadAt;

    public RuntimeConfigFilePollService(RuntimeConfigLoader runtimeConfigLoader,
                                        RuntimeConfigManager runtimeConfigManager) {
        this.runtimeConfigLoader = runtimeConfigLoader;
        this.runtimeConfigManager = runtimeConfigManager;
    }

    @PostConstruct
    void logPollMode() {
        LOGGER.info("Runtime config file poll enabled (mtime-based fallback for Docker bind mounts).");
    }

    @Scheduled(fixedDelayString = "${blog.runtime-config.watch-poll-interval-ms:2000}")
    public void poll() {
        Optional<Path> pathOpt = runtimeConfigLoader.getWatchableConfigPath();
        if (pathOpt.isEmpty()) {
            return;
        }
        Path path = pathOpt.get();
        if (!Files.isRegularFile(path)) {
            return;
        }

        final long mtime;
        try {
            mtime = Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ex) {
            LOGGER.debug("runtime-config poll: cannot read mtime for {}: {}", path, ex.toString());
            return;
        }

        long seen = lastKnownMtimeMillis;
        if (seen == Long.MIN_VALUE) {
            lastKnownMtimeMillis = mtime;
            return;
        }
        if (mtime == seen) {
            return;
        }

        long baselineBeforeChange = seen;
        lastKnownMtimeMillis = mtime;

        long now = System.currentTimeMillis();
        if (now - lastReloadAt < RELOAD_DEBOUNCE_MS) {
            LOGGER.debug("runtime-config poll: skip reload due to debounce (mtime changed). path={}", path);
            lastKnownMtimeMillis = baselineBeforeChange;
            return;
        }
        lastReloadAt = now;

        boolean success = runtimeConfigManager.reload();
        if (success) {
            LOGGER.info("Runtime config reloaded via mtime poll. path={}", path);
            return;
        }
        lastKnownMtimeMillis = baselineBeforeChange;
        LOGGER.warn("Runtime config poll saw file change but reload failed; fix YAML and save again. path={}", path);
    }
}
