package com.worldatlas.bot.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_chat_id", columnList = "chatId"),
    @Index(name = "idx_users_username", columnList = "username")
})
public class User {
    
    @Id
    private Long chatId;
    private String username;
    private String firstName;
    private String lastName;
    private String language = "ru";
    private String timeFormat = "24";
    private String homeCity;

    @Column(name = "show_weather")
    private Boolean showWeather = true;

    @Column(name = "show_sunrise_sunset")
    private Boolean showSunriseSunset = true;
    private boolean subscribed = false;
    
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
    
    private boolean banned = false;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_favorites", joinColumns = @JoinColumn(name = "chat_id"))
    @Column(name = "city_name")
    private Set<String> favorites = new HashSet<>();
    
    public enum Role { USER, MODERATOR, ADMIN, OWNER }


    public Boolean getShowWeather() {
        return showWeather;
    }

    public void setShowWeather(Boolean showWeather) {
        this.showWeather = showWeather;
    }

    public Boolean getShowSunriseSunset() {
        return showSunriseSunset;
    }

    public void setShowSunriseSunset(Boolean showSunriseSunset) {
        this.showSunriseSunset = showSunriseSunset;
    }

}
