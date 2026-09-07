package com.worldatlas.bot.service;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TimeService {
    private final ConcurrentHashMap<String, CachedTime> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 60_000; // 60 секунд

    public ZonedDateTime getAccurateNow(ZoneId zone) {
        String zoneId = zone.getId();
        CachedTime cached = cache.get(zoneId);
        long nowMs = System.currentTimeMillis();
        
        if (cached != null && (nowMs - cached.timestamp) < CACHE_TTL_MS) {
            return cached.time;
        }
        
        try {
            String urlStr = "https://timeapi.io/api/Time/current/zone?timeZone=" + zoneId;
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = in.readLine();
            in.close();
            
            if (response != null && response.contains("\"dateTime\"")) {
                String[] parts = response.split("\"dateTime\":\"");
                if (parts.length > 1) {
                    String dt = parts[1].split("\"")[0];
                    LocalDateTime localDateTime = LocalDateTime.parse(dt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    ZonedDateTime result = localDateTime.atZone(zone);
                    cache.put(zoneId, new CachedTime(result, nowMs));
                    return result;
                }
            }
        } catch (Exception e) {
            System.err.println("TimeAPI failed for " + zoneId + ": " + e.getMessage());
        }
        return ZonedDateTime.now(zone);
    }

    private static class CachedTime {
        final ZonedDateTime time;
        final long timestamp;
        CachedTime(ZonedDateTime time, long timestamp) {
            this.time = time;
            this.timestamp = timestamp;
        }
    }
}