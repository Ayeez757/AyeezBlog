package cn.ayeez.blogserver.runtime.plugin.paging;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
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
    private static final String DEFAULT_PLUGIN_DIR = "plugins/page-size";

    /**
     * 为 true 时，启动后延迟补扫一次插件目录：解决「jar 在 WatchService 注册前已存在」时收不到 CREATE 事件的问题（Docker 评审镜像预置 jar 场景）。
     */
    private static final String BOOT_SCAN_ENV = "EXTERNAL_PLUGIN_BOOT_SCAN";

    private final ExternalPageSizePluginLoader externalPageSizePluginLoader;
    private final PageSizeRuleEngineService pageSizeRuleEngineService;

    private WatchService watchService;
    private Thread watchThread;
    private volatile boolean running;
    private volatile long lastReloadAt = 0L;

    /**
     * 最近一次「目录里最新 jar」的稳定签名；用于轮询发现复制进目录但无 inotify 的场景。0 表示当时无 jar。
     */
    private volatile long lastPolledJarSignature = Long.MIN_VALUE;

    @Value("${blog.external-page-size-plugin.watch-poll-enabled:true}")
    private boolean pluginDirPollEnabled;

    public ExternalPageSizePluginWatchService(ExternalPageSizePluginLoader externalPageSizePluginLoader,
                                              PageSizeRuleEngineService pageSizeRuleEngineService) {
        this.externalPageSizePluginLoader = externalPageSizePluginLoader;
        this.pageSizeRuleEngineService = pageSizeRuleEngineService;
    }

    @PostConstruct
    public void startWatching() {
        // 默认启用自动监听，优先级：环境变量 > JVM参数 > 项目默认目录。
        Path dir = resolvePluginDir();
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
        scheduleBootJarScanIfEnabled(dir);
    }

    private static boolean isBootJarScanEnabled() {
        String v = System.getenv(BOOT_SCAN_ENV);
        return v != null && ("true".equalsIgnoreCase(v.trim()) || "1".equals(v.trim()));
    }

    /**
     * Docker 等场景：entrypoint 在进程启动前已放入 jar，WatchService 不会收到历史文件的创建事件，延迟触发一次与自动监听相同的加载链路。
     */
    private void scheduleBootJarScanIfEnabled(Path pluginDir) {
        if (!isBootJarScanEnabled()) {
            return;
        }
        Thread bootScan = new Thread(() -> {
            try {
                Thread.sleep(2500L);
                LOGGER.info("External plugin boot scan enabled ({}=true), scanning dir={}", BOOT_SCAN_ENV, pluginDir.toAbsolutePath());
                triggerReloadWithDebounce(pluginDir);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }, "external-page-size-plugin-boot-scan");
        bootScan.setDaemon(true);
        bootScan.start();
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
                // 仅对“看起来是最终产物”的 jar 做响应：忽略临时文件，减少半写入触发。
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

    /**
     * Docker Desktop 等环境：宿主复制 jar 进挂载目录后 inotify 常丢失；在仍为内置插件时轮询目录签名以触发与 Watch 相同的加载链路。
     * 已 external 接管时不轮询，避免与「先回退再换 jar」语义冲突；回退后若 jar 未变不会重复加载。
     */
    @Scheduled(fixedDelayString = "${blog.external-page-size-plugin.watch-poll-interval-ms:3000}")
    public void pollPluginDirectory() {
        if (!pluginDirPollEnabled) {
            return;
        }
        if ("external".equals(pageSizeRuleEngineService.getCurrentPluginOrigin())) {
            return;
        }
        Path dir = resolvePluginDir();
        if (!Files.isDirectory(dir)) {
            return;
        }
        Optional<Path> jarOpt = pickLatestJar(dir);
        long sig = jarOpt.map(this::signatureForJar).orElse(0L);
        if (sig == 0L) {
            lastPolledJarSignature = 0L;
            return;
        }
        if (lastPolledJarSignature == sig) {
            return;
        }
        if (triggerReloadWithDebounceReturning(dir)) {
            lastPolledJarSignature = sig;
        }
    }

    private long signatureForJar(Path jarPath) {
        try {
            return Files.getLastModifiedTime(jarPath).toMillis() ^ (Files.size(jarPath) << 1);
        } catch (IOException ex) {
            return -1L;
        }
    }

    private void triggerReloadWithDebounce(Path pluginDir) {
        triggerReloadWithDebounceReturning(pluginDir);
    }

    /**
     * @return 是否已成功切换为外部插件
     */
    private boolean triggerReloadWithDebounceReturning(Path pluginDir) {
        long now = System.currentTimeMillis();
        if (now - lastReloadAt < RELOAD_DEBOUNCE_MS) {
            return false;
        }
        lastReloadAt = now;

        try {
            // 选择目录内“最新”的 jar 作为候选；这比依赖文件名规则更稳健（尤其在手动复制/脚本上传场景）。
            Path jarPath = pickLatestJar(pluginDir)
                    .orElseThrow(() -> new IllegalStateException("插件目录下未找到可加载的 .jar 文件。"));
            // 避免复制/上传过程中读取到半写入 jar：简单做一次大小稳定性检查。
            if (!isStableFile(jarPath)) {
                LOGGER.info("External plugin jar not stable yet, skip this round. jar={}", jarPath);
                return false;
            }
            ExternalPageSizePluginLoadResult loadResult = externalPageSizePluginLoader.loadFromJar(jarPath.toString());
            pageSizeRuleEngineService.switchToExternalPlugin(loadResult, "auto-watch");
            LOGGER.info("External plugin auto-reload succeeded. jar={}", jarPath);
            return true;
        } catch (Exception ex) {
            // 失败不切换：保持当前插件继续服务，符合“热更新失败回退/不中断”的要求。
            LOGGER.warn("External plugin auto-reload failed, keep current plugin. reason={}", ex.getMessage(), ex);
            return false;
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

    private Path resolvePluginDir() {
        String fromEnv = System.getenv("PAGE_SIZE_PLUGIN_DIR");
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            return Paths.get(fromEnv.trim()).toAbsolutePath().normalize();
        }

        String fromJvmProp = System.getProperty("page.size.plugin.dir");
        if (fromJvmProp != null && !fromJvmProp.trim().isEmpty()) {
            return Paths.get(fromJvmProp.trim()).toAbsolutePath().normalize();
        }

        return Paths.get(DEFAULT_PLUGIN_DIR).toAbsolutePath().normalize();
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

