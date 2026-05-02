#!/bin/sh
# 将镜像内的 demo jar 放到 bundled-demo（非 Spring 监听目录）；监听目录 page-size 默认为空，便于 Level1。
# Level2：把 jar 从 plugins/bundled-demo 复制到 plugins/page-size 即可触发自动加载，无需改 compose 环境变量。
set -e
DEMO_SRC=/opt/review-defaults/blog-plugin-demo.jar
BUNDLED_DIR=/app/plugins/bundled-demo
PAGE_SIZE_DIR=/app/plugins/page-size
mkdir -p "$BUNDLED_DIR" "$PAGE_SIZE_DIR"
if [ -f "$DEMO_SRC" ] && ! ls "$BUNDLED_DIR"/*.jar >/dev/null 2>&1; then
  cp "$DEMO_SRC" "$BUNDLED_DIR/blog-plugin-demo-0.0.1-SNAPSHOT.jar"
fi
exec java -jar /app/app.jar "$@"
