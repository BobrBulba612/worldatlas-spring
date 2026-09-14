#!/bin/bash
# Бэкап базы данных каждый день

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="/tmp/backup_$DATE.sql.gz"

pg_dump "$DATABASE_URL" | gzip > "$BACKUP_FILE"

echo "✅ Бэкап создан: $BACKUP_FILE ($(du -h $BACKUP_FILE | cut -f1))"

# Удаляем бэкапы старше 30 дней
find /tmp -name "backup_*.sql.gz" -mtime +30 -delete
