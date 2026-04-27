package cn.ayeez.blogserver.controller.user;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogserver.runtime.RuntimeConfig;
import cn.ayeez.blogserver.runtime.RuntimeConfigLoader;
import cn.ayeez.blogserver.runtime.RuntimeConfigManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RuntimeConfigDemoController 提供配置热重载演示接口。
 * <p>
 * 该控制器面向 Day1 学习与考核演示：
 * <ul>
 *     <li>查询当前生效配置</li>
 *     <li>手动触发重载</li>
 *     <li>通过业务接口观察“配置变化 -> 响应变化”</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/post/runtime")
public class RuntimeConfigDemoController {

    private final RuntimeConfigManager runtimeConfigManager;
    private final RuntimeConfigLoader runtimeConfigLoader;

    /**
     * 通过构造器注入运行时配置管理器。
     *
     * @param runtimeConfigManager 运行时配置管理器
     */
    public RuntimeConfigDemoController(RuntimeConfigManager runtimeConfigManager,
                                       RuntimeConfigLoader runtimeConfigLoader) {
        this.runtimeConfigManager = runtimeConfigManager;
        this.runtimeConfigLoader = runtimeConfigLoader;
    }

    /**
     * 获取当前生效配置快照。
     *
     * @return 当前配置对象
     */
    @GetMapping("/config")
    public Result<RuntimeConfig> getCurrentConfig() {
        return Result.success(runtimeConfigManager.getCurrent());
    }

    /**
     * 手动触发配置重载。
     * <p>
     * 当 runtime-config.yml 合法时返回成功；非法时返回失败，
     * 且服务继续使用旧配置。
     * </p>
     *
     * @return 重载执行结果
     */
    @PostMapping("/reload-config")
    public Result<Map<String, Object>> reloadConfig() {
        boolean success = runtimeConfigManager.reload();
        if (!success) {
            return Result.error(400, "重载失败：配置文件格式或取值非法，已保持旧配置继续服务");
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reloaded", true);
        payload.put("currentConfig", runtimeConfigManager.getCurrent());
        payload.put("loadedFrom", runtimeConfigLoader.getLastLoadedFrom());
        return Result.success(payload);
    }

    /**
     * 兼容浏览器直接访问：GET 方式触发重载，内部复用 POST 逻辑。
     *
     * @return 重载执行结果
     */
    @GetMapping("/reload-config")
    public Result<Map<String, Object>> reloadConfigByGet() {
        return reloadConfig();
    }

    /**
     * 业务演示接口：返回“推荐卡片”列表。
     * <p>
     * 该接口用于验证热重载是否影响业务行为：
     * <ul>
     *     <li>返回条数由 postPageSize 决定</li>
     *     <li>标题前缀是否带 [STRICT] 由 strictModeEnabled 决定</li>
     * </ul>
     * </p>
     *
     * @return 用于演示业务差异的数据
     */
    @GetMapping("/demo-cards")
    public Result<Map<String, Object>> demoCards() {
        RuntimeConfig runtimeConfig = runtimeConfigManager.getCurrent();
        int size = runtimeConfig.getPostPageSize();
        boolean strictModeEnabled = runtimeConfig.isStrictModeEnabled();

        List<String> cards = new ArrayList<>(size);
        String prefix = strictModeEnabled ? "[STRICT] " : "";
        for (int i = 1; i <= size; i++) {
            cards.add(prefix + "热重载演示卡片 #" + i);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("size", cards.size());
        payload.put("strictModeEnabled", strictModeEnabled);
        payload.put("cards", cards);
        return Result.success(payload);
    }
}
