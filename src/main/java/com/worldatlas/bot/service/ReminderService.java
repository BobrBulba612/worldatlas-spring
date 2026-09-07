package com.worldatlas.bot.service;

import com.worldatlas.bot.entity.Reminder;
import com.worldatlas.bot.repository.ReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ReminderService {
    
    @Autowired
    private ReminderRepository reminderRepository;
    
    public boolean hasReminder(Long chatId) {
        return reminderRepository.findByChatId(chatId).isPresent();
    }
    
    public void createReminder(Long chatId, String cityName, String time, String text, String days) {
        System.out.println("=== CREATING REMINDER ===");
        System.out.println("Chat ID: " + chatId);
        System.out.println("City: " + cityName);
        System.out.println("Time: " + time);
        System.out.println("Text: " + text);
        System.out.println("Days: " + days);
        
        Reminder reminder = new Reminder();
        reminder.setChatId(chatId);
        reminder.setCityName(cityName);
        reminder.setTime(time);
        reminder.setText(text);
        reminder.setDays(days);
        
        try {
            Reminder saved = reminderRepository.save(reminder);
            System.out.println("✅ Reminder saved with ID: " + saved.getId());
            System.out.println("Total reminders in DB: " + reminderRepository.findAll().size());
        } catch (Exception e) {
            System.out.println("❌ Error saving reminder: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Transactional
    public void deleteReminder(Long chatId) {
        reminderRepository.deleteByChatId(chatId);
        System.out.println("🗑️ Reminder deleted for chatId: " + chatId);
    }
    
    public Optional<Reminder> getReminder(Long chatId) {
        return reminderRepository.findByChatId(chatId);
    }
    
    public List<Reminder> getAllReminders() {
        List<Reminder> reminders = reminderRepository.findAll();
        System.out.println("📋 getAllReminders() returned " + reminders.size() + " reminders");
        return reminders;
    }
}
