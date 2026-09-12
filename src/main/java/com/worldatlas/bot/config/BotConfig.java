package com.worldatlas.bot.config;

import com.worldatlas.bot.bot.WorldAtlasBot;
import com.worldatlas.bot.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotConfig {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    private static WorldAtlasBot botInstance;

    @Bean
    public WorldAtlasBot worldAtlasBot(UserService userService,
                                       CityService cityService,
                                       LocalizationService localization,
                                       TimeService timeService,
                                       CustomCityService customCityService,
                                       ReminderService reminderService, SupportService supportService) {
        try {
            DefaultBotOptions options = new DefaultBotOptions();
            System.out.println("ℹ️ Работаем без прокси (прямое подключение)");

            WorldAtlasBot bot = new WorldAtlasBot(options, userService, cityService, localization, timeService, customCityService, reminderService, supportService, botUsername, botToken);
            botInstance = bot;

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);

            System.out.println("✅ Бот успешно зарегистрирован!");
        } catch (Exception e) {
            System.err.println("❌ Ошибка регистрации бота: " + e.getMessage());
            e.printStackTrace();
        }
        return botInstance;
    }

    public static WorldAtlasBot getBot() {
        return botInstance;
    }
}
