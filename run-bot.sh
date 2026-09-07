#!/bin/bash
cd /home/denis/worldatlas-spring

# Запускаем прокси в фоне
pkill -f "proxy.py" 2>/dev/null || true
sleep 1
python3 /home/denis/worldatlas-spring/proxy.py > /tmp/proxy.log 2>&1 &
PROXY_PID=$!
sleep 3

echo "✅ Прокси запущен (PID: $PROXY_PID)"

# Функция очистки при остановке
cleanup() {
    echo "Останавливаем прокси..."
    kill $PROXY_PID 2>/dev/null
    exit 0
}
trap cleanup SIGTERM SIGINT

# Запускаем бота (в основном процессе)
exec java -jar target/bot-0.0.1-SNAPSHOT.jar
