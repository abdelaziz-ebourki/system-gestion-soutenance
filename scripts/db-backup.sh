#!/bin/sh
# Nightly MySQL dump with 7-day rotation. Runs inside the `backup` service.
set -eu
set -o pipefail 2>/dev/null || true

: "${MYSQL_ROOT_PASSWORD:?MYSQL_ROOT_PASSWORD must be set}"
MYSQL_DATABASE="${MYSQL_DATABASE:-defensedb}"
BACKUP_DIR="${BACKUP_DIR:-/backups}"
RETENTION="${BACKUP_RETENTION_DAYS:-7}"

mkdir -p "$BACKUP_DIR"

echo "Waiting for MySQL..."
for i in $(seq 1 30); do
  if mysqladmin ping -h db -uroot -p"$MYSQL_ROOT_PASSWORD" --silent 2>/dev/null; then
    break
  fi
  if [ "$i" = "30" ]; then
    echo "MySQL never became ready" >&2
    exit 1
  fi
  sleep 5
done

while true; do
  TS=$(date +%Y%m%d-%H%M%S)
  FILE="$BACKUP_DIR/defensedb-$TS.sql.gz"
  echo "Dumping $MYSQL_DATABASE to $FILE"
  if mysqldump -h db -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction "$MYSQL_DATABASE" | gzip > "$FILE.tmp"; then
    mv "$FILE.tmp" "$FILE"
    # shellcheck disable=SC2012
    ls -t "$BACKUP_DIR"/defensedb-*.sql.gz 2>/dev/null | tail -n +"$((RETENTION + 1))" | xargs -r rm --
    echo "Backup done, sleeping 24h"
  else
    echo "Backup FAILED, keeping previous dumps" >&2
    rm -f "$FILE.tmp"
    sleep 3600
    continue
  fi
  sleep 86400
done
