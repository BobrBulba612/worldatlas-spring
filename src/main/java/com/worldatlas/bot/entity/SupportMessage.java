package com.worldatlas.bot.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "support_messages")
public class SupportMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long chatId;
    private String username;
    private String text;
    
    @Column(length = 2000)
    private String adminReply;
    
    private boolean answered = false;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime answeredAt;
}
