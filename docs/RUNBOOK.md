# Runbook — system-gestion-soutenance (staging/prod, single VPS)

All commands run from the repo root with a `.env` file present
(`cp .env.example .env`, then fill real values and run `sh scripts/check-env.sh`).

## Fresh install

```bash
cp .env.example .env
# edit .env: strong JWT_SECRET (≥32 bytes), DB passwords, real SMTP_HOST,
# CORS_ORIGINS=https://<your-domain>, COOKIE_SECURE=true, APP_HOST=<your-domain>,
# BOOTSTRAP_ADMIN_EMAIL/PASSWORD (first boot only)
sh scripts/check-env.sh
```

> First boot must create the schema: start once with `DDL_AUTO=update`,
> then flip back to `validate` for steady state (validate on an empty
> database refuses to boot — by design).

```bash
DDL_AUTO=update docker compose up --build -d
docker compose ps                    # all services Up (db healthy)
# wait for app health, then:
docker compose exec app wget -qO- http://localhost:8080/actuator/health
# log in as BOOTSTRAP_ADMIN, rotate the password, unset BOOTSTRAP_* in .env
DDL_AUTO=validate docker compose up -d app   # steady state from now on
```

Caddy provisions TLS automatically once `APP_HOST` is a real domain pointing
at the VPS. For `localhost`, plain HTTP is served.

## Smoke checklist (after every deploy)

1. `GET /actuator/health` → `{"status":"UP"}`
2. Login per role via `/login` (admin/coordinator/teacher/student)
3. Coordinator → Documents → schedule PDF downloads a valid PDF
4. Refresh flow: wait out the 15-min access token or delete the `jwt_token`
   cookie → next request transparently refreshes (no login redirect)
5. Logout → refresh token rejected (401)

## Upgrade

```bash
git pull
docker compose up --build -d
# watch: docker compose logs -f app
# then run the smoke checklist above
```

Rollback: `git checkout <previous-tag-or-sha> && docker compose up --build -d`.

## Backup & restore

- `backup` service dumps MySQL nightly to the `db_backups` volume
  (`defensedb-YYYYMMDD-HHMMSS.sql.gz`, 7-day rotation).
- List: `docker compose exec backup ls -la /backups`
- Copy off-host: `docker cp $(docker compose ps -q backup):/backups/<file> ./`
- Restore into a fresh stack:
  ```bash
  docker compose down -v
  docker compose up -d db && sleep 30   # wait healthy
  gunzip -c <file> | docker compose exec -T db mysql -uroot -p"$MYSQL_ROOT_PASSWORD" defensedb
  docker compose up -d
  ```
- Uploaded documents live in the `uploads_data` volume (`/app/uploads`).
  Back it up alongside the DB (replace `<project>` with your compose project
  name, shown by `docker volume ls`):
  `docker run --rm -v <project>_uploads_data:/data -v "$PWD":/out alpine tar czf /out/uploads-$(date +%F).tgz /data`.

## Notes

- Demo seed data NEVER runs in prod (`app.seed.enabled=false` default there).
  E2E CI opts back in explicitly (`APP_SEED_ENABLED=true`).
- Stuck deploys: `docker compose logs app | tail`, `docker compose ps`.
- Full reset (destroys all data): `docker compose down -v`.
