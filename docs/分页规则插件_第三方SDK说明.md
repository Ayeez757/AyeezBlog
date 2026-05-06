# 分页规则插件 · 第三方 SDK 说明

本文面向在 **AyeezBlog 后端** 上开发 **外置分页规则 jar** 的第三方开发者，说明插件契约、可见 API、打包元信息与部署约定。与业务热重载评审步骤相关的操作仍请以 [配置文件热重载考核交付文档](./配置文件热重载考核交付文档.md) 第 7 节为准。

---

## 1. 能力范围

- 宿主在运行时通过 **独立 ClassLoader** 加载你的 jar，将「每页条数如何确定」委托给当前生效插件。
- 本说明 **不是** 通用开放 HTTP API 文档；调试用的运行时查询接口见交付文档（如配置、当前插件状态、切换历史等路径）。

---

## 2. 必须实现的接口

**类型**：`cn.ayeez.blogserver.runtime.plugin.RulePlugin<PageSizeRuleInput, PageSizeRuleOutput>`

| 方法 | 说明 |
| --- | --- |
| `String id()` | 稳定、可读的唯一 ID；与 `plugin.properties` 的 `plugin.id` 宜保持一致。 |
| `void init(PluginContext context)` | 在插件**即将**成为当前生效实现时调用；可保存 `context`、读取配置、登记需平台兜底的清理动作。 |
| `PageSizeRuleOutput execute(PageSizeRuleInput input)` | 每个列表请求会调用；根据输入与当前配置计算**最终** `pageSize`。 |
| `void dispose()` | 插件下线时调用；释放本插件直接持有的句柄、引用。 |

**输入 / 输出 DTO**（包名 `cn.ayeez.blogserver.runtime.plugin.paging`）：

- `PageSizeRuleInput`：`getRequestedPageSize()` — 前端请求中的分页大小（可能为 `null`）。
- `PageSizeRuleOutput`：构造参数为 `(int effectivePageSize, String reason)`；`reason` 会随业务结果返回，便于确认规则是否命中。

宿主加载类后会校验：主类需为 `RulePlugin` 的实例，且泛型实际类型与分页 DTO 匹配（与演示插件相同约定）。

---

## 3. 插件可见的上下文：`PluginContext`

包：`cn.ayeez.blogserver.runtime.plugin`

| 方法 | 用途 |
| --- | --- |
| `RuntimeConfigManager getRuntimeConfigManager()` | 读取当前运行时配置快照（只读使用）。 |
| `boolean isPlatformCleanupEnabled()` | 与配置中 `pluginCleanupEnabled` 一致；为 `false` 时平台可跳过已登记清理（用于对照实验，生产宜为 `true`）。 |
| `void registerCleanupAction(Runnable action)` | **仅允许在 `init` 执行期间**调用。用于登记线程池、定时任务等，在插件切换下线时由平台与 `dispose()` 配合执行。 |

**配置快照** `RuntimeConfig`（通过 `getRuntimeConfigManager().getCurrent()` 获取）常见只读方法：

- `int getPostPageSize()` — 热重载配置中的默认每页条数。
- `boolean isStrictModeEnabled()` — 与演示业务相关的开关。
- `String getPageSizeRulePluginId()` — 配置指定的内置插件 ID（外置接管后的持久行为见交付文档说明）。
- `boolean isPluginCleanupEnabled()` — 是否启用平台统一回收。

不要在插件内修改 `RuntimeConfig` 实例字段；配置变更由宿主重载 `runtime-config.yml` 完成。

---

## 4. 生命周期与并发（摘要）

宿主切换顺序：**初始化新插件 → 切换当前引用 → 下线旧插件**（含登记清理与关闭旧 `URLClassLoader`）。

- `execute` 可能被多线程并发调用：避免在插件内使用非线程安全的可变共享状态，或对共享状态加同步。
- 若在 `init` 中创建线程、调度任务等，应优先通过 `registerCleanupAction` 登记释放逻辑，并在 `dispose` 中释放插件自有引用。

---

## 5. jar 元信息：`plugin.properties`

文件必须位于 jar **根路径**，文件名固定为 **`plugin.properties`**（宿主仅识别此名）。

| 键 | 是否必填 | 说明 |
| --- | --- | --- |
| `plugin.id` | 必填 | 插件 ID。 |
| `plugin.class` | 必填 | 主类全限定名，供自动发现加载。 |
| `plugin.version` | 可选 | 建议填写，便于排查。 |

---

## 6. 编译依赖（与宿主版本对齐）

插件字节码只能依赖宿主暴露的 **插件 API 与 DTO**，不要把 Spring Boot  fat jar 打进插件依赖树作为主 classpath。

**推荐做法（与本仓库 `blog-plugin-demo` 一致）**：

1. 使用与线上（或你要对接的）后端 **相同版本** 的源码树。
2. 在后端目录执行打包，生成 **`blog-server-0.0.1-SNAPSHOT-plain.jar`**（Maven classifier `plain`）。
3. 插件模块编译 classpath 包含上述 plain jar（以及必要时 `blog-server/target/classes`），**插件 jar 产物不要打包容器宿主接口以外的宿主实现类**，以免运行期出现 duplicate class / ClassLoader 类型不一致。

若脱离本仓库单独构建，可将 plain jar 安装到私服或使用 `system` 范围依赖，但必须保证 **版本与运行的 `blog-server` 一致**。

---

## 7. 部署与发现

- 将符合约定的 jar 放入宿主的**分页插件监听目录**；目录解析优先级与验证步骤见 [配置文件热重载考核交付文档](./配置文件热重载考核交付文档.md) **7.5**。
- 加载失败时宿主会保持上一版稳定插件，不中断业务。

---

## 8. 参考实现

本仓库模块 **`AyeezBlog-Backend/blog-plugin-demo`**：

- `DoublePageSizeRulePlugin`：无状态示例（生效大小 ≈ 配置 `postPageSize` 的 2 倍）。
- `StatefulSideEffectPageSizeRulePlugin`：带资源登记与副作用，用于交付文档中的回收对照；第三方一般只需参考其 **init / registerCleanupAction / dispose** 模式。

---

## 9. 版本与坐标（当前仓库）

| 项 | 值 |
| --- | --- |
| GroupId / ArtifactId（宿主） | `cn.ayeez` / `blog-server` |
| 版本 | `0.0.1-SNAPSHOT`（以你检出的 `pom.xml` 为准） |
| 编译用 plain 产物 | `blog-server-0.0.1-SNAPSHOT-plain.jar` |

升级宿主时，请同步用新宿主重新编译插件并回归测试。
