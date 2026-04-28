package cn.ayeez.blogserver.runtime.plugin.paging;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * ExternalPageSizePluginWatchService 自动监听插件目录变更并触发外部插件加载。
 */
@Component
public class ExternalPageSizePluginWatchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalPageSizePluginWatchService.class);

    private static final long RELOAD_DEBOUNCE_MS = 1200L;
    private static final long STABLE_CHECK_SLEEP_MS = 250L;

    private final ExternalPageSizePluginLoader externalPageSizePluginLoader;
    private final PageSizeRuleEngineService pageSizeRuleEngineService;

    private WatchService watchService;
    private Thread watchThread;
    private volatile boolean running;
    private volatile long lastReloadAt = 0L;

    public ExternalPageSizePluginWatchService(ExternalPageSizePluginLoader externalPageSizePluginLoader,
                                              PageSizeRuleEngineService pageSizeRuleEngineService) {
        this.externalPageSizePluginLoader = externalPageSizePluginLoader;
        this.pageSizeRuleEngineService = pageSizeRuleEngineService;
    }

    @PostConstruct
    public void startWatching() {
        Optional<Path> pluginDir = resolvePluginDir();
        if (pluginDir.isEmpty()) {
            LOGGER.info("External plugin auto-reload disabled: PAGE_SIZE_PLUGIN_DIR not set.");
            return;
        }
        Path dir = pluginDir.get();
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException ex) {
                LOGGER.warn("External plugin auto-reload disabled: failed to create plugin dir. dir={}", dir, ex);
                return;
            }
        }
        if (!Files.isDirectory(dir)) {
            LOGGER.warn("External plugin auto-reload disabled: plugin dir is not a directory. dir={}", dir);
            return;
        }

        try {
            watchService = FileSystems.getDefault().newWatchService();
            dir.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);
        } catch (IOException ex) {
            LOGGER.warn("External plugin auto-reload disabled: failed to init WatchService.", ex);
            return;
        }

        running = true;
        watchThread = new Thread(() -> watchLoop(dir), "external-page-size-plugin-watch-thread");
        watchThread.setDaemon(true);
        watchThread.start();
        LOGGER.info("External plugin watch started. dir={}", dir.toAbsolutePath());
    }

    private void watchLoop(Path pluginDir) {
        while (running) {
            WatchKey watchKey;
            try {
                watchKey = watchService.take();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                LOGGER.error("External plugin watch loop terminated unexpectedly.", ex);
                return;
            }

            boolean shouldReload = false;
            for (WatchEvent<?> event : watchKey.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }
                Path changed = (Path) event.context();
                if (changed == null) {
                    continue;
                }
                String name = changed.getFileName().toString();
                if (name.endsWith(".jar") && !name.endsWith(".tmp.jar") && !name.endsWith(".jar.tmp")) {
                    shouldReload = true;
                    break;
                }
            }

            boolean valid = watchKey.reset();
            if (!valid) {
                LOGGER.warn("External plugin watch key is no longer valid, stop watching.");
                break;
            }

            if (shouldReload) {
                triggerReloadWithDebounce(pluginDir);
            }
        }
    }

    private void triggerReloadWithDebounce(Path pluginDir) {
        long now = System.currentTimeMillis();
        if (now - lastReloadAt < RELOAD_DEBOUNCE_MS) {
            return;
        }
        lastReloadAt = now;

        try {
            Path jarPath = pickLatestJar(pluginDir)
                    .orElseThrow(() -> new IllegalStateException("插件目录下未找到可加载的 .jar 文件。"));
            if (!isStableFile(jarPath)) {
                LOGGER.info("External plugin jar not stable yet, skip this round. jar={}", jarPath);
                return;
            }
            ExternalPageSizePluginLoadResult loadResult = externalPageSizePluginLoader.loadFromJar(jarPath.toString());
            pageSizeRuleEngineService.switchToExternalPlugin(loadResult, "auto-watch");
            LOGGER.info("External plugin auto-reload succeeded. jar={}", jarPath);
        } catch (Exception ex) {
            LOGGER.warn("External plugin auto-reload failed, keep current plugin. reason={}", ex.getMessage(), ex);
        }
    }

    private Optional<Path> pickLatestJar(Path pluginDir) {
        try (Stream<Path> stream = Files.list(pluginDir)) {
            return stream
                    .filter(p -> p.getFileName().toString().endsWith(".jar"))
                    .filter(p -> !p.getFileName().toString().endsWith(".tmp.jar"))
                    .filter(p -> !p.getFileName().toString().endsWith(".jar.tmp"))
                    .max(Comparator.comparingLong(p -> p.toFile().lastModified()));
        } catch (IOException ex) {
            return Optional.empty();
        }
    }

    private boolean isStableFile(Path jarPath) {
        try {
            long size1 = Files.size(jarPath);
            Thread.sleep(STABLE_CHECK_SLEEP_MS);
            long size2 = Files.size(jarPath);
            return size1 > 0 && size1 == size2;
        } catch (Exception ex) {
            return false;
        }
    }

    private Optional<Path> resolvePluginDir() {
        String fromEnv = System.getenv("PAGE_SIZE_PLUGIN_DIR");
        String raw = (fromEnv == null || fromEnv.trim().isEmpty()) ? null : fromEnv.trim();
        if (raw == null) {
            return Optional.empty();
        }
        return Optional.of(Paths.get(raw).toAbsolutePath().normalize());
    }

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

