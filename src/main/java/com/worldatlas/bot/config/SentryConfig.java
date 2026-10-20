package com.worldatlas.bot.config;

import io.sentry.Sentry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SentryConfig {
    
    private static final Logger log = LoggerFactory.getLogger(SentryConfig.class);
    
    @Value("${sentry.dsn:}")
    private String sentryDsn;
    
    @PostConstruct
    public void init() {
        if (sentryDsn == null || sentryDsn.isEmpty()) {
            log.info("Sentry DSN not configured - error monitoring disabled");
            return;
        }
        
        Sentry.init(options -> {
            options.setDsn(sentryDsn);
            options.setEnvironment("production");
            options.setRelease("worldatlas-spring@1.0.0");
            options.setTracesSampleRate(0.2);
            // Не отправляем персональные данные
            options.setSendDefaultPii(false);
            // Автоматически ловим необработанные исключения
            options.setEnableUncaughtExceptionHandler(true);
        });
        
        log.info("✅ Sentry initialized successfully");
    }
}
