#!/bin/bash
# Бэкап базы данных каждый день

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="/tmp/backup_$DATE.sql.gz"

if [ -z "$DATABASE_URL" ]; then
    echo "❌ DATABASE_URL не задан"
    exit 1
fi

pg_dump "$DATABASE_URL" | gzip > "$BACKUP_FILE"

if [ -s "$BACKUP_FILE" ]; then
    echo "✅ Бэкап создан: $BACKUP_FILE ($(du -h $BACKUP_FILE | cut -f1))"
    find /tmp -name "backup_*.sql.gz" -mtime +30 -delete
else
    echo "❌ Бэкап пустой"
    rm -f "$BACKUP_FILE"
    exit 1
fi
