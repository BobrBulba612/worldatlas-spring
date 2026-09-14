package com.worldatlas.bot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "worldatlas-bot");
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }
    
    @GetMapping("/")
    public String root() {
        return "🌍 WorldAtlas Bot is running!";
    }
}
