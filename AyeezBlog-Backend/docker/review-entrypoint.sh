#!/bin/sh
# 将镜像内的 demo jar 放到 bundled-demo（非 Spring 监听目录）；监听目录 page-size 默认为空，便于 Level1。
# Level2：把 jar 从 plugins/bundled-demo 复制到 plugins/page-size 即可触发自动加载，无需改 compose 环境变量。
set -e
DEMO_SRC=/opt/review-defaults/blog-plugin-demo.jar
DEMO_DOUBLE_SRC=/opt/review-defaults/blog-plugin-demo-double.jar
BUNDLED_DIR=/app/plugins/bundled-demo
PAGE_SIZE_DIR=/app/plugins/page-size
mkdir -p "$BUNDLED_DIR" "$PAGE_SIZE_DIR"
# 始终用镜像内 jar 覆盖 bundled-demo，避免宿主机卷残留旧构建产物（改 plugin.properties 重建镜像后仍能拿到新 jar）。
if [ -f "$DEMO_SRC" ]; then
  cp -f "$DEMO_SRC" "$BUNDLED_DIR/blog-plugin-demo-0.0.1-SNAPSHOT.jar"
fi
# 无副作用「纯双倍」演示 jar；Level2 压力/泄露脚本默认使用上方无 classifier 的主 jar（Stateful）。
if [ -f "$DEMO_DOUBLE_SRC" ]; then
  cp -f "$DEMO_DOUBLE_SRC" "$BUNDLED_DIR/blog-plugin-demo-0.0.1-SNAPSHOT-double.jar"
fi
exec java -jar /app/app.jar "$@"
