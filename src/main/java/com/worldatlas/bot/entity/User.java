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
@Table(name = "users")
public class User {
    
    @Id
    private Long chatId;
    private String username;
    private String firstName;
    private String lastName;
    private String language = "ru";
    private String timeFormat = "24";
    private String homeCity;
    private boolean subscribed = false;
    
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
    
    private boolean banned = false;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_favorites", joinColumns = @JoinColumn(name = "chat_id"))
    @Column(name = "city_name")
    private Set<String> favorites = new HashSet<>();
    
    public enum Role { USER, MODERATOR, ADMIN, OWNER }
}
