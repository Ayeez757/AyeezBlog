ARG IMAGE_PREFIX=m.daocloud.io/docker.io/library

FROM ${IMAGE_PREFIX}/node:18-alpine AS frontend-build
WORKDIR /frontend
COPY AyeezBlog-Frontend/package*.json ./
RUN npm ci
COPY AyeezBlog-Frontend ./
RUN npm run build

FROM ${IMAGE_PREFIX}/node:18-alpine AS admin-build
WORKDIR /admin
COPY AyeezBlog-AdminPanel/package*.json ./
RUN npm ci
COPY AyeezBlog-AdminPanel ./
RUN npm run build

FROM ${IMAGE_PREFIX}/nginx:1.27-alpine AS runtime

# Nginx 配置（前台/管理端静态 + API 反代）
COPY nginx/default.conf /etc/nginx/conf.d/default.conf

# 生成自签证书（让 https 能开箱即用；生产环境建议替换为真实证书）
RUN apk add --no-cache openssl \
  && mkdir -p /etc/nginx/certs \
  && openssl req -x509 -nodes -newkey rsa:2048 -days 365 \
     -subj "/CN=localhost" \
     -keyout /etc/nginx/certs/self-signed.key \
     -out /etc/nginx/certs/self-signed.crt

# 先放前台 dist（提供 /index.html 与 /assets/*）
COPY --from=frontend-build /frontend/dist/ /usr/share/nginx/html/

# 再叠加管理端：/admin/index.html + /assets/*（hash 资源不易冲突）
RUN mkdir -p /usr/share/nginx/html/admin
COPY --from=admin-build /admin/dist/index.html /usr/share/nginx/html/admin/index.html
COPY --from=admin-build /admin/dist/assets/ /usr/share/nginx/html/assets/

