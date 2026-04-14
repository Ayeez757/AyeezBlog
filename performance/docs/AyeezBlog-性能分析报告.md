# AyeezBlog 性能分析报告（JMeter）

## 1. 报告目标

本报告用于对 `AyeezBlog` 后端核心接口进行系统化性能评估，覆盖：

- 公网高频读取链路（文章列表、详情、相册、友链、关于页）
- 访客埋点链路（`/post/stats/track`）
- 管理端看板链路（登录、仪表盘、日志列表、文章列表）

最终目标：

- 评估当前系统在典型业务负载下的吞吐能力与稳定性边界
- 提前识别数据库、应用线程池、网络 IO 等潜在瓶颈
- 形成可复用的基线报告与扩容/优化依据

---

## 2. 测试资产与目录

- JMeter 脚本：`performance/jmeter/ayeezblog-performance-test.jmx`
- 报告文档：`performance/docs/AyeezBlog-性能分析报告.md`
- 结果文件（执行后生成）：`performance/jmeter/results/ayeezblog-perf.jtl`
- HTML 可视化报告（执行后生成）：`performance/jmeter/reports/html`

---

## 3. 测试方法论（专业实践）

本次方案采用“基线 -> 压力 -> 诊断 -> 优化回归”的工程化路径：

1. **基线测试（Baseline）**
  在可控并发下运行 15 分钟，观察响应时间分布、错误率和资源占用。
2. **容量验证（Load）**
  逐步提高并发，定位吞吐拐点（TPS 不再线性增长、P95 快速恶化处）。
3. **稳定性测试（Soak）**
  中高并发持续 1~2 小时，验证连接池、线程池、缓存和数据库的长期稳定性。
4. **突刺测试（Spike）**
  短时间骤增并发，验证系统弹性、限流与恢复能力。

---

## 4. 测试模型设计

## 4.1 线程组与业务映射

### TG01-Public-ReadAndTrack-Baseline（开启）

- 并发用户：80
- Ramp-Up：120 秒
- 持续时长：900 秒（可调）
- 核心请求：
  - `GET /post/list`
  - `GET /post/get`
  - `GET /about/anime/list`
  - `GET /links/list`
  - `GET /album/list`
  - `POST /post/stats/track`

说明：该线程组模拟真实前台用户浏览和埋点上报，是最重要的公网负载模型。

### TG02-Admin-Dashboard-Baseline（开启）

- 并发用户：20
- Ramp-Up：60 秒
- 持续时长：900 秒
- 核心请求：
  - `POST /admin/login`（每虚拟用户仅登录一次）
  - `GET /admin/stats/dashboard`
  - `GET /admin/post/list`
  - `GET /admin/logs/list`

说明：覆盖管理端运营场景，关注接口聚合查询和看板峰值响应。

### TG03-Admin-Write-Scenario-DisabledByDefault（默认关闭）

- 用于后续扩展写操作压测（避免误写生产环境数据）
- 建议仅在预发布/压测专用环境开启

## 4.2 专业压测配置点

- 统一 HTTP Defaults（超时、主机、端口）
- Header/Cookie/Cache 管理器
- Uniform Random Timer（模拟人类停顿）
- JSON 提取器（从列表中提取文章 ID，用于详情链路）
- 响应断言（校验 `code=200`，避免“假成功”）

---

## 5. 执行步骤

## 5.1 前置准备

1. 启动被测服务（默认 `http://127.0.0.1:8080`）
2. 准备压测专用账号（管理端）
3. 确认数据库、Redis（若有）、MQ（若有）等依赖稳定
4. 清理旧结果目录（避免报告混杂）

## 5.2 命令行执行（推荐）

在项目根目录执行：

```bash
jmeter -n -t performance/jmeter/ayeezblog-performance-test.jmx ^
  -Jprotocol=http ^
  -Jhost=127.0.0.1 ^
  -Jport=8080 ^
  -Jadmin_username=admin ^
  -Jadmin_password=你的密码 ^
  -Jramp_up_sec=120 ^
  -Jhold_sec=900 ^
  -l performance/jmeter/results/ayeezblog-perf.jtl ^
  -e -o performance/jmeter/reports/html
```

> Windows PowerShell 可将 `^` 改为反引号 ```，或写成单行命令。

## 5.3 生产环境零污染执行规范（必读）

当前 `jmx` 已调整为默认零污染模式：

- `TG01-Public-ReadOnly-Baseline`：仅保留只读 `GET` 请求
- `Track /post/stats/track`：默认 `enabled=false`（不会增加 PV/UV）
- `TG02-Admin-Dashboard-Baseline`：默认 `enabled=false`（避免登录链路产生业务副作用）
- `TG03-Admin-Write-Scenario-DisabledByDefault`：默认 `enabled=false`

生产压测时请保持以上默认状态，不要启用任何 `POST/PUT/DELETE` 采样器或线程组。

## 5.4 图表可视化（更直观）

统一使用 **JMeter 官方 HTML Dashboard** 查看图表：

1. 执行压测时增加参数：
  - `-e -o performance/jmeter/reports/html`
2. 压测结束后打开：
  - `performance/jmeter/reports/html/index.html`

官方 Dashboard 可直接查看：

- 请求吞吐趋势（Throughput）
- 响应时间分布与百分位（Percentiles）
- 错误率与错误明细
- 各接口统计对比

---

## 6. 指标口径与验收标准

## 6.1 核心指标

- 吞吐量：TPS / RPS
- 时延：Avg / P90 / P95 / P99
- 错误率：HTTP 非 2xx + 业务断言失败
- 资源：CPU、Heap、GC、线程数、数据库连接池使用率

## 6.2 建议 SLA（可按业务调整）

- 接口错误率 `< 0.5%`
- 核心读接口 P95 `< 300ms`
- 管理端看板接口 P95 `< 800ms`
- 在目标并发下 15 分钟内无持续性抖动与雪崩

---

## 7. 测试结果记录（服务器只读基线）

> 数据来源：  
>
> - 原始结果：`performance/jmeter/results/server-full-readonly.jtl`  
> - 官方图表：`performance/jmeter/reports/server-full-readonly/index.html`

### 7.1 本轮压测配置

- 目标地址：`https://blog.ayeez.cn:443/api`
- 负载模型：`TG01-Public-ReadOnly-Baseline`
- 配置参数：`threads=80`，`ramp_up_sec=120`，`hold_sec=900`
- 数据保护：`track` 写入关闭、管理端线程组关闭、写场景线程组关闭

### 7.2 全局结果


| 指标     | 数值                  |
| ------ | ------------------- |
| 测试开始时间 | 2026-04-15 02:07:57 |
| 测试结束时间 | 2026-04-15 02:22:56 |
| 总时长    | 898.91 s            |
| 总请求数   | 50,515              |
| 成功请求数  | 49,323              |
| 失败请求数  | 1,192               |
| 全局错误率  | 2.36%               |
| 全局吞吐量  | 56.20 req/s         |
| 平均响应时间 | 377.91 ms           |
| P50    | 142 ms              |
| P90    | 310.6 ms            |
| P95    | 1169 ms             |
| P99    | 5002 ms             |
| 最大响应时间 | 38329 ms            |


### 7.3 接口维度结果（只读公网链路）


| 接口                      | 请求数    | 错误率   | 平均(ms) | P95(ms) | P99(ms) | 最大(ms) | 吞吐(req/s) |
| ----------------------- | ------ | ----- | ------ | ------- | ------- | ------ | --------- |
| `GET /post/list`        | 10,133 | 2.24% | 365.03 | 1160.0  | 5002.0  | 19329  | 11.27     |
| `GET /post/get`         | 10,119 | 2.35% | 378.03 | 1195.6  | 5002.0  | 19319  | 11.29     |
| `GET /about/anime/list` | 10,103 | 2.30% | 368.50 | 1163.9  | 5002.0  | 5021   | 11.28     |
| `GET /links/list`       | 10,087 | 2.56% | 396.63 | 1529.9  | 5002.0  | 38329  | 11.27     |
| `GET /album/list`       | 10,073 | 2.35% | 381.43 | 1167.4  | 5002.0  | 8182   | 11.27     |


### 7.4 错误分布与根因判断

主要错误类型为连接建立超时，且在所有只读接口上均有分布：

- `ConnectTimeoutException: Connect to blog.ayeez.cn:443 ... failed: Connect timed out`（主导错误）
- 少量 `SocketTimeoutException: Read timed out`

根因倾向：

1. **网络/连接层瓶颈优先级最高**：出现大量连接超时，且接口无明显业务偏向。
2. **网关或上游连接容量限制**：可能是 Nginx 上游连接、系统 `somaxconn`、连接队列或带宽抖动。
3. **次级风险是应用慢请求拖尾**：P99 固定在 5s 附近、最大值达到 38s，说明在高峰时存在严重长尾。

---

## 8. SLA 对比结论

按本报告约定 SLA：

- 错误率 `< 0.5%`：**未达标**（当前 2.36%）
- 核心读接口 P95 `< 300ms`：**未达标**（当前约 1.16s~1.53s）
- 稳定性（15 分钟无持续性抖动）：**未达标**（存在明显超时与长尾）

结论：当前系统在该公网负载配置下可运行，但**可靠性和尾延迟均不满足生产目标**。

---

## 9. 优化建议（按优先级）

1. **P0：网络与网关容量排查**
  - 排查服务器安全组、防火墙、Nginx `worker_connections`、`keepalive`、上游连接池设置。
  - 查看 Nginx `access/error` 中 499/502/504 占比和峰值时段。
  - 检查主机 TCP 参数（连接队列、端口复用、TIME_WAIT 累积）。
2. **P0：应用端连接与线程池校准**
  - 校准 Tomcat/Undertow 线程池、连接超时、队列深度。
  - 结合 JVM GC 日志确认是否存在停顿放大（特别是长尾窗口）。
3. **P1：数据库慢查询治理**
  - 将慢查询阈值临时设为 1s，抓取 TopN SQL 与执行计划。
  - 优先优化 `list/get` 高频链路索引与分页策略。
4. **P1：读链路缓存与降载**
  - 对 `post/list`、`post/get`、`links/list` 引入短 TTL 缓存。
  - 对热点文章详情增加二级缓存，降低数据库突发压力。
5. **P2：压测脚本与场景分层**
  - 保持只读基线脚本用于容量评估。
  - 将管理端和写场景单独分批测试，避免混合负载掩盖瓶颈。

---

## 10. 下一轮回归计划（可执行）

### 10.1 阶梯压测

按 4 轮执行，每轮 15 分钟：

1. `threads=20`
2. `threads=40`
3. `threads=60`
4. `threads=80`

每轮都输出独立 `jtl` 与官方 HTML 报告（`-e -o`）。

### 10.2 达标判定

- 错误率稳定 `<0.5%`
- 各核心接口 P95 `<300ms`
- 全局 P99 明显低于 5s 且无长尾峰值

### 10.3 回归后产出

- 更新本报告第 7~10 章
- 附每轮官方 Dashboard 截图（APDEX、响应分布、Errors、Over Time）
- 给出“最大可承载并发（在 SLA 内）”结论

