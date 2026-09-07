package com.worldatlas.bot.service;

import com.worldatlas.bot.entity.User;
import com.worldatlas.bot.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findOrCreate(Long chatId, String username, String firstName, String lastName) {
        return userRepository.findById(chatId).map(user -> {
            user.setUsername(username);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            return userRepository.save(user);
        }).orElseGet(() -> {
            User newUser = new User(chatId, username, firstName, lastName, "ru", User.Role.USER, false);
            return userRepository.save(newUser);
        });
    }

    public User getUser(Long chatId) {
        return userRepository.findById(chatId).orElse(null);
    }

    public void setLanguage(Long chatId, String lang) {
        userRepository.findById(chatId).ifPresent(user -> {
            user.setLanguage(lang);
            userRepository.save(user);
        });
    }

    public void setRole(Long chatId, User.Role role) {
        userRepository.findById(chatId).ifPresent(user -> {
            user.setRole(role);
            userRepository.save(user);
        });
    }

    public void setBanned(Long chatId, boolean banned) {
        userRepository.findById(chatId).ifPresent(user -> {
            user.setBanned(banned);
            userRepository.save(user);
        });
    }

    public User findByUsername(String username) {
        if (username == null || username.isEmpty()) return null;
        return userRepository.findByUsernameIgnoreCase(username.replace("@", "")).orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Примечание: Если твой метод называется иначе (например, getFavoriteCities), измени название здесь и в WorldAtlasBot.java
    public List<String> getFavorites(Long chatId) {
        // Заглушка: замени на реальный вызов твоего репозитория избранного, если он есть
        // return favoriteRepository.findByChatId(chatId);
        return List.of(); 
    }
}