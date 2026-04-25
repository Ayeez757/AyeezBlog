# AyeezBlog Load Testing Notes (Local Docker Environment)

This note distills reusable practical lessons from recent local Docker + JMeter tests.

## 1) Define boundary before testing

- Keep backend resource constraints fixed (`cpus=2.0`, `mem_limit=4g`) for comparable results.
- Use local entrypoint `http://localhost:8080` and `-Jbase_path=""`.
- Keep read-only tests clean (`write_threads=0`, admin/write thread groups disabled).
- If environment changes, recalibrate from scratch.

## 2) Command and tooling hygiene

- Ensure `-J` parameters are correctly parsed in PowerShell.
- In JMeter test plan/defaults, read all runtime values via `${__P(name,default)}`.
- Keep `-e -o` output directory empty/non-existent per run.
- Archive commands and result summary even if large artifacts are not committed.

## 3) Do not mix quick probe and final conclusion

- Quick scan: `hold_sec=180~300s` (for range finding).
- Final capacity decision: `hold_sec=600~900s`.
- Always tag which type produced the conclusion.

## 4) Read reports at two levels

- Total-level: error rate, P95/P99, throughput.
- Per-API level: each G1 endpoint must pass thresholds.
- Never use Total pass alone as strict S-tier pass.

## 5) Typical failure patterns

- **Generator-side issue**: `BindException` in `.jtl` usually indicates client-side socket/port limitation.
- **Service-side degradation**: zero errors but P95/P99 exceeds threshold.
- For strict S-tier, use per-API thresholds as final authority.

## 6) Efficient test scheduling

- Smoke: 5~10 threads.
- Range probing: 20 -> 40 -> 80 -> 160.
- Boundary convergence: binary-style narrowing near suspected limit.
- If generator bottleneck appears, optimize generator side or use distributed load.

## 7) Post-run checklist

- Confirm command inputs are fully documented.
- Confirm test type (quick vs final) is explicitly labeled.
- Confirm grade is based on Total + each G1 API.
- Confirm environment and generator location are stated.

## Related Documents

- English standards: `performance/docs/AyeezBlog-Performance-Benchmark-Standards.md`
- English analysis report: `performance/docs/AyeezBlog-Performance-Analysis-Report.md`
- Chinese notes (full details): `performance/docs/AyeezBlog-压测经验笔记.md`
