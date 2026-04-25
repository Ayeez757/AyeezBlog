# AyeezBlog Docker Compose 线上部署指南

本文档面向开源用户，目标是让你在一台全新 Linux 服务器上，通过 Docker Compose 完成部署。

部署完成后默认访问：

- 前台：`http://<你的服务器IP或域名>/`
- 管理端：`http://<你的服务器IP或域名>/admin/`
- 后端 API：`http://<你的服务器IP或域名>/api`

---

## 1. 前置条件

- 系统：Ubuntu 22.04+（其他 Linux 发行版也可）
- 已安装：
  - Docker
  - Docker Compose（`docker compose` 子命令）
  - Git
- 服务器需开放端口：
  - `80`（HTTP）
  - `443`（HTTPS，可选）
  - `3306`（仅在你需要从外部连接 MySQL 时开放；不建议公网开放）

检查版本：

```bash
docker --version
docker compose version
git --version
```

---

## 2. 拉取项目代码

```bash
mkdir -p /opt/ayeezblog
cd /opt/ayeezblog
git clone https://github.com/Ayeez757/AyeezBlog.git .
```

---

## 3. 创建部署配置

### 3.1 创建 .env

```bash
cp deploy/prod/.env.example deploy/prod/.env
```

按需编辑 `deploy/prod/.env`：

```dotenv
# 时区
TZ=Asia/Shanghai

# MySQL 数据卷名称（新部署可自定义）
MYSQL_VOLUME_NAME=ayeezblog_mysql_data

# 数据库
MYSQL_ROOT_PASSWORD=请改成强密码
HM_DB_USER=blog_user
HM_DB_PASSWORD=请改成业务账号密码

# JWT（请使用足够随机、长度足够的密钥）
HM_JWT_SECRET_KEY=请改成强随机字符串

# 可选：第三方能力（不用可留空）
QINIU_ACCESS_KEY=
QINIU_SECRET_KEY=
QINIU_BUCKET=
QINIU_DOMAIN=
DEEPSEEK_API_KEY=
VOLCENGINE_ACCESS_KEY=
VOLCENGINE_SECRET_KEY=
```

### 3.2 初始化外部网络与卷（首次执行）

`deploy/prod/docker-compose.yml` 使用了 external 网络/卷，首次部署前请创建：

```bash
docker network create ayeez || true
docker volume create html || true
docker volume create config || true
docker volume create "$(awk -F= '/^MYSQL_VOLUME_NAME=/{print $2}' deploy/prod/.env)" || true
```

> 说明：  
> - `html` 用于 Nginx 静态站点文件  
> - `config` 用于 Nginx 配置目录  
> - `MYSQL_VOLUME_NAME` 用于 MySQL 数据持久化

---

## 4. 启动服务

在项目根目录执行：

```bash
docker compose -f deploy/prod/docker-compose.yml --env-file deploy/prod/.env up -d --build
```

查看状态：

```bash
docker compose -f deploy/prod/docker-compose.yml --env-file deploy/prod/.env ps
```

---

## 5. 初始化数据库（首次部署）

等待 MySQL 就绪后，先创建数据库，再导入建表脚本：

```bash
docker exec mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS ayeezblog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"'
docker exec -i mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" ayeezblog' < "AyeezBlog建表.sql"
```

上述命令会在容器内读取 `MYSQL_ROOT_PASSWORD`，不依赖宿主机环境变量。

---

## 6. 验证部署

### 6.1 容器层验证

```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

期望至少看到：

- `mysql`
- `ayeezblog-backend`
- `nginx`

### 6.2 HTTP 验证

```bash
curl -I http://127.0.0.1
curl -I http://127.0.0.1:8080
```

---

## 7. 常用运维命令

重启：

```bash
docker compose -f deploy/prod/docker-compose.yml --env-file deploy/prod/.env restart
```

查看日志：

```bash
docker compose -f deploy/prod/docker-compose.yml --env-file deploy/prod/.env logs --tail=200 mysql
docker compose -f deploy/prod/docker-compose.yml --env-file deploy/prod/.env logs --tail=200 backend
docker compose -f deploy/prod/docker-compose.yml --env-file deploy/prod/.env logs --tail=200 nginx
```

仅重建后端：

```bash
docker compose -f deploy/prod/docker-compose.yml --env-file deploy/prod/.env up -d --build backend
```

---

## 8. 数据安全与备份

### 8.1 重要原则

- 不要执行：`docker compose down -v`
- 不要删除 MySQL 对应的数据卷

### 8.2 建议备份命令

```bash
mkdir -p /root/db-backup
docker exec mysql sh -c 'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --all-databases --single-transaction --routines --events --triggers --set-gtid-purged=OFF' > /root/db-backup/all-databases-$(date +%F-%H%M%S).sql
ls -lh /root/db-backup
```

---

## 9. 常见问题排查

### 9.1 MySQL 启动失败

先看日志：

```bash
docker logs --tail=200 mysql
```

如果容器反复重启，优先检查：

- `.env` 中数据库密码是否有特殊字符转义问题
- `MYSQL_VOLUME_NAME` 对应卷是否存在且可读写

### 9.2 `container name ... already in use`

说明机器上已有同名旧容器：

```bash
docker rm -f mysql ayeezblog-backend nginx
```

然后重新 `docker compose up -d --build`。

### 9.3 后端连不上数据库

检查：

- `HM_DB_USER/HM_DB_PASSWORD` 是否正确
- `HM_DB_HOST` 是否为 `mysql`（compose 内服务名）
- MySQL 是否处于 `Up` 状态

---

## 10. 升级项目版本

```bash
cd /opt/ayeezblog
git pull
docker compose -f deploy/prod/docker-compose.yml --env-file deploy/prod/.env up -d --build
```

升级前建议先做一次数据库备份。
