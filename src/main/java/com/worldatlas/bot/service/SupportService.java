package com.worldatlas.bot.service;

import com.worldatlas.bot.entity.SupportMessage;
import com.worldatlas.bot.entity.User;
import com.worldatlas.bot.repository.SupportMessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SupportService {
    
    private final SupportMessageRepository repository;
    
    public SupportService(SupportMessageRepository repository) {
        this.repository = repository;
    }
    
    public boolean canSendMessage(Long chatId) {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        int recentCount = repository.countByChatIdAndCreatedAtAfter(chatId, oneMinuteAgo);
        return recentCount == 0;
    }
    
    public SupportMessage createMessage(Long chatId, String username, String text) {
        SupportMessage msg = new SupportMessage();
        msg.setChatId(chatId);
        msg.setUsername(username != null ? username : "unknown");
        msg.setText(text);
        return repository.save(msg);
    }
    
    public SupportMessage answerMessage(Long messageId, String reply) {
        return repository.findById(messageId).map(msg -> {
            msg.setAdminReply(reply);
            msg.setAnswered(true);
            msg.setAnsweredAt(LocalDateTime.now());
            return repository.save(msg);
        }).orElse(null);
    }
    
    public List<SupportMessage> getUserMessages(Long chatId) {
        return repository.findByChatIdOrderByCreatedAtDesc(chatId);
    }
    
    public List<SupportMessage> getUnanswered() {
        return repository.findByAnsweredFalseOrderByCreatedAtAsc();
    }
    
    public SupportMessage getById(Long id) {
        return repository.findById(id).orElse(null);
    }
}
