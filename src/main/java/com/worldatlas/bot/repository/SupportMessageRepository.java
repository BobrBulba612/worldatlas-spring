package com.worldatlas.bot.repository;

import com.worldatlas.bot.entity.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {
    List<SupportMessage> findByChatIdOrderByCreatedAtDesc(Long chatId);
    int countByChatIdAndCreatedAtAfter(Long chatId, LocalDateTime after);
    List<SupportMessage> findByAnsweredFalseOrderByCreatedAtAsc();
    List<SupportMessage> findAllByOrderByCreatedAtDesc();
}
