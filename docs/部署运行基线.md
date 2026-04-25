# Production Runtime Baseline (Before Compose Migration)

This snapshot captures the currently running production topology before migration.

## Containers

- `ayeezblog-backend`
  - image: `ayeezblog:latest`
  - ports: `8080:8080`
  - network: `ayeez`
- `nginx`
  - image: `nginx:1.29.5`
  - ports: `80:80`, `443:443`
  - volumes:
    - `html` -> `/usr/share/nginx/html`
    - `config` -> `/etc/nginx`
  - network: `ayeez`
- `mysql`
  - image: `mysql:9.6.0`
  - ports: `3306:3306`
  - volume:
    - `00bf7a5c3317bc4d650e5815dbfd74b4866867e2d46cba4e75958b8434e28e08` -> `/var/lib/mysql`
  - networks: `ayeez`, `bridge`

## Network

- external network name: `ayeez`
- subnet: `172.18.0.0/16`

## Migration Guardrails

- Do not run `docker compose down -v`.
- Do not remove `html`, `config`, or MySQL data volume.
- Keep container names unchanged (`mysql`, `nginx`, `ayeezblog-backend`).
- Keep backend profile as `dev`.
