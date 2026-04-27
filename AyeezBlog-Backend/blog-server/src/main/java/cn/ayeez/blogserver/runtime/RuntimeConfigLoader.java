package cn.ayeez.blogserver.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * RuntimeConfigLoader 负责“把文件变成可用配置对象”。
 * <p>
 * 它的职责非常单一：
 * <ul>
 *     <li>从 runtime-config.yml 读取文本</li>
 *     <li>解析成 RuntimeConfig 对象</li>
 *     <li>做基础校验，保证数据合法</li>
 * </ul>
 * 不负责“切换当前配置”，切换由 RuntimeConfigManager 完成。
 * 这样拆分的好处是：加载失败时问题定位更快（到底是读取错，还是切换错）。
 * </p>
 */
@Component
public class RuntimeConfigLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeConfigLoader.class);

    private static final String RUNTIME_CONFIG_NAME = "runtime-config.yml";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Yaml yaml = new Yaml();
    private volatile String lastLoadedFrom = "unknown";

    /**
     * 加载配置并在返回前完成业务级校验。
     * <p>
     * 流程是：
     * <ol>
     *     <li>先定位配置文件（工作目录优先，classpath 兜底）</li>
     *     <li>读取 YAML 并转成 Map</li>
     *     <li>再转成 RuntimeConfig</li>
     *     <li>最后校验字段范围</li>
     * </ol>
     * 注意：该方法只“生产新配置对象”，并不直接改线上生效对象。
     * </p>
     *
     * @return 通过校验的配置快照
     * @throws IllegalStateException 当文件不存在、格式错误或配置值非法时抛出
     */
    public RuntimeConfig load() {
        // 先找到可读资源：开发阶段更希望读运行目录文件，方便你边改边验证。
        Resource resource = resolveResource();
        String loadedFrom = describeResource(resource);
        try (InputStream inputStream = resource.getInputStream()) {
            // SnakeYAML 先把文本解析成通用 Map，便于后续做对象映射。
            Map<String, Object> configMap = yaml.load(inputStream);
            // ObjectMapper 将 Map 转成强类型对象（RuntimeConfig），后续代码更安全。
            RuntimeConfig runtimeConfig = objectMapper.convertValue(configMap, RuntimeConfig.class);
            // 校验放在“返回前”执行，确保外部拿到的一定是合法配置。
            validate(runtimeConfig);
            lastLoadedFrom = loadedFrom;
            LOGGER.info("Runtime config loaded from {}", loadedFrom);
            return runtimeConfig;
        } catch (IOException | RuntimeException ex) {
            // 统一包装异常，避免调用方关心底层是 IO、YAML 还是类型转换异常。
            throw new IllegalStateException("Failed to load runtime configuration from " + RUNTIME_CONFIG_NAME, ex);
        }
    }

    /**
     * 解析配置文件来源。
     * <p>
     * 优先读取项目运行目录下的 runtime-config.yml，便于开发期直接修改；
     * 若不存在则回退到 classpath 下同名文件，保证首次启动可用。
     * </p>
     *
     * @return 可读取的资源对象
     */
    private Resource resolveResource() {
        // 1) 支持通过 JVM 参数或环境变量显式指定路径（Docker 推荐使用环境变量）
        String explicitPath = resolveExplicitRuntimeConfigPath();
        if (explicitPath != null && !explicitPath.trim().isEmpty()) {
            FileSystemResource explicitResource = new FileSystemResource(explicitPath.trim());
            if (explicitResource.exists()) {
                return explicitResource;
            }
            throw new IllegalStateException("Configured runtime.config.path does not exist: " + explicitPath);
        }

        // 2) 在常见开发目录中依次查找，优先命中“你正在编辑的源码文件”
        for (Path candidate : getDevelopmentCandidatePaths()) {
            FileSystemResource candidateResource = new FileSystemResource(candidate);
            if (candidateResource.exists()) {
                return candidateResource;
            }
        }

        // 3) 最后回退 classpath 内默认文件（用于初始兜底）
        ClassPathResource classPathResource = new ClassPathResource(RUNTIME_CONFIG_NAME);
        if (classPathResource.exists()) {
            // 兜底读 classpath：保证首次启动时项目自带默认配置可用。
            return classPathResource;
        }
        // 两处都找不到时直接失败，防止系统在“无配置”状态继续运行。
        throw new IllegalStateException("runtime-config.yml not found in working directory or classpath.");
    }
    /**
     * 校验配置对象，确保运行时切换不会引入非法值。
     *
     * @param runtimeConfig 待校验配置
     * @throws IllegalStateException 当校验失败时抛出
     */
    private void validate(RuntimeConfig runtimeConfig) {
        // 防御性检查：YAML 为空或解析失败时，避免后续出现空指针。
        if (runtimeConfig == null) {
            throw new IllegalStateException("Runtime config is empty.");
        }
        // 业务边界检查：分页大小过小/过大会导致功能异常或性能风险。
        if (runtimeConfig.getPostPageSize() < 1 || runtimeConfig.getPostPageSize() > 50) {
            throw new IllegalStateException("postPageSize must be between 1 and 50.");
        }
    }

    /**
     * 描述当前读取到的资源位置，便于排查“改了文件但没生效”的路径问题。
     *
     * @param resource 实际加载的资源
     * @return 可读的资源描述
     */
    private String describeResource(Resource resource) {
        try {
            if (resource instanceof FileSystemResource) {
                File file = resource.getFile();
                return file.getAbsolutePath();
            }
            return resource.getDescription();
        } catch (IOException ex) {
            return resource.getDescription();
        }
    }

    /**
     * 返回最近一次成功加载配置的来源路径。
     *
     * @return 最近成功加载来源
     */
    public String getLastLoadedFrom() {
        return lastLoadedFrom;
    }

    /**
     * 返回当前可用于 WatchService 监听的配置文件路径。
     * <p>
     * 仅当配置来自文件系统时可监听；若当前仅能从 classpath 加载，则返回 empty。
     * </p>
     *
     * @return 可监听的配置文件路径
     */
    public Optional<Path> getWatchableConfigPath() {
        String explicitPath = resolveExplicitRuntimeConfigPath();
        if (explicitPath != null && !explicitPath.trim().isEmpty()) {
            Path path = Paths.get(explicitPath.trim()).toAbsolutePath().normalize();
            if (path.toFile().exists()) {
                return Optional.of(path);
            }
            return Optional.empty();
        }

        for (Path candidate : getDevelopmentCandidatePaths()) {
            Path normalized = candidate.toAbsolutePath().normalize();
            if (normalized.toFile().exists()) {
                return Optional.of(normalized);
            }
        }
        return Optional.empty();
    }

    /**
     * 构建开发环境下常见的 runtime-config.yml 候选路径。
     * <p>
     * 你的工程是多模块仓库，应用可能从仓库根目录、backend 根目录或 blog-server 目录启动，
     * 因此这里列出多种相对路径，避免“明明改了文件却读不到”。
     * </p>
     *
     * @return 待尝试路径列表（按优先级顺序）
     */
    private List<Path> getDevelopmentCandidatePaths() {
        List<Path> candidates = new ArrayList<>();
        String userDir = System.getProperty("user.dir", ".");
        Path cwd = Paths.get(userDir).toAbsolutePath().normalize();

        candidates.add(cwd.resolve(Paths.get("src", "main", "resources", RUNTIME_CONFIG_NAME)));
        candidates.add(cwd.resolve(Paths.get("blog-server", "src", "main", "resources", RUNTIME_CONFIG_NAME)));
        candidates.add(cwd.resolve(Paths.get("AyeezBlog-Backend", "blog-server", "src", "main", "resources", RUNTIME_CONFIG_NAME)));
        candidates.add(cwd.resolve(RUNTIME_CONFIG_NAME));
        return candidates;
    }

    /**
     * 解析显式配置路径，优先 JVM 参数，次选环境变量。
     *
     * @return 显式配置路径，若未设置则返回 null
     */
    private String resolveExplicitRuntimeConfigPath() {
        String fromJvmProperty = System.getProperty("runtime.config.path");
        if (fromJvmProperty != null && !fromJvmProperty.trim().isEmpty()) {
            return fromJvmProperty.trim();
        }
        String fromEnv = System.getenv("RUNTIME_CONFIG_PATH");
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            return fromEnv.trim();
        }
        return null;
    }
}
