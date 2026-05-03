package cn.ayeez.blogserver.controller.user;

import cn.ayeez.blogcommon.util.Result;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 评审用 JVM 侧信号快照（替代宿主机 jcmd），仅在 {@code review} 环境注册。
 * <p>
 * 供 {@code check-level2-leak-signals.sh --mode http} 使用：评审机只需 curl + jq，无需安装 JDK。
 * </p>
 */
@RestController
@RequestMapping("/post/runtime")
@Profile("review")
public class ReviewLeakDiagnosticsController {

    private static final String HEARTBEAT_NAME_MARK = "stateful-side-effect-heartbeat-";

    @GetMapping("/review-leak-signals-snapshot")
    public Result<Map<String, Object>> leakSignalsSnapshot() {
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        int live = threads.getThreadCount();

        long[] ids = threads.getAllThreadIds();
        int heartbeatNamed = 0;
        for (long id : ids) {
            ThreadInfo info = threads.getThreadInfo(id);
            if (info == null) {
                continue;
            }
            String name = info.getThreadName();
            if (name != null && name.contains(HEARTBEAT_NAME_MARK)) {
                heartbeatNamed++;
            }
        }

        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memory.getHeapMemoryUsage();
        long used = heap.getUsed();
        long committed = heap.getCommitted();
        long max = heap.getMax();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("at", Instant.now().toString());
        payload.put("processThreads", live);
        payload.put("jcmdThreads", live);
        payload.put("heartbeatThreads", heartbeatNamed);
        payload.put("urlClassLoaders", null);
        payload.put("workingSetMB", round2Mb(used));
        payload.put("privateMB", round2Mb(committed));
        payload.put("heapInfoSnippet", String.format(
                "heap used %d MB / committed %d MB / max %s MB",
                used / (1024 * 1024),
                committed / (1024 * 1024),
                max > 0 ? Long.toString(max / (1024 * 1024)) : "n/a"));

        return Result.success(payload);
    }

    private static double round2Mb(long bytes) {
        return Math.round(bytes / (1024.0 * 1024.0) * 100d) / 100d;
    }
}
