package com.worldatlas.bot.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimitService {
    
    // Rate limiting для конкретных команд
    private final Map<String, Map<Long, AtomicInteger>> commandCounters = new ConcurrentHashMap<>();
    private final Map<String, Map<Long, Long>> commandResetTimes = new ConcurrentHashMap<>();
    
    // Глобальный rate limiting
    private final Map<Long, AtomicInteger> globalCounters = new ConcurrentHashMap<>();
    private final Map<Long, Long> globalResetTimes = new ConcurrentHashMap<>();
    private static final int GLOBAL_LIMIT = 30; // 30 команд в минуту
    
    /**
     * Проверяет rate limit для конкретной команды
     */
    public boolean checkRateLimit(String command, Long chatId, int limit) {
        long now = System.currentTimeMillis();
        
        Map<Long, Long> resetTimes = commandResetTimes.computeIfAbsent(command, k -> new ConcurrentHashMap<>());
        Map<Long, AtomicInteger> counters = commandCounters.computeIfAbsent(command, k -> new ConcurrentHashMap<>());
        
        Long resetTime = resetTimes.getOrDefault(chatId, now);
        
        if (now - resetTime > 60000) {
            // Сбрасываем счётчик каждую минуту
            counters.put(chatId, new AtomicInteger(0));
            resetTimes.put(chatId, now);
        }
        
        AtomicInteger counter = counters.computeIfAbsent(chatId, k -> new AtomicInteger(0));
        return counter.incrementAndGet() <= limit;
    }
    
    /**
     * Проверяет глобальный rate limit
     */
    public boolean checkGlobalRateLimit(Long chatId) {
        long now = System.currentTimeMillis();
        Long resetTime = globalResetTimes.getOrDefault(chatId, now);
        
        if (now - resetTime > 60000) {
            // Сбрасываем счётчик каждую минуту
            globalCounters.put(chatId, new AtomicInteger(0));
            globalResetTimes.put(chatId, now);
        }
        
        AtomicInteger counter = globalCounters.computeIfAbsent(chatId, k -> new AtomicInteger(0));
        return counter.incrementAndGet() <= GLOBAL_LIMIT;
    }
    
    /**
     * Возвращает оставшееся количество запросов для команды
     */
    public int getRemaining(String command, Long chatId, int limit) {
        Map<Long, AtomicInteger> counters = commandCounters.get(command);
        if (counters == null) return limit;
        
        AtomicInteger counter = counters.get(chatId);
        return counter != null ? Math.max(0, limit - counter.get()) : limit;
    }
    
    /**
     * Возвращает оставшееся количество запросов глобально
     */
    public int getGlobalRemaining(Long chatId) {
        AtomicInteger counter = globalCounters.get(chatId);
        return counter != null ? Math.max(0, GLOBAL_LIMIT - counter.get()) : GLOBAL_LIMIT;
    }
    
    /**
     * Сбрасывает счётчик для команды
     */
    public void resetRateLimit(String command, Long chatId) {
        Map<Long, AtomicInteger> counters = commandCounters.get(command);
        if (counters != null) {
            counters.put(chatId, new AtomicInteger(0));
        }
    }
}
