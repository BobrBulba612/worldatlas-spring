package com.worldatlas.bot.config;

import com.worldatlas.bot.bot.WorldAtlasBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.menubutton.SetChatMenuButton;
import org.telegram.telegrambots.meta.api.objects.menubutton.MenuButtonWebApp;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;

import java.lang.reflect.Constructor;

@Component
public class MenuButtonRunner implements ApplicationRunner {

    private final WorldAtlasBot bot;

    @Value("${webapp.url:}")
    private String webAppUrl;

    public MenuButtonRunner(WorldAtlasBot bot) {
        this.bot = bot;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (webAppUrl == null || webAppUrl.isEmpty()) {
            System.out.println("ℹ️ webapp.url не задан — кнопка меню не устанавливается");
            return;
        }
        try {
            SetChatMenuButton cmd = new SetChatMenuButton();
            Constructor<MenuButtonWebApp> constructor = MenuButtonWebApp.class.getDeclaredConstructor(String.class, WebAppInfo.class);
            constructor.setAccessible(true);
            MenuButtonWebApp menuButton = constructor.newInstance("🌍 Mini App", new WebAppInfo(webAppUrl));
            cmd.setMenuButton(menuButton);
            bot.execute(cmd);
            System.out.println("✅ Кнопка Mini App установлена: " + webAppUrl);
        } catch (Exception e) {
            System.out.println("❌ Ошибка установки кнопки меню: " + e.getMessage());
        }
    }
}
