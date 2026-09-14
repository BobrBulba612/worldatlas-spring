package com.worldatlas.bot.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@Service
public class HolidayService {
    private static final Logger log = LoggerFactory.getLogger(HolidayService.class);
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Получает праздники для страны на текущий день
     * Использует бесплатный API: https://www.nager.at/api/v3/PublicHolidays
     */
    public List<Map<String, Object>> getTodayHolidays(String countryCode) {
        try {
            int year = LocalDate.now().getYear();
            String url = String.format(
                "https://date.nager.at/api/v3/PublicHolidays/%d/%s",
                year, countryCode
            );
            
            List<Map<String, Object>> holidays = restTemplate.getForObject(url, List.class);
            
            if (holidays == null) return List.of();
            
            // Фильтруем праздники на сегодня
            String today = LocalDate.now().toString();
            return holidays.stream()
                .filter(h -> today.equals(h.get("date")))
                .toList();
        } catch (Exception e) {
            log.warn("Error fetching holidays for {}: {}", countryCode, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Получает следующий ближайший праздник
     */
    public Map<String, Object> getNextHoliday(String countryCode) {
        try {
            int year = LocalDate.now().getYear();
            String url = String.format(
                "https://date.nager.at/api/v3/PublicHolidays/%d/%s",
                year, countryCode
            );
            
            List<Map<String, Object>> holidays = restTemplate.getForObject(url, List.class);
            
            if (holidays == null || holidays.isEmpty()) return null;
            
            String today = LocalDate.now().toString();
            return holidays.stream()
                .filter(h -> ((String) h.get("date")).compareTo(today) > 0)
                .findFirst()
                .orElse(null);
        } catch (Exception e) {
            log.warn("Error fetching next holiday for {}: {}", countryCode, e.getMessage());
            return null;
        }
    }
}
