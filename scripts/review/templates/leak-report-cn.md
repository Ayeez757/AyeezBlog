# Level2 资源泄露信号报告

- 风险等级：**{{RISK_LEVEL}}**
- 进程 PID：{{PID}}
- 采样次数：{{SAMPLES}}
- 采样间隔（秒）：{{INTERVAL_SECONDS}}
- 预热切换轮次：{{EXERCISE_ROUNDS}}
- 回退后静默观察（秒）：{{POST_REVERT_QUIET_SECONDS}}
- 线程变化量（进程）：{{THREAD_DELTA}}
- 私有内存变化量（MB）：{{PRIVATE_DELTA}}
- URLClassLoader 变化量：{{URL_CLASSLOADER_DELTA}}
- 心跳线程峰值：{{HEARTBEAT_THREAD_PEAK}}
- 回退后心跳线程数：{{HEARTBEAT_THREAD_AFTER_REVERT}}
- 预热阶段心跳日志增量（bytes）：{{HEARTBEAT_GROWTH_DURING_EXERCISE}}
- 回退后心跳日志增量（bytes）：{{HEARTBEAT_GROWTH_AFTER_REVERT}}
- JAR 删除检查：{{JAR_DELETE_CHECK}}
- 原始数据（JSON）：scripts/review/output/leak-signals.json

## 风险项

{{RISK_ITEMS}}

## 采样明细

| 序号 | 时间 | 进程线程数 | JCMD 线程数 | 心跳线程数 | URLClassLoader | WorkingSet MB | Private MB |
| --- | --- | --- | --- | --- | --- | --- | --- |
{{SAMPLES_TABLE}}
