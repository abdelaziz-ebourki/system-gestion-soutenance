#!/bin/sh
# Pre-flight check for required production environment. Run before `docker compose up`.
# Usage: sh scripts/check-env.sh
set -eu

fail=0
need() {
  if [ -z "${1:-}" ]; then
    echo "MISSING: $2" >&2
    fail=1
  fi
}

need "${MYSQL_ROOT_PASSWORD:-}" "MYSQL_ROOT_PASSWORD"
need "${DB_USERNAME:-}" "DB_USERNAME"
need "${DB_PASSWORD:-}" "DB_PASSWORD"
need "${JWT_SECRET:-}" "JWT_SECRET"
need "${SMTP_HOST:-}" "SMTP_HOST (real SMTP server for production)"

if [ -n "${JWT_SECRET:-}" ] && [ "${#JWT_SECRET}" -lt 32 ]; then
  echo "INVALID: JWT_SECRET must be at least 32 bytes" >&2
  fail=1
fi

if [ "${COOKIE_SECURE:-true}" != "true" ]; then
  echo "WARNING: COOKIE_SECURE is not true — auth cookies will work over plain HTTP" >&2
fi

case "${CORS_ORIGINS:-}" in
  *localhost*)
    echo "WARNING: CORS_ORIGINS still references localhost: ${CORS_ORIGINS}" >&2
    ;;
esac

if [ "$fail" -ne 0 ]; then
  echo "Environment check FAILED — see MISSING entries above (copy .env.example to .env first)" >&2
  exit 1
fi
echo "Environment check OK"
