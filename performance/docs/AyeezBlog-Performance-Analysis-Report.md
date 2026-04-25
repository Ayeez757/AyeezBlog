# AyeezBlog Performance Analysis Report (Local Docker Fixed Environment + JMeter)

## Executive Summary

- Under the fixed local setup (backend `2C4G`) and current read-only workload model, the converged **strict S-tier** maximum concurrency is **278**.
- `279` still passes at Total level, but fails strict S-tier because `Public /post/list` reaches `P95=204ms` (> `200ms`).
- This conclusion represents **steady-state capacity** (`hold_sec=900`), not burst/spike peak.
- External communication recommendation: around **250 active concurrent users** (S-tier quality target).

## Scope and Goal

This report evaluates the **G1 read-only APIs** in a fixed local environment, with goals to:

- identify the concurrency range that satisfies S-tier thresholds,
- distinguish server bottlenecks from load-generator bottlenecks,
- provide reproducible commands and archival references.

## Test Assets

- Script: `performance/jmeter/ayeezblog-performance-test.jmx`
- Standard: `performance/docs/AyeezBlog-Performance-Benchmark-Standards.md`
- This report: `performance/docs/AyeezBlog-Performance-Analysis-Report.md`
- Chinese report (full details): `performance/docs/AyeezBlog-性能分析报告.md`

## Fixed Environment

- Backend (local Docker): `cpus=2.0`, `mem_limit=4g`, recommended JVM `-Xms3g -Xmx3g`
- Database: MySQL in the same compose network (`HM_DB_HOST=mysql`)
- Load generator: Windows + JMeter on local machine
- Risk note: high concurrency can hit load-generator port/TIME_WAIT limits (`java.net.BindException: Address already in use`)

## Workload and S-tier Criteria

- Read-only TG only (`TG01`)
- APIs covered:
  - `GET /post/list`
  - `GET /post/get`
  - `GET /about/anime/list`
  - `GET /links/list`
  - `GET /album/list`
- Constraints:
  - `-Jwrite_threads=0`
  - admin/write thread groups disabled
- S-tier pass criteria:
  - global error rate `<= 0.1%`
  - for **every G1 API**: `P95 <= 200ms` and `P99 <= 800ms`

## Reproducible Command Template

```powershell
jmeter -n -t "performance/jmeter/ayeezblog-performance-test.jmx" `
  -Jprotocol=http -Jhost=localhost -Jport=8080 -Jbase_path="" `
  -Jthink_time_ms=200 `
  -Jramp_up_sec=180 -Jhold_sec=900 `
  -Jwrite_threads=0 `
  -Jthreads=<N> `
  -l "performance/jmeter/results/<run>.jtl" `
  -e -o "performance/jmeter/reports/<run>"
```

## Effective Results (Current Round)

| threads | Strict S-tier | Notes |
| ---: | --- | --- |
| 80 ~ 278 | Pass | Meets global + per-API S-tier targets |
| 279 | Fail | `Public /post/list` `P95=204ms` exceeds threshold |
| 280 | Fail | Total `P95=243ms` exceeds threshold |

Final strict S-tier conclusion in current model:

- **Maximum pass concurrency = 278**
- Suggested external value with safety margin: **250**

## Interpretation Notes

- This is a local, steady-state, read-only capacity result.
- Do not use this number for write-heavy/admin-heavy workloads without separate tests.
- If load-generator-side `BindException` appears, resolve client-side limitations before interpreting server-side limits.
