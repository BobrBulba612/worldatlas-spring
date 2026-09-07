package com.worldatlas.bot.repository;

import com.worldatlas.bot.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    Optional<Reminder> findByChatId(Long chatId);
    void deleteByChatId(Long chatId);
}
