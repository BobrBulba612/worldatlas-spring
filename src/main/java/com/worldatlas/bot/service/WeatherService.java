package com.worldatlas.bot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

@Service
public class WeatherService {
    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);
    
    @Value("${openweathermap.api.key:}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    
    public WeatherService() {
        this.restTemplate = new RestTemplate();
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = 
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);  // 3 секунды на подключение
        factory.setReadTimeout(3000);     // 3 секунды на чтение
        this.restTemplate.setRequestFactory(factory);
    }
    
    public Map<String, Object> getWeather(String cityName) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_API_KEY_HERE")) {
            log.debug("OpenWeatherMap API key not configured, skipping weather");
            return null;
        }
        
        try {
            String url = String.format(
                "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric",
                cityName, apiKey
            );
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response == null) return null;
            
            Map<String, Object> weather = new HashMap<>();
            
            Map<String, Object> main = (Map<String, Object>) response.get("main");
            weather.put("temp", main.get("temp"));
            weather.put("feelsLike", main.get("feels_like"));
            weather.put("humidity", main.get("humidity"));
            
            Map<String, Object> weatherInfo = (Map<String, Object>) ((java.util.List<Map<String, Object>>) response.get("weather")).get(0);
            weather.put("description", weatherInfo.get("description"));
            weather.put("icon", weatherInfo.get("icon"));
            
            Map<String, Object> wind = (Map<String, Object>) response.get("wind");
            weather.put("windSpeed", wind.get("speed"));
            
            Map<String, Object> sys = (Map<String, Object>) response.get("sys");
            weather.put("sunrise", sys.get("sunrise"));
            weather.put("sunset", sys.get("sunset"));
            
            return weather;
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("401")) {
                log.error("Invalid OpenWeatherMap API key. Get one at https://openweathermap.org/api");
            } else {
                log.debug("Weather unavailable for {}: {}", cityName, e.getMessage());
            }
            return null;
        }
    }
    
    public String getWeatherEmoji(String description) {
        if (description == null) return "🌤️";
        description = description.toLowerCase();
        
        if (description.contains("clear")) return "☀️";
        if (description.contains("cloud")) return "☁️";
        if (description.contains("rain")) return "🌧️";
        if (description.contains("drizzle")) return "🌦️";
        if (description.contains("thunderstorm")) return "⛈️";
        if (description.contains("snow")) return "🌨️";
        if (description.contains("mist") || description.contains("fog")) return "🌫️";
        
        return "🌤️";
    }
}
