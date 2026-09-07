package com.worldatlas.bot.service;

import com.worldatlas.bot.entity.CustomCity;
import com.worldatlas.bot.repository.CustomCityRepository;
import org.springframework.stereotype.Service;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CustomCityService {
    private final CustomCityRepository customCityRepository;
    private final CityService cityService;

    public CustomCityService(CustomCityRepository customCityRepository, CityService cityService) {
        this.customCityRepository = customCityRepository;
        this.cityService = cityService;
    }

    public boolean isNameTaken(String name) {
        return cityService.findCity(name) != null; // Проверяем только обычные города
    }

    public CustomCity createCustomCity(Long userId, String name, String timezone) {
        CustomCity city = new CustomCity();
        city.setUserId(userId);
        city.setName(name.toLowerCase());
        city.setTimezone(timezone);
        return customCityRepository.save(city);
    }

    public List<CustomCity> getUserCustomCities(Long userId) {
        return customCityRepository.findByUserId(userId);
    }

    public ZonedDateTime getTimeForCustomCity(CustomCity city) {
        try {
            int hours = Integer.parseInt(city.getTimezone().replace("+", "").replace("-", ""));
            if (city.getTimezone().startsWith("-")) hours = -hours;
            ZoneOffset offset = ZoneOffset.ofHours(hours);
            return ZonedDateTime.now(offset);
        } catch (Exception e) {
            return ZonedDateTime.now();
        }
    }

    public String getCustomCityInfo(CustomCity city, String lang) {
        ZonedDateTime now = getTimeForCustomCity(city);
        String timeStr = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        String dateStr = now.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        String name = city.getName().substring(0, 1).toUpperCase() + city.getName().substring(1);
        
        if ("en".equals(lang)) {
            return "🌍 <b>" + name + "</b> [Custom City]\n" +
                   "📍 User-defined city\n" +
                   "🕐 <b>" + timeStr + "</b> | 📅 " + dateStr + "\n" +
                   "🌐 UTC" + city.getTimezone();
        }
        return "🌍 <b>" + name + "</b> [Пользовательский город]\n" +
               "📍 Пользовательский город\n" +
               "🕐 <b>" + timeStr + "</b> | 📅 " + dateStr + "\n" +
               "🌐 UTC" + city.getTimezone();
    }
        public void deleteCustomCity(CustomCity city) {
        customCityRepository.delete(city);
    }

    public CustomCity findCustomCityByNameAndUserId(Long userId, String name) {
        List<CustomCity> cities = customCityRepository.findByUserId(userId);
        for (CustomCity cc : cities) {
            if (cc.getName().equalsIgnoreCase(name)) {
                return cc;
            }
        }
        return null;
    }

    public void updateCustomCity(CustomCity city) {
        customCityRepository.save(city);
    }

    public boolean isNameTakenByUser(Long userId, String name) {
        List<CustomCity> cities = customCityRepository.findByUserId(userId);
        for (CustomCity cc : cities) {
            if (cc.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
