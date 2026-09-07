package com.worldatlas.bot.config;

import com.worldatlas.bot.bot.WorldAtlasBot;
import com.worldatlas.bot.service.CityService;
import com.worldatlas.bot.service.CustomCityService;
import com.worldatlas.bot.service.ReminderService;
import com.worldatlas.bot.service.LocalizationService;
import com.worldatlas.bot.service.TimeService;
import com.worldatlas.bot.service.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotConfig {
    private static WorldAtlasBot botInstance;
    
    public static WorldAtlasBot getBot() {
        return botInstance;
    }
    
    @Value("${telegram.bot.username}") private String botUsername;
    @Value("${telegram.bot.token}") private String botToken;
    
    private final UserService userService;
    private final CityService cityService;
    private final LocalizationService localization;
    private final TimeService timeService;
    private final CustomCityService customCityService;
    private final ReminderService reminderService;

    public BotConfig(UserService userService, CityService cityService, LocalizationService localization, TimeService timeService, CustomCityService customCityService, ReminderService reminderService) {
        this.userService = userService;
        this.cityService = cityService;
        this.localization = localization;
        this.timeService = timeService;
        this.customCityService = customCityService;
        this.reminderService = reminderService;
    }

    @PostConstruct
    public void init() {
        System.out.println("🔥 НАЧАЛО РЕГИСТРАЦИИ БОТА...");
        
        System.setProperty("socksProxyHost", "10.25.24.1");
        System.setProperty("socksProxyPort", "1080");
        
        try {
            DefaultBotOptions options = new DefaultBotOptions();
            options.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
            options.setProxyHost("10.25.24.1");
            options.setProxyPort(1080);
            
            WorldAtlasBot bot = new WorldAtlasBot(options, userService, cityService, localization, timeService, customCityService, reminderService, botUsername, botToken);
            botInstance = bot;
            
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            
            System.out.println("✅ Бот успешно зарегистрирован!");
        } catch (Exception e) {
            System.err.println("❌ Ошибка регистрации бота: " + e.getMessage());
            e.printStackTrace();
        }
    }
}