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
@Table(name = "support_messages", indexes = {
    @Index(name = "idx_support_chat_id", columnList = "chatId"),
    @Index(name = "idx_support_status", columnList = "status")
})
public class SupportMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long chatId;
    private String username;
    private String text;
    
    @Column(length = 2000)
    private String adminReply;
    
    @Enumerated(EnumType.STRING)
    private TicketStatus status = TicketStatus.NEW;
    
    private boolean answered = false;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime answeredAt;
    
    public enum TicketStatus {
        NEW,           // 🆕 Новое
        IN_PROGRESS,   // ⏳ В процессе
        RESOLVED       // ✅ Решено
    }
}
