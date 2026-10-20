package com.worldatlas.bot.web;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sentry-webhook")
public class SentryWebhookController {
    
    private static final Logger log = LoggerFactory.getLogger(SentryWebhookController.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${telegram.bot.token}")
    private String botToken;

    // По умолчанию используем твой ID, но лучше вынести в переменные окружения
    @Value("${telegram.admin.chat.id:1319065617}")
    private Long adminChatId;

    @PostMapping
    public ResponseEntity<String> handleSentryWebhook(@RequestBody JsonNode payload) {
        try {
            JsonNode data = payload.get("data");
            if (data == null || !data.has("issue")) {
                return ResponseEntity.ok("Ignored non-issue payload");
            }

            JsonNode issue = data.get("issue");
            String title = issue.has("title") ? issue.get("title").asText() : "Неизвестная ошибка";
            String url = data.has("url") ? data.get("url").asText() : "#";
            String project = data.has("project") ? data.get("project").asText() : "worldatlas-spring";

            String message = String.format(
                "🚨 <b>Ошибка в Sentry!</b>\n\n" +
                "📌 <b>Проект:</b> %s\n" +
                "🐛 <b>Ошибка:</b> %s\n" +
                "🔗 <a href=\"%s\">Открыть в Sentry</a>",
                project, title, url
            );

            String telegramUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("chat_id", adminChatId);
            requestBody.put("text", message);
            requestBody.put("parse_mode", "HTML");

            restTemplate.postForObject(telegramUrl, requestBody, String.class);
            log.info("✅ Sentry alert sent to Telegram chat {}", adminChatId);
            
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("❌ Failed to process Sentry webhook", e);
            return ResponseEntity.status(500).body("Error");
        }
    }
}
