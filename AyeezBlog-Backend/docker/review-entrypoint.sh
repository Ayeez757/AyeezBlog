#!/bin/sh
# 评审镜像：挂载的 plugins/page-size 若为空，则放入内置 demo jar，便于 Level2（外置 jar）开箱演示。
set -e
DEMO_SRC=/opt/review-defaults/blog-plugin-demo.jar
DEMO_DIR=/app/plugins/page-size
mkdir -p "$DEMO_DIR"
if ! ls "$DEMO_DIR"/*.jar >/dev/null 2>&1; then
  if [ -f "$DEMO_SRC" ]; then
    cp "$DEMO_SRC" "$DEMO_DIR/blog-plugin-demo-0.0.1-SNAPSHOT.jar"
  fi
fi
exec java -jar /app/app.jar "$@"
