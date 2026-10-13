package com.worldatlas.bot.service;

import com.worldatlas.bot.entity.User;
import com.worldatlas.bot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    private UserService userService;
    
    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }
    
    @Test
    void findOrCreate_ExistingUser_UpdatesUser() {
        // Arrange
        Long chatId = 123L;
        User existingUser = new User();
        existingUser.setChatId(chatId);
        existingUser.setLanguage("ru");
        
        when(userRepository.findById(chatId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        
        // Act
        User result = userService.findOrCreate(chatId, "testuser", "Test", "User");
        
        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("Test", result.getFirstName());
        verify(userRepository, times(1)).findById(chatId);
        verify(userRepository, times(1)).save(existingUser);
    }
    
    @Test
    void findOrCreate_NewUser_CreatesUser() {
        // Arrange
        Long chatId = 456L;
        when(userRepository.findById(chatId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        
        // Act
        User result = userService.findOrCreate(chatId, "newuser", "New", "User");
        
        // Assert
        assertNotNull(result);
        assertEquals(chatId, result.getChatId());
        assertEquals("newuser", result.getUsername());
        verify(userRepository, times(1)).findById(chatId);
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void getUser_ExistingUser_ReturnsUser() {
        // Arrange
        Long chatId = 123L;
        User user = new User();
        user.setChatId(chatId);
        user.setLanguage("ru");
        
        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));
        
        // Act
        User result = userService.getUser(chatId);
        
        // Assert
        assertNotNull(result);
        assertEquals(chatId, result.getChatId());
        verify(userRepository, times(1)).findById(chatId);
    }
    
    @Test
    void getUser_NonExistingUser_ReturnsNull() {
        // Arrange
        Long chatId = 999L;
        when(userRepository.findById(chatId)).thenReturn(Optional.empty());
        
        // Act
        User result = userService.getUser(chatId);
        
        // Assert
        assertNull(result);
        verify(userRepository, times(1)).findById(chatId);
    }
}
