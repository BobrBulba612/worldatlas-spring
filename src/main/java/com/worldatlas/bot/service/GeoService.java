package com.worldatlas.bot.service;

import com.worldatlas.bot.entity.City;
import com.worldatlas.bot.repository.CityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Service
public class GeoService {
    
    private static final Logger log = LoggerFactory.getLogger(GeoService.class);
    
    @Value("${openweathermap.api.key:}")
    private String apiKey;
    
    private final CityRepository cityRepository;
    private final HttpClient httpClient;
    
    public GeoService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }
    
    /**
     * Ищет город через OpenWeatherMap Geo API
     * Возвращает список найденных городов
     */
    @Cacheable(value = "geoSearch", key = "#query.toLowerCase()")
    public List<City> searchCitiesOnline(String query) {
        if (query == null || query.length() < 2 || apiKey == null || apiKey.isEmpty()) {
            return List.of();
        }
        
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format(
                "http://api.openweathermap.org/geo/1.0/direct?q=%s&limit=5&appid=%s",
                encodedQuery, apiKey
            );
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log.warn("Geo API returned status: {}", response.statusCode());
                return List.of();
            }
            
            return parseGeoResponse(response.body());
        } catch (Exception e) {
            log.error("Geo search failed for '{}': {}", query, e.getMessage());
            return List.of();
        }
    }
    
    private List<City> parseGeoResponse(String json) {
        List<City> cities = new ArrayList<>();
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            List<Map<String, Object>> results = mapper.readValue(json, 
                mapper.getTypeFactory().constructCollectionType(List.class, Map.class));
            
            for (Map<String, Object> result : results) {
                City city = new City();
                String name = (String) result.get("name");
                String country = (String) result.get("country");
                Double lat = ((Number) result.get("lat")).doubleValue();
                Double lon = ((Number) result.get("lon")).doubleValue();
                
                // Получаем часовой пояс из координат
                String timezone = getTimezoneFromCoords(lat, lon);
                
                city.setName(name.toLowerCase());
                city.setCountry(country);
                city.setTimezone(timezone);
                
                cities.add(city);
            }
        } catch (Exception e) {
            log.error("Failed to parse geo response: {}", e.getMessage());
        }
        
        return cities;
    }
    
    private String getTimezoneFromCoords(double lat, double lon) {
        try {
            // Используем бесплатный API для получения часового пояса
            String url = String.format(
                "https://timeapi.io/api/Time/current/coordinate?latitude=%f&longitude=%f",
                lat, lon
            );
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> data = mapper.readValue(response.body(), Map.class);
                String timeZone = (String) data.get("timeZone");
                if (timeZone != null && !timeZone.isEmpty()) {
                    return timeZone;
                }
            }
        } catch (Exception e) {
            log.debug("Failed to get timezone for {},{}: {}", lat, lon, e.getMessage());
        }
        
        // Fallback: вычисляем примерный часовой пояс по долготе
        int offsetHours = (int) Math.round(lon / 15.0);
        return String.format("Etc/GMT%+d", -offsetHours);
    }
    
    /**
     * Сохраняет найденный город в БД для будущего использования
     */
    public City saveCityIfNotExists(City city) {
        try {
            Optional<City> existing = cityRepository.findById(city.getName());
            if (existing.isPresent()) {
                return existing.get();
            }
            return cityRepository.save(city);
        } catch (Exception e) {
            log.error("Failed to save city {}: {}", city.getName(), e.getMessage());
            return city;
        }
    }
}
