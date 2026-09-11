#!/bin/sh
# Nightly MySQL dump with 7-day rotation. Runs inside the `backup` service.
set -eu

: "${MYSQL_ROOT_PASSWORD:?MYSQL_ROOT_PASSWORD must be set}"
MYSQL_DATABASE="${MYSQL_DATABASE:-defensedb}"
BACKUP_DIR="${BACKUP_DIR:-/backups}"
RETENTION="${BACKUP_RETENTION_DAYS:-7}"

mkdir -p "$BACKUP_DIR"

while true; do
  TS=$(date +%Y%m%d-%H%M%S)
  FILE="$BACKUP_DIR/defensedb-$TS.sql.gz"
  echo "Dumping $MYSQL_DATABASE to $FILE"
  mysqldump -h db -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction "$MYSQL_DATABASE" | gzip > "$FILE"
  # shellcheck disable=SC2012
  ls -t "$BACKUP_DIR"/defensedb-*.sql.gz 2>/dev/null | tail -n +"$((RETENTION + 1))" | xargs -r rm --
  echo "Backup done, sleeping 24h"
  sleep 86400
done
