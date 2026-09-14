package com.worldatlas.bot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimitService {
    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);
    
    // Fallback in-memory rate limiting если Redis недоступен
    private final ConcurrentHashMap<String, AtomicInteger> inMemoryCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> inMemoryTimestamps = new ConcurrentHashMap<>();
    
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;
    
    /**
     * Проверяет, может ли пользователь выполнить действие
     * @param userId ID пользователя
     * @param action Название действия (например, "support_message")
     * @param maxRequests Максимум запросов за период
     * @param windowSeconds Период в секундах
     * @return true если можно выполнить действие
     */
    public boolean isAllowed(Long userId, String action, int maxRequests, int windowSeconds) {
        String key = "rate_limit:" + action + ":" + userId;
        
        try {
            // Пробуем использовать Redis
            if (redisTemplate != null) {
                Long count = redisTemplate.opsForValue().increment(key);
                if (count != null && count == 1) {
                    redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
                }
                return count != null && count <= maxRequests;
            }
        } catch (Exception e) {
            log.warn("Redis unavailable, falling back to in-memory rate limiting: {}", e.getMessage());
        }
        
        // Fallback на in-memory rate limiting
        return isAllowedInMemory(key, maxRequests, windowSeconds);
    }
    
    private boolean isAllowedInMemory(String key, int maxRequests, int windowSeconds) {
        long now = System.currentTimeMillis();
        long windowMs = windowSeconds * 1000L;
        
        Long lastTime = inMemoryTimestamps.get(key);
        
        if (lastTime == null || (now - lastTime) > windowMs) {
            // Сбрасываем счётчик
            inMemoryTimestamps.put(key, now);
            inMemoryCounters.put(key, new AtomicInteger(1));
            return true;
        }
        
        AtomicInteger counter = inMemoryCounters.computeIfAbsent(key, k -> new AtomicInteger(0));
        int currentCount = counter.incrementAndGet();
        
        return currentCount <= maxRequests;
    }
    
    /**
     * Проверяет ограничение для обычных сообщений боту
     */
    public boolean isUserMessageAllowed(Long userId) {
        return isAllowed(userId, "user_message", 30, 60); // 30 сообщений в минуту
    }
    
    /**
     * Проверяет ограничение для обращений в поддержку
     */
    public boolean isSupportMessageAllowed(Long userId) {
        return isAllowed(userId, "support_message", 1, 60); // 1 сообщение в минуту
    }
    
    /**
     * Проверяет ограничение для создания пользовательских городов
     */
    public boolean isCustomCityAllowed(Long userId) {
        return isAllowed(userId, "custom_city", 5, 60); // 5 городов в минуту
    }
}
