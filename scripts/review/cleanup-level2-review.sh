#!/usr/bin/env bash
# 清理 review-loop-*.jar 与 output 报告产物（与 cleanup-level2-review.ps1 对齐）

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
PLUGIN_DIR="$REPO_ROOT/AyeezBlog-Backend/plugins/page-size"
OUTPUT_DIR="$SCRIPT_DIR/output"

echo "Starting cleanup..."

if [ -d "$PLUGIN_DIR" ]; then
  shopt -s nullglob
  for f in "$PLUGIN_DIR"/review-loop-*.jar; do
    rm -f "$f"
    echo "Deleted plugin file: $(basename "$f")"
  done
  shopt -u nullglob
else
  echo "Plugin directory not found, skipped."
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
