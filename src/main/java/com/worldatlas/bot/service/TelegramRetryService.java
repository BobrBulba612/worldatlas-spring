package com.worldatlas.bot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class TelegramRetryService {
    private static final Logger log = LoggerFactory.getLogger(TelegramRetryService.class);
    
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_DELAY_MS = 1000;
    
    public <T extends java.io.Serializable> T executeWithRetry(
        TelegramLongPollingBot bot,
        BotApiMethod<T> method
    ) throws TelegramApiException {
        int attempt = 0;
        long delay = INITIAL_DELAY_MS;
        
        while (attempt < MAX_RETRIES) {
            try {
                return bot.execute(method);
            } catch (TelegramApiException e) {
                attempt++;
                
                String errorMsg = e.getMessage();
                boolean isRateLimit = errorMsg != null && errorMsg.contains("429");
                boolean isNetworkError = errorMsg != null && 
                    (errorMsg.contains("Connection") || errorMsg.contains("Timeout"));
                
                if (attempt >= MAX_RETRIES) {
                    log.error("Max retries ({}) exceeded for method {}: {}", 
                        MAX_RETRIES, method.getClass().getSimpleName(), errorMsg);
                    throw e;
                }
                
                if (isRateLimit || isNetworkError) {
                    log.warn("Attempt {} failed for {}, retrying in {}ms: {}", 
                        attempt, method.getClass().getSimpleName(), delay, errorMsg);
                    
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new TelegramApiException("Retry interrupted", ie);
                    }
                    
                    delay *= 2;
                } else {
                    log.error("Non-retryable error for {}: {}", 
                        method.getClass().getSimpleName(), errorMsg);
                    throw e;
                }
            }
        }
        
        throw new TelegramApiException("Unexpected error: max retries exceeded");
    }
}
