#!/usr/bin/env bash
# 资源/副作用信号检测（与 check-level2-leak-signals.ps1 对齐）
# 依赖：bash、curl、jq
# 模式：
#   host  — 本机 jcmd + 宿主机上的 blog-server 进程（默认）
#   http  — 仅 GET /post/runtime/review-leak-signals-snapshot，无本机 JDK
#   docker— 拉取 JDK 镜像，docker run --pid=container:<后端容器> jcmd …（与 host 同口径，宿主机无需 JDK）

set -euo pipefail

if ! command -v jq >/dev/null 2>&1; then
  echo "需要 jq，请先安装。" >&2
  exit 1
fi
if ! command -v curl >/dev/null 2>&1; then
  echo "需要 curl。" >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

BASE_URL="${BASE_URL:-http://localhost:8080}"
MODE="${MODE:-host}"
PLUGIN_DIR_ARG="${PLUGIN_DIR:-}"
PLUGIN_SOURCE_JAR_ARG="${PLUGIN_SOURCE_JAR:-}"
SAMPLES="${SAMPLES:-5}"
INTERVAL_SECONDS="${INTERVAL_SECONDS:-2}"
EXERCISE_ROUNDS="${EXERCISE_ROUNDS:-10}"
EXERCISE_WAIT_MS="${EXERCISE_WAIT_MS:-1200}"
POST_REVERT_QUIET_SECONDS="${POST_REVERT_QUIET_SECONDS:-8}"
DOCKER_CONTAINER="${DOCKER_CONTAINER:-ayeezblog-review-backend}"
DOCKER_JAVA_PID="${DOCKER_JAVA_PID:-1}"
DOCKER_JDK_IMAGE="${DOCKER_JDK_IMAGE:-eclipse-temurin:21-jdk-jammy}"
DOCKER_PULL="${DOCKER_PULL:-0}"

UNWRAP='(if type == "object" and has("data") then .data else . end)'

while [ $# -gt 0 ]; do
  case "$1" in
    --base-url) BASE_URL="$2"; shift 2 ;;
    --mode) MODE=$(printf '%s' "$2" | tr '[:upper:]' '[:lower:]'); shift 2 ;;
    --docker-container) DOCKER_CONTAINER="$2"; shift 2 ;;
    --docker-java-pid) DOCKER_JAVA_PID="$2"; shift 2 ;;
    --docker-jdk-image) DOCKER_JDK_IMAGE="$2"; shift 2 ;;
    --docker-pull) DOCKER_PULL="1"; shift ;;
    --plugin-dir) PLUGIN_DIR_ARG="$2"; shift 2 ;;
    --plugin-source-jar) PLUGIN_SOURCE_JAR_ARG="$2"; shift 2 ;;
    --samples) SAMPLES="$2"; shift 2 ;;
    --interval-seconds) INTERVAL_SECONDS="$2"; shift 2 ;;
    --exercise-rounds) EXERCISE_ROUNDS="$2"; shift 2 ;;
    --exercise-wait-ms) EXERCISE_WAIT_MS="$2"; shift 2 ;;
    --post-revert-quiet-seconds) POST_REVERT_QUIET_SECONDS="$2"; shift 2 ;;
    -h|--help)
      echo "用法: $0 [--mode host|http|docker] [--base-url URL] [--plugin-dir DIR] [--plugin-source-jar PATH] ..."
      echo "  未指定 --plugin-source-jar 时：优先 Maven target，其次 deploy/review/plugins/bundled-demo 下 .jar（评审 Docker 启动后可用）。"
      echo "  --mode host    默认：本机 jcmd（需宿主机 JDK）"
      echo "  --mode http    仅 curl 调 review 快照接口（无 JDK；指标较窄）"
      echo "  --mode docker  用临时容器跑 JDK 的 jcmd，目标：--docker-container（默认 ayeezblog-review-backend）"
      echo "  --docker-container NAME   与 docker compose 中 backend 的 container_name 一致"
      echo "  --docker-java-pid N       目标 JVM 在容器内 PID，评审镜像 entrypoint 为 exec java 时一般为 1"
      echo "  --docker-jdk-image IMG    默认 eclipse-temurin:21-jdk-jammy（需含 jcmd）"
      echo "  --docker-pull             先执行 docker pull 指定 JDK 镜像"
      exit 0
      ;;
    *) echo "未知参数: $1" >&2; exit 1 ;;
  esac
done

case "$MODE" in
  host|http|docker) ;;
  *) echo "--mode 只能是 host、http 或 docker" >&2; exit 1 ;;
esac

if [ "$MODE" = "host" ] && ! command -v jcmd >/dev/null 2>&1; then
  echo "host 模式需要 jcmd。可改用: bash $0 --mode http ... 或 bash $0 --mode docker ..." >&2
  exit 1
fi

if [ "$MODE" = "docker" ] && ! command -v docker >/dev/null 2>&1; then
  echo "docker 模式需要本机已安装 Docker 且可访问守护进程。" >&2
  exit 1
fi

BACKEND_ROOT="$REPO_ROOT/AyeezBlog-Backend"
BLOG_SERVER_PLUGIN_DIR="$BACKEND_ROOT/blog-server/plugins/page-size"
BACKEND_PLUGIN_DIR="$BACKEND_ROOT/plugins/page-size"
MAVEN_PLUGIN_JAR="$BACKEND_ROOT/blog-plugin-demo/target/blog-plugin-demo-0.0.1-SNAPSHOT.jar"
BUNDLED_DEMO_DIR="$REPO_ROOT/deploy/review/plugins/bundled-demo"
BUNDLED_DEMO_JAR="$BUNDLED_DEMO_DIR/blog-plugin-demo-0.0.1-SNAPSHOT.jar"

# 未传 --plugin-source-jar 时：Maven target > 评审 Docker 挂载 bundled-demo（仅 Docker 的评审员无需本机 package）
resolve_default_plugin_source_jar() {
  if [ -f "$MAVEN_PLUGIN_JAR" ]; then echo "$MAVEN_PLUGIN_JAR"; return; fi
  if [ -f "$BUNDLED_DEMO_JAR" ]; then echo "$BUNDLED_DEMO_JAR"; return; fi
  local f
  for f in "$BUNDLED_DEMO_DIR"/*.jar; do
    if [ -f "$f" ]; then echo "$f"; return; fi
  done
  echo "$MAVEN_PLUGIN_JAR"
}

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
  if [ -d "$BLOG_SERVER_PLUGIN_DIR" ]; then echo "$BLOG_SERVER_PLUGIN_DIR"; return; fi
  if [ -d "$BACKEND_PLUGIN_DIR" ]; then echo "$BACKEND_PLUGIN_DIR"; return; fi
  echo "$BLOG_SERVER_PLUGIN_DIR"
}

PLUGIN_DIR="$(resolve_plugin_dir)"
if [ -d "$PLUGIN_DIR" ]; then PLUGIN_DIR="$(cd "$PLUGIN_DIR" && pwd)"; fi

if [ -n "$PLUGIN_SOURCE_JAR_ARG" ]; then
  if [[ "$PLUGIN_SOURCE_JAR_ARG" = /* ]] || [[ "$PLUGIN_SOURCE_JAR_ARG" =~ ^[A-Za-z]:[\\/] ]]; then
    PLUGIN_SOURCE_JAR="$PLUGIN_SOURCE_JAR_ARG"
  else
    PLUGIN_SOURCE_JAR="$REPO_ROOT/${PLUGIN_SOURCE_JAR_ARG//\\//}"
  fi
else
  PLUGIN_SOURCE_JAR="$(resolve_default_plugin_source_jar)"
fi
OUTPUT_DIR="$SCRIPT_DIR/output"
TEMPLATE_PATH="$SCRIPT_DIR/templates/leak-report-cn.md"
JSON_PATH="$OUTPUT_DIR/leak-signals.json"
REPORT_PATH="$OUTPUT_DIR/leak-signals-report.md"

mkdir -p "$OUTPUT_DIR"
if [ ! -f "$TEMPLATE_PATH" ]; then
  echo "未找到模板: $TEMPLATE_PATH" >&2
  exit 1
fi

strip_ansi() { sed $'s/\x1b\[[0-9;]*[a-zA-Z]//g' 2>/dev/null || cat; }

# jcmd：host 用本机；docker 用临时 JDK 容器并 join 目标容器 PID 命名空间（目标 JVM 一般为 PID 1）
run_jcmd_pid() {
  local pid="$1"
  shift
  if [ "$MODE" = "docker" ]; then
    docker run --rm \
      --pid="container:${DOCKER_CONTAINER}" \
      "${DOCKER_JDK_IMAGE}" \
      jcmd "$pid" "$@"
  else
    jcmd "$pid" "$@"
  fi
}

find_blog_server_java_pid() {
  local pid
  if command -v jps >/dev/null 2>&1; then
    pid=$(jps -l 2>/dev/null | awk '/blog-server/ {print $1; exit}')
    if [ -n "${pid:-}" ]; then echo "$pid"; return; fi
  fi
  if [ -d /proc ]; then
    for p in /proc/[0-9]*; do
      [ -r "$p/cmdline" ] || continue
      if tr '\0' ' ' <"$p/cmdline" 2>/dev/null | grep -q blog-server; then
        echo "${p#/proc/}"
        return
      fi
    done
  fi
  pid=$(ps -Ao pid,args 2>/dev/null | awk '/java/ && /blog-server/ && $1 != "PID" {print $1; exit}')
  [ -n "${pid:-}" ] && echo "$pid"
}

parse_thread_count_jcmd() {
  local text="$1"
  local legacy
  legacy=$(echo "$text" | grep -oE 'thread #[0-9]+' | head -1 | sed -E 's/thread #//')
  if [ -n "$legacy" ] && [ "$legacy" -eq "$legacy" ] 2>/dev/null; then echo "$legacy"; return; fi
  echo "$text" | grep -E '^"[^"]+"[[:space:]]+#[0-9]+' 2>/dev/null | wc -l | tr -d ' '
}

parse_heartbeat_thread_count() {
  echo "$1" | grep -o 'stateful-side-effect-heartbeat-' 2>/dev/null | wc -l | tr -d ' '
}

parse_url_classloader_count() {
  local text
  text=$(echo "$1" | strip_ansi)
  if echo "$text" | grep -qi "Unknown diagnostic command"; then echo ""; return; fi
  local n
  n=$(echo "$text" | grep -Eic '^0x[0-9a-fA-F]+[[:space:]]+0x[0-9a-fA-F]+[[:space:]]+0x[0-9a-fA-F]+[[:space:]]+[0-9]+[[:space:]]+[0-9]+[[:space:]]+[0-9]+[[:space:]].*URLClassLoader[[:space:]]*$' || true)
  if [ "${n:-0}" -gt 0 ] 2>/dev/null; then echo "$n"; return; fi
  n=$(echo "$text" | grep -oiE '(java\.net\.)?URLClassLoader' | wc -l | tr -d ' ')
  if [ "${n:-0}" -gt 0 ] 2>/dev/null; then echo "$n"; return; fi
  if echo "$text" | grep -qEi '^Total[[:space:]]*=[[:space:]]*[0-9]+'; then
    echo "$text" | grep -oiE '^Total[[:space:]]*=[[:space:]]*[0-9]+' | head -1 | grep -oE '[0-9]+'
    return
  fi
  n=$(echo "$text" | grep -Eic '^0x[0-9a-fA-F]+[[:space:]]+0x[0-9a-fA-F]+[[:space:]]+0x[0-9a-fA-F]+[[:space:]]+[0-9]+[[:space:]]+[0-9]+[[:space:]]+[0-9]+[[:space:]].+$' || true)
  if [ "${n:-0}" -gt 0 ] 2>/dev/null; then echo "$n"; return; fi
  echo ""
}

# 将 jcmd 输出写入 $2，在 stdout 打印选用的子命令名
write_classloader_probe() {
  local pid="$1"
  local out_file="$2"
  local cmd
  for cmd in VM.classloader_stats VM.classloaders; do
    run_jcmd_pid "$pid" "$cmd" >"$out_file" 2>&1 || true
    if ! grep -qi "Unknown diagnostic command" "$out_file"; then
      echo "$cmd"
      return 0
    fi
  done
  : >"$out_file"
  echo "unsupported"
}

histogram_url_classloader_count() {
  local pid="$1"
  local histogram line
  histogram=$(run_jcmd_pid "$pid" GC.class_histogram 2>&1 || true)
  if echo "$histogram" | grep -qi "Unknown diagnostic command"; then echo ""; return; fi
  line=$(echo "$histogram" | grep -iE 'java\.net\.URLClassLoader(\s|\(|$)' | head -1)
  if [ -z "$line" ]; then echo ""; return; fi
  echo "$line" | sed -nE 's/^[[:space:]]*[0-9]+:[[:space:]]+([0-9]+)[[:space:]].*/\1/p'
}

proc_mem_mb() {
  local pid="$1"
  if [ "$MODE" = "docker" ]; then
    docker run --rm --pid="container:${DOCKER_CONTAINER}" "${DOCKER_JDK_IMAGE}" \
      cat "/proc/${pid}/status" 2>/dev/null | awk '
        /^VmRSS:/{r=$2}
        /^RssAnon:/{a=$2}
        END {
          if (r == "") r = 0
          if (a == "") a = r
          printf "%.2f %.2f\n", r/1024, a/1024
        }'
    return
  fi
  case "$(uname -s)" in
    Linux)
      local rss rssanon
      rss=$(awk '/^VmRSS:/{print $2}' "/proc/$pid/status" 2>/dev/null || echo 0)
      rssanon=$(awk '/^RssAnon:/{print $2}' "/proc/$pid/status" 2>/dev/null || echo "")
      if [ -z "$rssanon" ]; then rssanon=$rss; fi
      awk -v r="$rss" -v a="$rssanon" 'BEGIN{printf "%.2f %.2f\n", r/1024, a/1024}'
      ;;
    Darwin)
      local kb
      kb=$(ps -p "$pid" -o rss= 2>/dev/null | tr -d ' ' || echo 0)
      awk -v k="$kb" 'BEGIN{v=k/1024; printf "%.2f %.2f\n", v, v}'
      ;;
    *)
      echo "0 0"
      ;;
  esac
}

proc_thread_count() {
  local pid="$1"
  if [ "$MODE" = "docker" ]; then
    local tc
    tc=$(docker run --rm --pid="container:${DOCKER_CONTAINER}" "${DOCKER_JDK_IMAGE}" \
      cat "/proc/${pid}/status" 2>/dev/null | awk '/^Threads:/{print $2; exit}')
    echo "${tc:-0}"
    return
  fi
  case "$(uname -s)" in
    Linux)
      awk '/^Threads:/{print $2}' "/proc/$pid/status" 2>/dev/null || echo 0
      ;;
    Darwin)
      local n
      n=$(ps -p "$pid" -o thcount= 2>/dev/null | tr -d ' ')
      if [ -n "$n" ]; then echo "$n"; return; fi
      n=$(ps -M "$pid" 2>/dev/null | wc -l | tr -d ' ')
      if [ "${n:-0}" -gt 1 ]; then echo $((n - 1)); else echo 0; fi
      ;;
    *)
      echo 0
      ;;
  esac
}

JAVA_PID=""
PID_FOR_REPORT=""
if [ "$MODE" = "host" ]; then
  JAVA_PID="$(find_blog_server_java_pid || true)"
  if [ -z "${JAVA_PID:-}" ]; then
    echo "未找到 blog-server Java 进程，请先启动后端。" >&2
    exit 1
  fi
  PID_FOR_REPORT="$JAVA_PID"
elif [ "$MODE" = "http" ]; then
  SNAP_URL="${BASE_URL%/}/post/runtime/review-leak-signals-snapshot"
  if ! curl -sS -m 15 -f "$SNAP_URL" | jq -e "$UNWRAP | type == \"object\"" >/dev/null 2>&1; then
    echo "HTTP 模式无法访问快照接口: $SNAP_URL" >&2
    echo "请确认：后端已启动、BASE_URL 正确、且为 SPRING_PROFILES_ACTIVE=review（该接口仅在 review 注册）。" >&2
    exit 1
  fi
  PID_FOR_REPORT="HTTP"
elif [ "$MODE" = "docker" ]; then
  if ! docker inspect "$DOCKER_CONTAINER" >/dev/null 2>&1; then
    echo "找不到 Docker 容器: $DOCKER_CONTAINER（请先 docker compose up 评审栈）。" >&2
    exit 1
  fi
  if [ "$DOCKER_PULL" = "1" ]; then
    echo "正在拉取 JDK 镜像: $DOCKER_JDK_IMAGE ..."
    docker pull "${DOCKER_JDK_IMAGE}"
  fi
  JAVA_PID="$DOCKER_JAVA_PID"
  PID_FOR_REPORT="docker:${DOCKER_CONTAINER}:pid${JAVA_PID}"
  echo "docker 模式：将使用镜像 $DOCKER_JDK_IMAGE 对容器 $DOCKER_CONTAINER 内 PID $JAVA_PID 执行 jcmd（首次可能自动拉取镜像，较慢）。"
fi

if [ ! -f "$PLUGIN_SOURCE_JAR" ]; then
  echo "未找到插件源 jar: $PLUGIN_SOURCE_JAR" >&2
  echo "任选其一：先启动评审 Docker 使 ${BUNDLED_DEMO_JAR} 存在；或在 AyeezBlog-Backend 执行 ./mvnw package -pl blog-server,blog-plugin-demo -am -DskipTests；或传 --plugin-source-jar。" >&2
  exit 1
fi

mkdir -p "$PLUGIN_DIR"

HEARTBEAT_PATH="$PLUGIN_DIR/side-effect-heartbeat.log"

hb_bytes() {
  if [ -f "$1" ]; then wc -c <"$1" | tr -d ' '; else echo 0; fi
}

HB_BEFORE_BYTES=$(hb_bytes "$HEARTBEAT_PATH")

# 预热切换
for ((ex = 1; ex <= EXERCISE_ROUNDS; ex++)); do
  printf -v tag '%03d' "$ex"
  cp -f "$PLUGIN_SOURCE_JAR" "$PLUGIN_DIR/leak-check-${tag}.jar"
  sleep_secs=$(awk -v ms="$EXERCISE_WAIT_MS" 'BEGIN{printf "%f", ms/1000}')
  sleep "$sleep_secs"
done

TMP_SAMPLES=$(mktemp)
CL_BODY=""
trap 'rm -f "$TMP_SAMPLES" "${TMP_SAMPLES}.full"; [ -n "${CL_BODY:-}" ] && rm -f "$CL_BODY"' EXIT
CL_CMD=""
CL_TEXT=""
if [ "$MODE" = "host" ] || [ "$MODE" = "docker" ]; then
  CL_BODY=$(mktemp)
  CL_CMD=$(write_classloader_probe "$JAVA_PID" "$CL_BODY")
  CL_TEXT=$(cat "$CL_BODY")
else
  CL_CMD="HTTP:GET /post/runtime/review-leak-signals-snapshot"
  CL_TEXT=""
fi

echo "Sampling leak signals (mode=$MODE)..."
echo "PID=$PID_FOR_REPORT Samples=$SAMPLES IntervalSeconds=$INTERVAL_SECONDS"
echo "PluginDir=$PLUGIN_DIR"
echo "PluginSourceJar=$PLUGIN_SOURCE_JAR ExerciseRounds=$EXERCISE_ROUNDS"
echo "ClassLoaderCommand=$CL_CMD"

: >"$TMP_SAMPLES"

if [ "$MODE" = "http" ]; then
  SNAP_URL="${BASE_URL%/}/post/runtime/review-leak-signals-snapshot"
  for ((i = 1; i <= SAMPLES; i++)); do
    raw=$(curl -sS -m 20 -f "$SNAP_URL") || {
      echo "采样失败: $SNAP_URL" >&2
      exit 1
    }
    printf '%s' "$raw" | jq -c --argjson idx "$i" "$UNWRAP | . + {index: \$idx}" >>"$TMP_SAMPLES"
    if [ "$i" -lt "$SAMPLES" ]; then sleep "$INTERVAL_SECONDS"; fi
  done
else
  for ((i = 1; i <= SAMPLES; i++)); do
    read -r working_mb private_mb <<<"$(proc_mem_mb "$JAVA_PID")"
    proc_threads=$(proc_thread_count "$JAVA_PID")

    thread_print=$(run_jcmd_pid "$JAVA_PID" Thread.print 2>&1 || true)
    jcmd_threads=$(parse_thread_count_jcmd "$thread_print")
    jcmd_threads=${jcmd_threads:-0}
    hb_threads=$(parse_heartbeat_thread_count "$thread_print")

    url_cnt=$(parse_url_classloader_count "$CL_TEXT")
    if [ -z "$url_cnt" ]; then
      url_cnt=$(histogram_url_classloader_count "$JAVA_PID" || true)
      [ -z "$url_cnt" ] && url_cnt="null"
    fi

    heap_info=$(run_jcmd_pid "$JAVA_PID" GC.heap_info 2>&1 || true)
    heap_snip=$(echo "$heap_info" | head -n 3 | awk '{if(NR>1) s=s" | "; s=s$0} END{print s}')

    at_iso="$(date -u +"%Y-%m-%dT%H:%M:%S")"

    uc_json="null"
    if [ "$url_cnt" != "null" ]; then uc_json="$url_cnt"; fi

    jq -nc \
      --argjson idx "$i" \
      --arg at "$at_iso" \
      --argjson pt "$proc_threads" \
      --argjson jt "$jcmd_threads" \
      --argjson ht "$hb_threads" \
      --argjson uc "$uc_json" \
      --argjson ws "$working_mb" \
      --argjson pr "$private_mb" \
      --arg hs "$heap_snip" \
      '{index: $idx, at: $at, processThreads: $pt, jcmdThreads: $jt, heartbeatThreads: $ht, urlClassLoaders: (if $uc == null then null else ($uc|tonumber) end), workingSetMB: ($ws|tonumber), privateMB: ($pr|tonumber), heapInfoSnippet: $hs}' \
      >>"$TMP_SAMPLES"

    if [ "$i" -lt "$SAMPLES" ]; then sleep "$INTERVAL_SECONDS"; fi
  done
fi

echo "采样完成（${SAMPLES} 条），正在聚合指标并写入 scripts/review/output/ …"

HB_AFTER_EX_BYTES=$(hb_bytes "$HEARTBEAT_PATH")

curl -sS -m 15 -f -X POST "${BASE_URL%/}/post/runtime/revert-page-size-rule-to-configured" -o /dev/null || true
sleep "$POST_REVERT_QUIET_SECONDS"

if [ "$MODE" = "http" ]; then
  hb_after_revert=$(curl -sS -m 20 -f "${BASE_URL%/}/post/runtime/review-leak-signals-snapshot" | jq -r "$UNWRAP | .heartbeatThreads // 0" || echo 0)
else
  thread_after=$(run_jcmd_pid "$JAVA_PID" Thread.print 2>&1 || true)
  hb_after_revert=$(parse_heartbeat_thread_count "$thread_after")
fi
HB_AFTER_QUIET_BYTES=$(hb_bytes "$HEARTBEAT_PATH")

# JAR 删除探测
jar_checked=false
jar_can_delete=false
jar_msg="no jar found under plugin dir"
jar_target=""
latest_jar=$(ls -t "$PLUGIN_DIR"/*.jar 2>/dev/null | head -1 || true)
if [ -n "$latest_jar" ] && [ -f "$latest_jar" ]; then
  jar_checked=true
  jar_target="$latest_jar"
  tmp_jar="${latest_jar}.leakcheck.tmp"
  if cp -f "$latest_jar" "$tmp_jar" 2>/dev/null && rm -f "$tmp_jar" 2>/dev/null; then
    jar_can_delete=true
    jar_msg="temp copy/delete succeeded"
  else
    jar_msg="temp copy/delete failed"
  fi
elif [ ! -d "$PLUGIN_DIR" ]; then
  jar_msg="plugin directory not found: $PLUGIN_DIR"
fi

SAMPLE_JSON=$(jq -s '.' "$TMP_SAMPLES")
sample_count=$(echo "$SAMPLE_JSON" | jq -r 'length')
if [ "${sample_count:-0}" -lt 1 ]; then
  echo "错误：未采集到任何采样（合并 ${TMP_SAMPLES} 为空或无效）。脚本已在写入报告前退出。" >&2
  echo "请确认：① 在仓库根目录执行（路径中应能看到 scripts/review/）；② Docker 与后端容器 ${DOCKER_CONTAINER} 正常；③ 未 Ctrl+C 中断；④ 本机 jq 正常。" >&2
  exit 1
fi

proc_start=$(echo "$SAMPLE_JSON" | jq -r '.[0].processThreads // 0')
proc_end=$(echo "$SAMPLE_JSON" | jq -r '.[-1].processThreads // 0')
proc_delta=$((proc_end - proc_start))

priv_start=$(echo "$SAMPLE_JSON" | jq -r '.[0].privateMB // 0')
priv_end=$(echo "$SAMPLE_JSON" | jq -r '.[-1].privateMB // 0')
priv_delta=$(awk -v a="$priv_start" -v b="$priv_end" 'BEGIN{printf "%.2f", b-a}')

uc_start=$(echo "$SAMPLE_JSON" | jq '.[0].urlClassLoaders')
uc_end=$(echo "$SAMPLE_JSON" | jq '.[-1].urlClassLoaders')
if [ "$uc_start" = "null" ] || [ "$uc_end" = "null" ]; then
  uc_delta="null"
else
  uc_delta=$((uc_end - uc_start))
fi

hb_peak=$(echo "$SAMPLE_JSON" | jq -r '[.[].heartbeatThreads] | max // 0')

growth_exercise=$((HB_AFTER_EX_BYTES - HB_BEFORE_BYTES))
growth_after_revert=$((HB_AFTER_QUIET_BYTES - HB_AFTER_EX_BYTES))

risks_json='[]'
add_risk() {
  local msg="$1"
  risks_json=$(echo "$risks_json" | jq --arg m "$msg" '. + [$m]')
}

if [ "$proc_delta" -ge 10 ] 2>/dev/null; then
  add_risk "Thread count increased noticeably (process thread delta = $proc_delta)"
fi
priv_cmp=$(echo "$priv_delta 200" | awk '{if ($1+0 >= $2+0) print 1; else print 0}')
if [ "$priv_cmp" = "1" ]; then
  add_risk "Private memory increased noticeably (private MB delta = $priv_delta)"
fi
if [ "$uc_delta" != "null" ] && [ "$uc_delta" -ge 3 ] 2>/dev/null; then
  add_risk "URLClassLoader count increased noticeably (delta = $uc_delta)"
fi
if [ "${hb_peak:-0}" -gt 0 ] 2>/dev/null && [ "${hb_after_revert:-0}" -gt 0 ] 2>/dev/null; then
  add_risk "Heartbeat threads still alive after revert (count = $hb_after_revert)"
fi
if [ "$growth_exercise" -gt 0 ] && [ "$growth_after_revert" -gt 0 ]; then
  add_risk "Heartbeat log still grows after revert (bytes increased = $growth_after_revert)"
fi
if [ "$jar_checked" = true ] && [ "$jar_can_delete" = false ]; then
  add_risk "Jar temp copy/delete failed; possible file handle retention"
fi
if [ "$jar_checked" = false ]; then
  add_risk "Jar delete check not executed effectively ($jar_msg)"
fi

if [ "$(echo "$risks_json" | jq 'length')" -eq 0 ]; then
  risk_level="LOW"
else
  risk_level="MEDIUM_OR_HIGH"
fi

jar_chk=$( [ "$jar_checked" = true ] && echo true || echo false )
jar_del=$( [ "$jar_can_delete" = true ] && echo true || echo false )
jar_obj=$(jq -cn \
  --argjson chk "$jar_chk" \
  --argjson del "$jar_del" \
  --arg msg "$jar_msg" \
  --arg tj "$jar_target" \
  '{checked: $chk, canDelete: $del, message: $msg, targetJar: $tj}')

jq -s '.' "$TMP_SAMPLES" >"$TMP_SAMPLES.full"
priv_json=$(jq -n --arg v "${priv_delta:-0}" '$v|tonumber')
if [ "$uc_delta" = "null" ]; then ucd_json=null; else ucd_json="$uc_delta"; fi

PID_JSON_VAL=$(if [ "$MODE" = "http" ]; then echo null; else echo "$JAVA_PID"; fi)

jq -n \
  --arg bu "$BASE_URL" \
  --argjson pid "$PID_JSON_VAL" \
  --arg pd "$PLUGIN_DIR" \
  --argjson sm "$SAMPLES" \
  --argjson iv "$INTERVAL_SECONDS" \
  --argjson er "$EXERCISE_ROUNDS" \
  --argjson pq "$POST_REVERT_QUIET_SECONDS" \
  --argjson td "$proc_delta" \
  --argjson pdd "$priv_json" \
  --argjson ucd "$ucd_json" \
  --argjson hbp "$hb_peak" \
  --argjson hbar "$hb_after_revert" \
  --argjson gex "$growth_exercise" \
  --argjson gar "$growth_after_revert" \
  --arg clc "$CL_CMD" \
  --argjson jo "$jar_obj" \
  --arg rl "$risk_level" \
  --argjson ri "$risks_json" \
  --slurpfile sd "$TMP_SAMPLES.full" \
  '{
    baseUrl: $bu,
    pid: $pid,
    pluginDir: $pd,
    samples: $sm,
    intervalSeconds: $iv,
    exerciseRounds: $er,
    postRevertQuietSeconds: $pq,
    threadDelta: $td,
    privateMemoryDeltaMB: $pdd,
    urlClassLoaderDelta: (if $ucd == null then null else ($ucd|tonumber) end),
    heartbeatThreadPeak: $hbp,
    heartbeatThreadsAfterRevert: $hbar,
    heartbeatLogGrowthBytesDuringExercise: $gex,
    heartbeatLogGrowthBytesAfterRevert: $gar,
    classLoaderCommandUsed: $clc,
    jarDeleteCheck: $jo,
    riskLevel: $rl,
    riskItems: $ri,
    sampledData: $sd[0]
  }' >"$JSON_PATH"

rm -f "$TMP_SAMPLES.full"

RISK_ITEMS_MD=$(echo "$risks_json" | jq -r 'if length == 0 then "- No obvious leak signals in current short sampling window." else (map("- " + .) | join("\r\n")) end')

SAMPLES_TABLE=$(jq -r '[.sampledData[] | "| \(.index) | \(.at) | \(.processThreads) | \(.jcmdThreads) | \(.heartbeatThreads) | \(.urlClassLoaders // "null") | \(.workingSetMB) | \(.privateMB) |"] | join("\r\n")' "$JSON_PATH")

JAR_SUMMARY="$jar_can_delete ($jar_msg)"

{
  printf '\xEF\xBB\xBF'
  jq --rawfile tpl "$TEMPLATE_PATH" -n \
    --arg rl "$risk_level" \
    --arg pid "$PID_FOR_REPORT" \
    --arg sm "$SAMPLES" \
    --arg iv "$INTERVAL_SECONDS" \
    --arg td "$proc_delta" \
    --arg pd "$priv_delta" \
    --arg ucd "${uc_delta:-}" \
    --arg hbp "$hb_peak" \
    --arg hbar "$hb_after_revert" \
    --arg gex "$growth_exercise" \
    --arg gar "$growth_after_revert" \
    --arg er "$EXERCISE_ROUNDS" \
    --arg pq "$POST_REVERT_QUIET_SECONDS" \
    --arg js "$JAR_SUMMARY" \
    --arg ri "$RISK_ITEMS_MD" \
    --arg st "$SAMPLES_TABLE" \
    '($tpl|tostring)
      | gsub("{{RISK_LEVEL}}"; $rl)
      | gsub("{{PID}}"; $pid)
      | gsub("{{SAMPLES}}"; $sm)
      | gsub("{{INTERVAL_SECONDS}}"; $iv)
      | gsub("{{THREAD_DELTA}}"; $td)
      | gsub("{{PRIVATE_DELTA}}"; $pd)
      | gsub("{{URL_CLASSLOADER_DELTA}}"; $ucd)
      | gsub("{{HEARTBEAT_THREAD_PEAK}}"; $hbp)
      | gsub("{{HEARTBEAT_THREAD_AFTER_REVERT}}"; $hbar)
      | gsub("{{HEARTBEAT_GROWTH_DURING_EXERCISE}}"; $gex)
      | gsub("{{HEARTBEAT_GROWTH_AFTER_REVERT}}"; $gar)
      | gsub("{{EXERCISE_ROUNDS}}"; $er)
      | gsub("{{POST_REVERT_QUIET_SECONDS}}"; $pq)
      | gsub("{{JAR_DELETE_CHECK}}"; $js)
      | gsub("{{RISK_ITEMS}}"; $ri)
      | gsub("{{SAMPLES_TABLE}}"; $st)'
} >"$REPORT_PATH"

echo "Leak signal check finished."
echo "JSON: $JSON_PATH"
echo "Report: $REPORT_PATH"
