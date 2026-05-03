#!/usr/bin/env bash
# 清理 review-loop-*.jar 与 output 报告产物（与 cleanup-level2-review.ps1 对齐）

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
# 与 run-level2-auto-watch / Docker 评审挂载对齐：本地默认目录 + deploy/review 挂载目录
PLUGIN_DIRS=(
  "$REPO_ROOT/deploy/review/plugins/page-size"
  "$REPO_ROOT/AyeezBlog-Backend/plugins/page-size"
  "$REPO_ROOT/AyeezBlog-Backend/blog-server/plugins/page-size"
)
OUTPUT_DIR="$SCRIPT_DIR/output"

echo "Starting cleanup..."

found_any=false
for PLUGIN_DIR in "${PLUGIN_DIRS[@]}"; do
  if [ -d "$PLUGIN_DIR" ]; then
    found_any=true
    shopt -s nullglob
    for f in "$PLUGIN_DIR"/review-loop-*.jar; do
      rm -f "$f"
      echo "Deleted plugin file: $PLUGIN_DIR/$(basename "$f")"
    done
    shopt -u nullglob
  fi
done
if [ "$found_any" = false ]; then
  echo "No plugin directory found among deploy/review and AyeezBlog-Backend defaults; skipped review-loop-*.jar cleanup."
fi

if [ -d "$OUTPUT_DIR" ]; then
  for f in "$OUTPUT_DIR"/*; do
    [ -f "$f" ] || continue
    rm -f "$f"
    echo "Deleted output file: $(basename "$f")"
  done
else
  echo "Output directory not found, skipped."
fi

echo "Cleanup finished."
