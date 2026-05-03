#!/usr/bin/env bash
# Level2 自动监听热加载循环（与 run-level2-auto-watch.ps1 行为对齐）
# 依赖：curl、jq（https://jqlang.github.io/jq/download/）

set -euo pipefail

if ! command -v jq >/dev/null 2>&1; then
  echo "需要 jq 处理 JSON，请先安装 jq 后再运行本脚本。" >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

BASE_URL="${BASE_URL:-http://localhost:8080}"
BUSINESS_VERIFY_PATH="${BUSINESS_VERIFY_PATH:-/post/list?page=1&pageSize=10}"
PLUGIN_DIR_ARG="${PLUGIN_DIR:-}"
ROUNDS="${ROUNDS:-50}"
WAIT_SECONDS="${WAIT_SECONDS:-2}"
SWITCH_WAIT_TIMEOUT_SECONDS="${SWITCH_WAIT_TIMEOUT_SECONDS:-8}"

usage() {
  echo "用法: $0 [--base-url URL] [--business-path PATH] [--plugin-dir DIR] [--rounds N] [--wait-seconds N] [--switch-timeout N]"
  echo "环境变量: BASE_URL, BUSINESS_VERIFY_PATH, PLUGIN_DIR, PLUGIN_SOURCE_JAR（可选，显式指定源 jar）,"
  echo "           ROUNDS, WAIT_SECONDS, SWITCH_WAIT_TIMEOUT_SECONDS"
  exit 0
}

while [ $# -gt 0 ]; do
  case "$1" in
    --base-url) BASE_URL="$2"; shift 2 ;;
    --business-path) BUSINESS_VERIFY_PATH="$2"; shift 2 ;;
    --plugin-dir) PLUGIN_DIR_ARG="$2"; shift 2 ;;
    --rounds) ROUNDS="$2"; shift 2 ;;
    --wait-seconds) WAIT_SECONDS="$2"; shift 2 ;;
    --switch-timeout) SWITCH_WAIT_TIMEOUT_SECONDS="$2"; shift 2 ;;
    -h|--help) usage ;;
    *) echo "未知参数: $1" >&2; usage ;;
  esac
done

BACKEND_ROOT="$REPO_ROOT/AyeezBlog-Backend"
MAVEN_PLUGIN_JAR="$BACKEND_ROOT/blog-plugin-demo/target/blog-plugin-demo-0.0.1-SNAPSHOT.jar"
BUNDLED_DEMO_DIR="$REPO_ROOT/deploy/review/plugins/bundled-demo"
BUNDLED_DEMO_JAR="$BUNDLED_DEMO_DIR/blog-plugin-demo-0.0.1-SNAPSHOT.jar"
BACKEND_DEFAULT_PLUGIN_DIR="$BACKEND_ROOT/plugins/page-size"
BLOG_SERVER_DEFAULT_PLUGIN_DIR="$BACKEND_ROOT/blog-server/plugins/page-size"
OUTPUT_DIR="$SCRIPT_DIR/output"
TEMPLATE_PATH="$SCRIPT_DIR/templates/report-cn.md"
RESULT_PATH="$OUTPUT_DIR/results.json"
REPORT_PATH="$OUTPUT_DIR/report.md"

resolve_plugin_dir() {
  if [ -n "$PLUGIN_DIR_ARG" ]; then
    if [[ "$PLUGIN_DIR_ARG" = /* ]] || [[ "$PLUGIN_DIR_ARG" =~ ^[A-Za-z]:[\\/] ]]; then
      echo "$PLUGIN_DIR_ARG"
    else
      echo "$REPO_ROOT/${PLUGIN_DIR_ARG//\\//}"
    fi
    return
  fi
  if [ -n "${PAGE_SIZE_PLUGIN_DIR:-}" ]; then
    echo "$PAGE_SIZE_PLUGIN_DIR"
    return
  fi
  if [ -d "$BLOG_SERVER_DEFAULT_PLUGIN_DIR" ]; then
    echo "$BLOG_SERVER_DEFAULT_PLUGIN_DIR"
    return
  fi
  if [ -d "$BACKEND_DEFAULT_PLUGIN_DIR" ]; then
    echo "$BACKEND_DEFAULT_PLUGIN_DIR"
    return
  fi
  echo "$BLOG_SERVER_DEFAULT_PLUGIN_DIR"
}

PLUGIN_DIR="$(resolve_plugin_dir)"
if [ -d "$PLUGIN_DIR" ]; then
  PLUGIN_DIR="$(cd "$PLUGIN_DIR" && pwd)"
fi

# 源 jar：显式 PLUGIN_SOURCE_JAR 环境变量 >（Maven 与 bundled-demo 并存时取修改时间较新者）> 单一存在方 > bundled-demo 目录内任意 .jar
resolve_plugin_source_jar() {
  if [ -n "${PLUGIN_SOURCE_JAR:-}" ] && [ -f "$PLUGIN_SOURCE_JAR" ]; then
    echo "$(cd "$(dirname "$PLUGIN_SOURCE_JAR")" && pwd)/$(basename "$PLUGIN_SOURCE_JAR")"
    return
  fi
  if [ -f "$MAVEN_PLUGIN_JAR" ] && [ -f "$BUNDLED_DEMO_JAR" ]; then
    if [ "$BUNDLED_DEMO_JAR" -nt "$MAVEN_PLUGIN_JAR" ]; then echo "$BUNDLED_DEMO_JAR"; else echo "$MAVEN_PLUGIN_JAR"; fi
    return
  fi
  if [ -f "$MAVEN_PLUGIN_JAR" ]; then echo "$MAVEN_PLUGIN_JAR"; return; fi
  if [ -f "$BUNDLED_DEMO_JAR" ]; then echo "$BUNDLED_DEMO_JAR"; return; fi
  local f
  for f in "$BUNDLED_DEMO_DIR"/*.jar; do
    if [ -f "$f" ]; then echo "$f"; return; fi
  done
  echo ""
}

PLUGIN_SOURCE_JAR="$(resolve_plugin_source_jar)"
if [ -z "$PLUGIN_SOURCE_JAR" ] || [ ! -f "$PLUGIN_SOURCE_JAR" ]; then
  echo "未找到插件 demo jar。任选其一：" >&2
  echo "  1) 已用评审 Docker：先 docker compose up -d，使 entrypoint 写入 ${BUNDLED_DEMO_JAR}（或 bundled-demo 下任意 .jar）" >&2
  echo "  2) 本机构建：cd AyeezBlog-Backend && ./mvnw package -pl blog-server,blog-plugin-demo -am -DskipTests" >&2
  echo "  3) 设置环境变量 PLUGIN_SOURCE_JAR 指向已有 jar 的绝对路径" >&2
  exit 1
fi

mkdir -p "$PLUGIN_DIR"
mkdir -p "$OUTPUT_DIR"

if [ ! -f "$TEMPLATE_PATH" ]; then
  echo "未找到模板: $TEMPLATE_PATH" >&2
  exit 1
fi

if [ -d "$BACKEND_DEFAULT_PLUGIN_DIR" ] && [ -d "$BLOG_SERVER_DEFAULT_PLUGIN_DIR" ] && \
   [ "$PLUGIN_DIR" != "$BACKEND_DEFAULT_PLUGIN_DIR" ] && [ "$PLUGIN_DIR" != "$BLOG_SERVER_DEFAULT_PLUGIN_DIR" ]; then
  echo "警告: 两个默认插件目录均存在，请确认 --plugin-dir 与后端监听目录一致。" >&2
fi

# 将 API 根对象中的 .data 解包（若无 data 则保持原样）
unwrap_data='(if type == "object" and has("data") then .data else . end)'

curl_json_get() {
  curl -sS -m 15 -f "$1" || return 1
}

wait_for_external_auto_watch() {
  local base="$1"
  local timeout="$2"
  local start elapsed
  start=$(date +%s)
  local last_origin="" last_hist=""
  local rule_raw hist_raw
  while true; do
    elapsed=$(($(date +%s) - start))
    if [ "$elapsed" -ge "$timeout" ]; then
      printf '%s\t%s\n' "$last_origin" "$last_hist"
      return 1
    fi
    rule_raw=$(curl_json_get "${base%/}/post/runtime/page-size-rule" 2>/dev/null || true)
    hist_raw=$(curl_json_get "${base%/}/post/runtime/page-size-rule-history?limit=1" 2>/dev/null || true)

    last_origin=$(printf '%s' "$rule_raw" | jq -r "${unwrap_data} | .currentPluginOrigin // empty" 2>/dev/null || echo "")
    last_hist=$(printf '%s' "$hist_raw" | jq -r "${unwrap_data}
      | if type == \"array\" and length > 0 then .[0].source // empty
        elif type == \"object\" and (.list? != null) and (.list|type==\"array\") and (.list|length>0) then .list[0].source // empty
        else empty end" 2>/dev/null || echo "")

    if [ "$last_origin" = "external" ] && [ "$last_hist" = "auto-watch" ]; then
      printf '%s\t%s\n' "$last_origin" "$last_hist"
      return 0
    fi
    sleep 0.5
  done
}

echo "Starting Level2 auto-watch loop test..."
echo "BaseUrl=$BASE_URL Rounds=$ROUNDS PluginDir=$PLUGIN_DIR"
echo "PluginSourceJar=$PLUGIN_SOURCE_JAR"
echo "BusinessVerifyPath=$BUSINESS_VERIFY_PATH"

TMP_RESULTS="$(mktemp)"
trap 'rm -f "$TMP_RESULTS"' EXIT

for ((i = 1; i <= ROUNDS; i++)); do
  printf -v round_tag '%03d' "$i"
  target_jar="$PLUGIN_DIR/review-loop-${round_tag}.jar"
  cp -f "$PLUGIN_SOURCE_JAR" "$target_jar"
  sleep "$WAIT_SECONDS"

  ok=true
  msg="ok"
  origin=""
  history_source=""
  business_status="unknown"

  if read -r origin history_source < <(wait_for_external_auto_watch "$BASE_URL" "$SWITCH_WAIT_TIMEOUT_SECONDS"); then
    :
  else
    ok=false
    if [ -z "$history_source" ]; then
      msg="switch timeout: currentPluginOrigin=$origin, latest source=empty"
    else
      msg="switch timeout: currentPluginOrigin=$origin, latest source=$history_source"
    fi
  fi

  burl="${BASE_URL%/}${BUSINESS_VERIFY_PATH}"
  if curl -sS -m 15 -f -o /dev/null "$burl" 2>/dev/null; then
    business_status="ok"
  else
    business_status="failed"
    ok=false
    msg="business api failed"
  fi

  jar_name="$(basename "$target_jar")"
  at_iso="$(date -u +"%Y-%m-%dT%H:%M:%S")"
  succ_json="false"
  if $ok; then succ_json="true"; fi

  jq -nc \
    --argjson round "$i" \
    --arg jar "$jar_name" \
    --argjson success "$succ_json" \
    --arg origin "$origin" \
    --arg history_source "$history_source" \
    --arg business "$business_status" \
    --arg msg "$msg" \
    --arg at "$at_iso" \
    '{round: $round, jar: $jar, success: $success, origin: $origin, latestHistorySource: $history_source, businessApi: $business, message: $msg, at: $at}' \
    >> "$TMP_RESULTS"

  if $ok; then echo "Round $round_tag: PASS - $msg"; else echo "Round $round_tag: FAIL - $msg"; fi
done

jq -s '.' "$TMP_RESULTS" > "$RESULT_PATH"

pass_count=$(jq '[.[] | select(.success == true)] | length' "$RESULT_PATH")
fail_count=$(jq '[.[] | select(.success == false)] | length' "$RESULT_PATH")
total=$(jq 'length' "$RESULT_PATH")
if [ "$fail_count" -eq 0 ]; then status="PASS"; else status="FAIL"; fi

TABLE=$(jq -r '
  [.[] | "| \(.round) | \(.jar) | \(.success) | \(.origin // "") | \(.latestHistorySource // "") | \(.businessApi // "") | \((.message // "") | gsub("\\|"; "\\\\|")) |"]
  | join("\r\n")
' "$RESULT_PATH")

{
  printf '\xEF\xBB\xBF'
  jq --rawfile tpl "$TEMPLATE_PATH" -n \
    --arg st "$status" \
    --arg bu "$BASE_URL" \
    --argjson tr "$total" \
    --argjson pc "$pass_count" \
    --argjson fc "$fail_count" \
    --arg tbl "$TABLE" \
    '($tpl | tostring)
      | gsub("{{STATUS}}"; $st)
      | gsub("{{BASE_URL}}"; $bu)
      | gsub("{{ROUNDS}}"; ($tr | tostring))
      | gsub("{{PASS}}"; ($pc | tostring))
      | gsub("{{FAIL}}"; ($fc | tostring))
      | gsub("{{TABLE}}"; $tbl)'
} > "$REPORT_PATH"

final="$status"
echo ""
echo "Test finished: $final"
echo "Result file: $RESULT_PATH"
echo "Report file: $REPORT_PATH"
