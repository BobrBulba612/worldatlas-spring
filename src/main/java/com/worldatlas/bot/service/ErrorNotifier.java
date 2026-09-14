package com.worldatlas.bot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ErrorNotifier {
    private static final Logger log = LoggerFactory.getLogger(ErrorNotifier.class);
    
    @Value("${telegram.bot.token}")
    private String botToken;
    
    @Value("${telegram.error.chat-id:1319065617}")
    private String errorChatId;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public void notify(String errorType, String message) {
        try {
            String text = "❌ <b>ОШИБКА БОТА</b>\n\n" +
                "🔴 Тип: " + errorType + "\n" +
                "💬 Сообщение: " + (message != null ? message.substring(0, Math.min(200, message.length())) : "нет") + "\n" +
                "🕐 Время: " + java.time.LocalDateTime.now() + "\n" +
                "🚨 Исправь как можно скорее!";
            
            Map<String, Object> body = new HashMap<>();
            body.put("chat_id", errorChatId);
            body.put("text", text);
            body.put("parse_mode", "HTML");
            
            restTemplate.postForObject(
                "https://api.telegram.org/bot" + botToken + "/sendMessage",
                body,
                Map.class
            );
            
            log.error("Error notification sent: {} - {}", errorType, message);
        } catch (Exception e) {
            log.error("Failed to send error notification: {}", e.getMessage());
        }
    }
}
