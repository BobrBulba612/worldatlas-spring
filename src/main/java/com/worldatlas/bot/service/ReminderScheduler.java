package com.worldatlas.bot.service;

import com.worldatlas.bot.bot.WorldAtlasBot;
import com.worldatlas.bot.config.BotConfig;
import com.worldatlas.bot.entity.City;
import com.worldatlas.bot.entity.Reminder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class ReminderScheduler {
    
    @Autowired
    private ReminderService reminderService;
    
    @Autowired
    private CityService cityService;
    
    private static final Set<String> VALID_DAYS = new HashSet<>();
    static {
        // Русские короткие
        VALID_DAYS.add("пн"); VALID_DAYS.add("вт"); VALID_DAYS.add("ср");
        VALID_DAYS.add("чт"); VALID_DAYS.add("пт"); VALID_DAYS.add("сб"); VALID_DAYS.add("вс");
        // Русские полные
        VALID_DAYS.add("понедельник"); VALID_DAYS.add("вторник"); VALID_DAYS.add("среда");
        VALID_DAYS.add("четверг"); VALID_DAYS.add("пятница"); VALID_DAYS.add("суббота"); VALID_DAYS.add("воскресенье");
        // Английские короткие и вариации
        VALID_DAYS.add("mon"); VALID_DAYS.add("tue"); VALID_DAYS.add("tues");
        VALID_DAYS.add("wed"); VALID_DAYS.add("thu"); VALID_DAYS.add("thur"); VALID_DAYS.add("thurs");
        VALID_DAYS.add("fri"); VALID_DAYS.add("sat"); VALID_DAYS.add("sun");
        // Английские полные
        VALID_DAYS.add("monday"); VALID_DAYS.add("tuesday"); VALID_DAYS.add("wednesday");
        VALID_DAYS.add("thursday"); VALID_DAYS.add("friday"); VALID_DAYS.add("saturday"); VALID_DAYS.add("sunday");
        // Специальные
        VALID_DAYS.add("all"); VALID_DAYS.add("все"); VALID_DAYS.add("every"); VALID_DAYS.add("каждый");
    }
    
    @Scheduled(fixedRate = 60000)
    public void checkReminders() {
        WorldAtlasBot bot = BotConfig.getBot();
        if (bot == null) {
            System.out.println("❌ Бот не инициализирован");
            return;
        }
        
        List<Reminder> reminders = reminderService.getAllReminders();
        
        for (Reminder reminder : reminders) {
            try {
                City city = cityService.findCity(reminder.getCityName());
                if (city == null) {
                    System.out.println("❌ Город не найден: " + reminder.getCityName());
                    continue;
                }
                
                ZonedDateTime cityTime = ZonedDateTime.now(ZoneId.of(city.getTimezone()));
                String currentTime = cityTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                String currentDay = cityTime.getDayOfWeek().toString().toLowerCase().substring(0, 3);
                String reminderTime = reminder.getTime();
                
                System.out.println("=== REMINDER CHECK ===");
                System.out.println("Chat ID: " + reminder.getChatId());
                System.out.println("City: " + reminder.getCityName() + " | Timezone: " + city.getTimezone());
                System.out.println("Current time: " + currentTime + " | Current day: " + currentDay);
                System.out.println("Reminder time: " + reminderTime + " | Reminder days: " + reminder.getDays());
                
                if (currentTime.equals(reminderTime)) {
                    System.out.println("✅ Время совпало!");
                    if (checkDays(reminder.getDays(), currentDay)) {
                        System.out.println("✅ День совпал! Отправляем напоминание...");
                        try {
                            SendMessage msg = new SendMessage();
                            msg.setChatId(reminder.getChatId());
                            msg.setText("⏰ <b>Напоминание!</b>\n\n" + reminder.getText());
                            msg.setParseMode("HTML");
                            bot.execute(msg);
                            System.out.println("✅ Напоминание отправлено для " + reminder.getChatId());
                        } catch (Exception e) {
                            System.out.println("❌ Ошибка отправки: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("❌ День не совпал");
                    }
                }
            } catch (Exception e) {
                System.out.println("❌ Ошибка обработки напоминания: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public static boolean validateDays(String days) {
        if (days == null || days.isEmpty()) return false;
        
        String lowerDays = days.toLowerCase().trim();
        String[] dayParts = lowerDays.split("[,\\s]+");
        
        for (String day : dayParts) {
            day = day.trim();
            if (day.isEmpty()) continue;
            
            if (!VALID_DAYS.contains(day)) {
                return false;
            }
        }
        
        return dayParts.length > 0;
    }
    
    private boolean checkDays(String days, String currentDay) {
        if (days == null || days.isEmpty()) return false;
        
        String lowerDays = days.toLowerCase().trim();
        
        if (lowerDays.equals("all") || lowerDays.equals("все") || lowerDays.equals("every") || lowerDays.equals("каждый")) {
            return true;
        }
        
        String[] dayParts = lowerDays.split("[,\\s]+");
        
        for (String day : dayParts) {
            day = day.trim();
            if (day.isEmpty()) continue;
            
            String englishDay = convertToEnglishDay(day);
            
            if (englishDay.equals(currentDay)) {
                return true;
            }
        }
        
        return false;
    }
    
    private String convertToEnglishDay(String day) {
        String lowerDay = day.toLowerCase().trim();
        
        // Русские короткие
        if (lowerDay.equals("пн")) return "mon";
        if (lowerDay.equals("вт")) return "tue";
        if (lowerDay.equals("ср")) return "wed";
        if (lowerDay.equals("чт")) return "thu";
        if (lowerDay.equals("пт")) return "fri";
        if (lowerDay.equals("сб")) return "sat";
        if (lowerDay.equals("вс")) return "sun";
        
        // Русские полные
        if (lowerDay.equals("понедельник")) return "mon";
        if (lowerDay.equals("вторник")) return "tue";
        if (lowerDay.equals("среда")) return "wed";
        if (lowerDay.equals("четверг")) return "thu";
        if (lowerDay.equals("пятница")) return "fri";
        if (lowerDay.equals("суббота")) return "sat";
        if (lowerDay.equals("воскресенье")) return "sun";
        
        // Английские короткие и вариации
        if (lowerDay.equals("mon") || lowerDay.equals("monday")) return "mon";
        if (lowerDay.equals("tue") || lowerDay.equals("tues") || lowerDay.equals("tuesday")) return "tue";
        if (lowerDay.equals("wed") || lowerDay.equals("wednesday")) return "wed";
        if (lowerDay.equals("thu") || lowerDay.equals("thur") || lowerDay.equals("thurs") || lowerDay.equals("thursday")) return "thu";
        if (lowerDay.equals("fri") || lowerDay.equals("friday")) return "fri";
        if (lowerDay.equals("sat") || lowerDay.equals("saturday")) return "sat";
        if (lowerDay.equals("sun") || lowerDay.equals("sunday")) return "sun";
        
        return lowerDay;
    }
}
