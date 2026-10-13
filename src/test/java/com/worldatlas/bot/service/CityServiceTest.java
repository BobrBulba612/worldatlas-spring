package com.worldatlas.bot.service;

import com.worldatlas.bot.entity.City;
import com.worldatlas.bot.repository.CityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {
    
    @Mock
    private CityRepository cityRepository;
    
    @Mock
    private WeatherService weatherService;
    
    @Mock
    private HolidayService holidayService;
    
    private CityService cityService;
    
    @BeforeEach
    void setUp() {
        cityService = new CityService(cityRepository, weatherService, holidayService);
    }
    
    @Test
    void findCity_ExistingCity_ReturnsCity() {
        // Arrange
        City moscow = new City();
        moscow.setName("москва");
        moscow.setTimezone("Europe/Moscow");
        
        // "Moscow" -> "moscow" -> "москва" через EN_TO_RU маппинг
        when(cityRepository.findById("москва")).thenReturn(Optional.of(moscow));
        
        // Act
        City result = cityService.findCity("Moscow");
        
        // Assert
        assertNotNull(result);
        assertEquals("москва", result.getName());
        verify(cityRepository, times(1)).findById("москва");
    }
    
    @Test
    void findCity_NonExistingCity_ReturnsNull() {
        // Arrange
        when(cityRepository.findById("unknown")).thenReturn(Optional.empty());
        
        // Act
        City result = cityService.findCity("unknown");
        
        // Assert
        assertNull(result);
        verify(cityRepository, times(1)).findById("unknown");
    }
    
    @Test
    void findCity_NullInput_ReturnsNull() {
        // Act
        City result = cityService.findCity(null);
        
        // Assert
        assertNull(result);
        verify(cityRepository, never()).findById(any());
    }
    
    @Test
    void searchCities_WithQuery_ReturnsResults() {
        // Arrange
        City moscow = new City();
        moscow.setName("москва");
        
        when(cityRepository.findByNameContainingIgnoreCase("mos")).thenReturn(List.of(moscow));
        
        // Act
        List<City> results = cityService.searchCities("mos");
        
        // Assert
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        verify(cityRepository, times(1)).findByNameContainingIgnoreCase("mos");
    }
    
    @Test
    void searchCities_TooShortQuery_ReturnsEmpty() {
        // Act
        List<City> results = cityService.searchCities("a");
        
        // Assert
        assertTrue(results.isEmpty());
        verify(cityRepository, never()).findByNameContainingIgnoreCase(any());
    }
}
