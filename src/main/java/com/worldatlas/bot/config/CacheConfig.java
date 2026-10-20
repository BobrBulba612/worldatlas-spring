package com.worldatlas.bot.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Кэш для погоды: хранится 15 минут, максимум 1000 городов
        cacheManager.registerCustomCache("weather", 
            Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build());
                
        // Кэш для гео-поиска: хранится 24 часа
        cacheManager.registerCustomCache("geoSearch", 
            Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .maximumSize(5000)
                .build());
                
        return cacheManager;
    }
}
