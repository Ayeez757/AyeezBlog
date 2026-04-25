# AyeezBlog Docker Compose Production Deployment Guide

This guide targets open-source users and explains how to deploy AyeezBlog on a fresh Linux server with Docker Compose.

Default access after deployment:

- Frontend: `http://<your-server-ip-or-domain>/`
- Admin panel: `http://<your-server-ip-or-domain>/admin/`
- Backend API: `http://<your-server-ip-or-domain>/api`

## 1. Prerequisites

- OS: Ubuntu 22.04+ (other Linux distributions should also work)
- Installed:
  - Docker
  - Docker Compose (`docker compose`)
  - Git
- Open ports:
  - `80` (HTTP)
  - `443` (HTTPS, optional)
  - `3306` (only if you need external MySQL access; not recommended on public network)

Check versions:

```bash
docker --version
docker compose version
git --version
```

## 2. Clone repository

```bash
mkdir -p /opt/ayeezblog
cd /opt/ayeezblog
git clone https://github.com/Ayeez757/AyeezBlog.git .
```

## 3. Create deployment configuration

Create `.env`:

```bash
cp deploy/prod/.env.example deploy/prod/.env
```

Edit `deploy/prod/.env` with your real values:

```dotenv
TZ=Asia/Shanghai
MYSQL_VOLUME_NAME=ayeezblog_mysql_data
MYSQL_ROOT_PASSWORD=your_strong_password
HM_DB_USER=blog_user
HM_DB_PASSWORD=your_app_password
HM_JWT_SECRET_KEY=your_random_secret
```

Create external network and volumes on first deploy:

```bash
docker network create ayeez || true
docker volume create html || true
docker volume create config || true
docker volume create "$(awk -F= '/^MYSQL_VOLUME_NAME=/{print $2}' deploy/prod/.env)" || true
```

## 4. Start services

```bash
docker compose -f deploy/prod/docker-compose.yml --env-file deploy/prod/.env up -d --build
docker compose -f deploy/prod/docker-compose.yml --env-file deploy/prod/.env ps
```

## 5. Initialize database (first deploy only)

```bash
docker exec mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS ayeezblog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"'
docker exec -i mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" ayeezblog' < "AyeezBlog建表.sql"
```

## 6. Validate deployment

```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
curl -I http://127.0.0.1
curl -I http://127.0.0.1:8080
```

Expected containers include:

- `mysql`
- `ayeezblog-backend`
- `nginx`

## 7. Common operations

Restart:

```bash
docker compose -f deploy/prod/docker-compose.yml --env-file deploy/prod/.env restart
```

Logs:

```bash
docker compose -f deploy/prod/docker-compose.yml --env-file deploy/prod/.env logs --tail=200 mysql
docker compose -f deploy/prod/docker-compose.yml --env-file deploy/prod/.env logs --tail=200 backend
docker compose -f deploy/prod/docker-compose.yml --env-file deploy/prod/.env logs --tail=200 nginx
```

Rebuild backend only:

```bash
docker compose -f deploy/prod/docker-compose.yml --env-file deploy/prod/.env up -d --build backend
```

## 8. Data safety

- Never run `docker compose down -v` in production.
- Never remove the MySQL data volume.

Recommended backup:

```bash
mkdir -p /root/db-backup
docker exec mysql sh -c 'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --all-databases --single-transaction --routines --events --triggers --set-gtid-purged=OFF' > /root/db-backup/all-databases-$(date +%F-%H%M%S).sql
ls -lh /root/db-backup
```

## 9. Troubleshooting

### 9.1 MySQL fails to start

```bash
docker logs --tail=200 mysql
```

Check:

- Password values in `.env`
- Whether `MYSQL_VOLUME_NAME` volume exists and is writable

### 9.2 `container name ... already in use`

```bash
docker rm -f mysql ayeezblog-backend nginx
```

Then run `docker compose up -d --build` again.

### 9.3 Backend cannot connect to MySQL

Check:

- `HM_DB_USER/HM_DB_PASSWORD`
- `HM_DB_HOST` equals `mysql` inside Compose network
- MySQL container is `Up`
