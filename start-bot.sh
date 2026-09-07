#!/bin/bash
cd /home/denis/worldatlas-spring
pkill -f proxy.py 2>/dev/null
sleep 1
python3 /home/denis/worldatlas-spring/proxy.py > /tmp/proxy.log 2>&1 &
sleep 3
echo "✅ Прокси запущен на 127.0.0.1:8081"
java -jar target/bot-0.0.1-SNAPSHOT.jar
