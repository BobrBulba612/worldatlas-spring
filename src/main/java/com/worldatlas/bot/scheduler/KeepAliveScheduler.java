package com.worldatlas.bot.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class KeepAliveScheduler {
    private static final Logger log = LoggerFactory.getLogger(KeepAliveScheduler.class);
    
    @Value("${app.url:}")
    private String appUrl;
    
    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();
    
    @Scheduled(fixedRate = 600000) // Каждые 10 минут
    public void keepAlive() {
        if (appUrl == null || appUrl.isEmpty()) {
            log.debug("APP_URL not set, skipping keep-alive");
            return;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(appUrl + "/actuator/health"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                log.info("✅ Keep-alive ping successful: {}", appUrl);
            }
        } catch (Exception e) {
            log.error("❌ Keep-alive ping error: {}", e.getMessage());
        }
    }
}
