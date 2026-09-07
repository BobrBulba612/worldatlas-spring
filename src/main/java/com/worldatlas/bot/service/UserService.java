package com.worldatlas.bot.service;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;
import com.worldatlas.bot.entity.User;
import com.worldatlas.bot.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class UserService {
    public static final Long MAIN_ADMIN_ID = 1319065617L;
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
        }).orElseGet(() -> userRepository.save(new User(chatId, username, firstName, lastName, "ru", "24", null, false, User.Role.USER, false, new HashSet<>())));
    }

    public User getUser(Long chatId) {
        return userRepository.findById(chatId).orElse(null);
    }

    public void setLanguage(Long chatId, String lang) {
        userRepository.findById(chatId).ifPresent(u -> {
            u.setLanguage(lang);
            userRepository.save(u);
        });
    }

    public void setRole(Long chatId, User.Role role) {
        userRepository.findById(chatId).ifPresent(u -> {
            u.setRole(role);
            userRepository.save(u);
        });
    }

    public void setBanned(Long chatId, boolean banned) {
        userRepository.findById(chatId).ifPresent(u -> {
            u.setBanned(banned);
            userRepository.save(u);
        });
    }

    public User findByUsername(String username) {
        if (username == null || username.isEmpty()) return null;
        return userRepository.findByUsernameIgnoreCase(username.replace("@", "")).orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<String> getFavorites(Long chatId) {
        return userRepository.findById(chatId)
            .map(u -> new ArrayList<>(u.getFavorites()))
            .orElse(new ArrayList<>());
    }

    public boolean hasFavorite(Long chatId, String cityName) {
        return userRepository.findById(chatId).map(user -> {
            Set<String> favs = user.getFavorites();
            return favs != null && favs.contains(cityName.toLowerCase());
        }).orElse(false);
    }

    @Transactional
    public void addFavorite(Long chatId, String cityName) {
        userRepository.findById(chatId).ifPresent(user -> {
            user.getFavorites().add(cityName.toLowerCase());
            userRepository.save(user);
        });
    }
    @Transactional
    public void removeFavorite(Long chatId, String cityName) {
        userRepository.findById(chatId).ifPresent(user -> {
            user.getFavorites().remove(cityName.toLowerCase());
            userRepository.save(user);
        });
    }

    public void setTimeFormat(Long chatId, String format) {
        userRepository.findById(chatId).ifPresent(user -> {
            user.setTimeFormat(format);
            userRepository.save(user);
        });
    }

    public void setHomeCity(Long chatId, String cityName) {
        userRepository.findById(chatId).ifPresent(user -> {
            user.setHomeCity(cityName);
            userRepository.save(user);
        });
    }

    public String getHomeCity(Long chatId) {
        return userRepository.findById(chatId).map(User::getHomeCity).orElse(null);
    }

    public void removeHomeCity(Long chatId) {
        userRepository.findById(chatId).ifPresent(user -> {
            user.setHomeCity(null);
            userRepository.save(user);
        });
    }

    public void setSubscribed(Long chatId, boolean subscribed) {
        userRepository.findById(chatId).ifPresent(user -> {
            user.setSubscribed(subscribed);
            userRepository.save(user);
        });
    }

    public boolean isSubscribed(Long chatId) {
        return userRepository.findById(chatId).map(User::isSubscribed).orElse(false);
    }
}
