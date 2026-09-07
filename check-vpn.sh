#!/bin/bash
# Проверяем, работает ли прокси на порту 8081
if ! curl -x http://127.0.0.1:8081 -m 5 -s https://api.telegram.org > /dev/null 2>&1; then
    echo "$(date): Прокси не отвечает, перезапускаем сервис..."
    sudo systemctl restart worldatlas-bot
fi
