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
    private static volatile boolean botStarted = false;
    private static final Object startLock = new Object();

    @Bean
    public WorldAtlasBot worldAtlasBot(UserService userService,
                                       CityService cityService,
                                       LocalizationService localization,
                                       TimeService timeService,
                                       CustomCityService customCityService,
                                       ReminderService reminderService, SupportService supportService,
                                       com.worldatlas.bot.service.TelegramRetryService retryService,
                                       com.worldatlas.bot.service.RateLimitService rateLimitService,
                                       com.worldatlas.bot.service.ErrorNotifier errorNotifier) {
        // Защита от запуска нескольких экземпляров бота
        synchronized (startLock) {
            if (botStarted) {
                System.err.println("⚠️ Бот уже запущен! Пропускаем повторную инициализацию.");
                return botInstance;
            }
            botStarted = true;
        }

        try {
            DefaultBotOptions options = new DefaultBotOptions();
            System.out.println("ℹ️ Работаем без прокси (прямое подключение)");

            WorldAtlasBot bot = new WorldAtlasBot(options, userService, cityService, localization, timeService, customCityService, reminderService, supportService, retryService, rateLimitService, errorNotifier, botUsername, botToken);
            botInstance = bot;

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);

            System.out.println("✅ Бот успешно зарегистрирован!");
        } catch (Exception e) {
            botStarted = false; // Сбрасываем флаг при ошибке
            System.err.println("❌ Ошибка регистрации бота: " + e.getMessage());
            e.printStackTrace();
        }
        return botInstance;
    }

    public static WorldAtlasBot getBot() {
        return botInstance;
    }
}
