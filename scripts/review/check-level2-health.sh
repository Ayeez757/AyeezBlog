#!/usr/bin/env bash
# 对 results.json 做健康汇总（与 check-level2-health.ps1 对齐）
# 依赖：jq

set -euo pipefail

if ! command -v jq >/dev/null 2>&1; then
  echo "需要 jq，请先安装。" >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
RESULT_PATH="${RESULT_PATH:-$SCRIPT_DIR/output/results.json}"

while [ $# -gt 0 ]; do
  case "$1" in
    --result-path) RESULT_PATH="$2"; shift 2 ;;
    -h|--help)
      echo "用法: $0 [--result-path PATH]"
      exit 0
      ;;
    *) echo "未知参数: $1" >&2; exit 1 ;;
  esac
done

if [ ! -f "$RESULT_PATH" ]; then
  echo "结果文件不存在：$RESULT_PATH。请先执行 run-level2-auto-watch.sh" >&2
  exit 1
fi

if ! jq -e 'length > 0' "$RESULT_PATH" >/dev/null 2>&1; then
  echo "结果文件为空或无效：$RESULT_PATH" >&2
  exit 1
fi

total=$(jq 'length' "$RESULT_PATH")
pass=$(jq '[.[] | select(.success == true)] | length' "$RESULT_PATH")
fail=$((total - pass))
origin_fail=$(jq '[.[] | select((.origin // "") != "external")] | length' "$RESULT_PATH")
source_fail=$(jq '[.[] | select((.latestHistorySource // "") != "auto-watch")] | length' "$RESULT_PATH")
business_fail=$(jq '[.[] | select((.businessApi // "") != "ok")] | length' "$RESULT_PATH")

if [ "$fail" -eq 0 ]; then status="PASS"; else status="FAIL"; fi

echo "===== Level2 Health Check ====="
echo "Result Path     : $RESULT_PATH"
echo "Total Rounds    : $total"
echo "Pass            : $pass"
echo "Fail            : $fail"
echo "Origin Mismatch : $origin_fail"
echo "Source Mismatch : $source_fail"
echo "Business Failed : $business_fail"
echo "Final Status    : $status"
echo "==============================="

if [ "$fail" -gt 0 ]; then
  echo ""
  echo "Failed rounds:"
  jq -r '.[] | select(.success != true) | "- round=\(.round) message=\(.message)"' "$RESULT_PATH"
fi
