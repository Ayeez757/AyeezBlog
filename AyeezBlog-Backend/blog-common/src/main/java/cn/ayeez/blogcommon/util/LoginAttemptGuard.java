package cn.ayeez.blogcommon.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录防暴力破解守卫（进程内）。
 * 规则：10分钟窗口内连续失败5次，锁定15分钟。
 */
public final class LoginAttemptGuard {

    private static final int MAX_FAILURES = 5;
    private static final long WINDOW_MS = 10 * 60 * 1000L;
    private static final long LOCK_MS = 15 * 60 * 1000L;
    private static final Map<String, AttemptState> ATTEMPTS = new ConcurrentHashMap<>();

    private LoginAttemptGuard() {
    }

    public static LockStatus getLockStatus(String key) {
        AttemptState state = ATTEMPTS.get(key);
        if (state == null) {
            return new LockStatus(false, 0);
        }
        synchronized (state) {
            long now = System.currentTimeMillis();
            if (state.lockUntilMs > now) {
                long seconds = Math.max(1L, (state.lockUntilMs - now + 999) / 1000);
                return new LockStatus(true, seconds);
            }
            if (state.lockUntilMs > 0 && state.lockUntilMs <= now && state.failureCount == 0) {
                ATTEMPTS.remove(key, state);
            }
            return new LockStatus(false, 0);
        }
    }

    public static FailureResult recordFailure(String key) {
        AttemptState state = ATTEMPTS.computeIfAbsent(key, ignored -> new AttemptState());
        synchronized (state) {
            long now = System.currentTimeMillis();
            if (state.windowStartMs == 0 || now - state.windowStartMs > WINDOW_MS) {
                state.windowStartMs = now;
                state.failureCount = 0;
                state.lockUntilMs = 0;
            }
            state.failureCount++;
            int remaining = Math.max(0, MAX_FAILURES - state.failureCount);
            if (state.failureCount >= MAX_FAILURES) {
                state.lockUntilMs = now + LOCK_MS;
                state.failureCount = 0;
                state.windowStartMs = 0;
                return new FailureResult(true, remaining, LOCK_MS / 1000);
            }
            return new FailureResult(false, remaining, 0);
        }
    }

    public static void recordSuccess(String key) {
        ATTEMPTS.remove(key);
    }

    public record LockStatus(boolean locked, long lockRemainingSeconds) {
    }

    public record FailureResult(boolean lockedNow, int remainingAttempts, long lockSeconds) {
    }

    private static class AttemptState {
        private int failureCount;
        private long windowStartMs;
        private long lockUntilMs;
    }
}
