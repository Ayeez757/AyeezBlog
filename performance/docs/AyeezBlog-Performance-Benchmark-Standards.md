# AyeezBlog Performance Benchmark Standards (Local Load-Test Environment)

This document defines performance metrics, grading thresholds, and acceptance criteria for the **local Docker load-test environment**.

## Scope and Preconditions

- Environment: local Docker load testing only.
- Entry: `http://localhost:8080`, no `/api` prefix (`-Jbase_path=""`).
- Workload model: consistent with `performance/jmeter/ayeezblog-performance-test.jmx`.
- Duration:
  - baseline >= 10 minutes
  - capacity/regression >= 15 minutes
  - soak >= 60 minutes (optional)
- Data mode: read-only by default; write scenarios must be benchmarked separately.

Current baseline resource constraints:

- backend container: `cpus=2.0`
- backend memory: `mem_limit=4g` (recommended JVM `-Xms3g -Xmx3g`)
- database: Docker MySQL in same compose network

## Unified Definitions

- **Concurrent users**: JMeter `threads`.
- **Per-user request rate**: `Total Throughput / threads`.
- **Capacity under SLO**: highest concurrency level that passes thresholds for a given model and duration.
- **Spike**: short burst capacity; not equal to steady-state capacity.

## Scenario Groups

- **G1 Local Read-Only**: `/post/list`, `/post/get`, `/about/*`, `/links/list`, `/album/*`
- **G2 Admin Read**: login/dashboard/admin list APIs
- **G3 Write/Tracking**: POST/PUT/DELETE and tracking APIs

## Grading (S / A / B)

### G1 Read-Only (Core)

| Grade | Global Error Rate | G1 P95 | G1 P99 | Meaning |
| --- | --- | --- | --- | --- |
| **S** | <= 0.1% | <= 200 ms | <= 800 ms | high-quality target |
| **A** | <= 0.5% | <= 300 ms | <= 1500 ms | default production target |
| **B** | <= 2.0% | <= 800 ms | <= 3000 ms | acceptable but should be optimized |
| Fail | worse than B on any line | — | — | cannot be used as release baseline |

### G2 Admin Read

| Grade | Error Rate | G2 P95 |
| --- | --- | --- |
| S | <= 0.5% | <= 500 ms |
| A | <= 1.0% | <= 800 ms |
| B | <= 2.0% | <= 1500 ms |

## Resource Risk Lines (Recommended Observability)

- App CPU avg > 85% for 5+ minutes
- Heap old generation persistently high (>85%) or frequent Full GC
- MySQL connection-pool waiting queue continuously non-empty
- Gateway 499/502/504 ratio rising

## Reporting Requirements

Every report should include:

- exact test command and parameter set,
- workload composition and duration,
- Total + per-API percentile results,
- environment constraints and generator location,
- pass/fail rationale by grade.

## Related Documents

- English analysis report: `performance/docs/AyeezBlog-Performance-Analysis-Report.md`
- English notes: `performance/docs/AyeezBlog-Load-Testing-Notes.md`
- Chinese standard (full details): `performance/docs/AyeezBlog-性能指标标准.md`
