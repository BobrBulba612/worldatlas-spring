package com.worldatlas.bot.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class LocalizationService {
    
    private final Map<String, Map<String, String>> translations = new HashMap<>();
    
    public LocalizationService() {
        // ========== РУССКИЙ ЯЗЫК ==========
        Map<String, String> ru = new HashMap<>();
        ru.put("not_found", "❌ Такого города нет в базе данных.");
        ru.put("added", "✅ %s добавлен в ваше избранное!");
        ru.put("already", "⚠️ Этот город уже в вашем избранном.");
        ru.put("removed", "✅ Удалено из избранного.");
        ru.put("not_in_fav", "❌ Не найдено в избранном.");
        ru.put("empty_fav", "📭 У вас пока нет избранных городов.");
        ru.put("enter_city", "🌍 Введите название города:");
        ru.put("canceled", "⏹ Отменено.");
        ru.put("admin_only", "🔐 Эта команда доступна только администраторам и модераторам.");
        ru.put("moderator_only", "🔐 Эта команда доступна только модераторам и администраторам.");
        ru.put("user_not_found", "❌ Пользователь с таким username не найден.");
        ru.put("lang_switched", "✅ Язык переключен на русский.");
        ru.put("fav_title", "⭐ Ваше избранное (%d)");
        ru.put("broadcast_request", "📢 Напиши текст объявления для всех пользователей:");
        ru.put("broadcast_started", "⏳ Рассылка запущена в фоновом режиме.");
        ru.put("broadcast_done", "✅ Рассылка завершена! Отправлено: %d из %d");
        
        ru.put("help_text", "📖 <b>Помощь по боту WorldAtlas</b>\n\n" +
            "<b>Основные команды:</b>\n" +
            "• /start - Запустить бота\n" +
            "• /search_city - Найти город\n" +
            "• /view_favorites - Избранные города\n" +
            "• /download - Скачать приложение\n" +
            "• /lang - Сменить язык\n" +
            "• /help - Эта справка\n" +
            "• /cancel - Отменить действие\n\n" +
            "<b>Inline режим:</b>\n" +
            "Используйте @WorldTimeMapBot в любом чате для поиска городов");
            
        ru.put("help_admin_text", "🛡️ <b>Админ-панель и команды управления</b>\n\n" +
            "• /users - Список всех пользователей\n" +
            "• /broadcast или /announce - Рассылка сообщения всем\n" +
            "• /ban @username - Заблокировать пользователя в боте\n" +
            "• /unban @username - Разблокировать пользователя\n" +
            "• /kick @username - Кикнуть пользователя (бан + очистка избранного)\n" +
            "• /help admin - Показать это сообщение");

        ru.put("download_title", "📥 <b>Скачать приложение WorldAtlas</b>\n\nВыберите вашу операционную систему:");
        ru.put("download_win", "💻 <b>Скачать для Windows</b>\n\n📥 <a href=\"https://drive.google.com/file/d/1H_-f3yTqhGvstVwLw37paltGU5TQHgWD/view\">Скачать файл</a>\n\n<b>Инструкция:</b>\n1. Нажмите на ссылку выше\n2. Нажмите кнопку Скачать\n3. Откройте скачatiй файл и следуйте инструкциям установщика");
        ru.put("download_android", "📱 <b>Скачать для Android</b>\n\n📥 <a href=\"https://drive.google.com/file/d/1cHzF9ba-91bKSv_3G7Wn7-fEfNPDHETR/view\">Скачать APK</a>\n\n<b>Инструкция:</b>\n1. Нажмите на ссылку выше\n2. Разрешите установку из неизвестных источников в настройках\n3. Откройте скачанный APK файл и нажмите Установить");
        ru.put("download_linux", "🐧 <b>Скачать для Linux</b>\n\n📥 <a href=\"https://drive.google.com/file/d/1F2324DmMfWR2UIQGD-sSMLliGevMKXsi/view\">Скачать файл</a>\n\n<b>Инструкция:</b>\n1. Скачайте файл\n2. Откройте терминал в папке с файлом\n3. Сделайте исполняемым: <code>chmod +x worldatlas-linux</code>\n4. Запустите: <code>./worldatlas-linux</code>");
        ru.put("back", "⬅️ Назад");
        ru.put("banned_msg", "🚫 Вы заблокированы в этом боте. Обратитесь к администратору.");
        ru.put("action_notify", "🚨 <b>Уведомление о действии</b>\n\n👤 Админ/Модератор: %s (ID: %d)\n🎯 Цель: %s (ID: %d)\n⚡ Действие: %s");
        ru.put("ban_success", "✅ Пользователь %s успешно заблокирован.");
        ru.put("unban_success", "✅ Пользователь %s успешно разблокирован.");
        ru.put("kick_success", "✅ Пользователь %s кикнут (заблокирован и очищен).");
        ru.put("cannot_ban_admin", "❌ Нельзя заблокировать администратора!");

        // ========== АНГЛИЙСКИЙ ЯЗЫК ==========
        Map<String, String> en = new HashMap<>();
        en.put("not_found", "❌ City not found in database.");
        en.put("added", "✅ %s added to your favorites!");
        en.put("already", "⚠️ Already in favorites.");
        en.put("removed", "✅ Removed from favorites.");
        en.put("not_in_fav", "❌ Not found in favorites.");
        en.put("empty_fav", "📭 You have no favorite cities yet.");
        en.put("enter_city", "🌍 Enter city name:");
        en.put("canceled", "⏹ Canceled.");
        en.put("admin_only", "🔐 This command is for administrators and moderators only.");
        en.put("moderator_only", "🔐 This command is for moderators and administrators only.");
        en.put("user_not_found", "❌ User with this username not found.");
        en.put("lang_switched", "✅ Language switched to English.");
        en.put("fav_title", "⭐ Your favorites (%d)");
        en.put("broadcast_request", "📢 Enter broadcast message text for all users:");
        en.put("broadcast_started", "⏳ Broadcast started in background.");
        en.put("broadcast_done", "✅ Broadcast complete! Sent: %d of %d");
        
        en.put("help_text", "📖 <b>WorldAtlas Bot Help</b>\n\n" +
            "<b>Main commands:</b>\n" +
            "• /start - Start the bot\n" +
            "• /search_city - Find a city\n" +
            "• /view_favorites - Favorite cities\n" +
            "• /download - Download the app\n" +
            "• /lang - Change language\n" +
            "• /help - This help message\n" +
            "• /cancel - Cancel action\n\n" +
            "<b>Inline mode:</b>\n" +
            "Use @WorldTimeMapBot in any chat to search for cities");
            
        en.put("help_admin_text", "🛡️ <b>Admin Panel & Management Commands</b>\n\n" +
            "• /users - List of all users\n" +
            "• /broadcast or /announce - Broadcast message to everyone\n" +
            "• /ban @username - Ban a user in the bot\n" +
            "• /unban @username - Unban a user\n" +
            "• /kick @username - Kick a user (ban + clear favorites)\n" +
            "• /help admin - Show this message");

        en.put("download_title", "📥 <b>Download WorldAtlas App</b>\n\nSelect your operating system:");
        en.put("download_win", "💻 <b>Download for Windows</b>\n\n📥 <a href=\"https://drive.google.com/file/d/1H_-f3yTqhGvstVwLw37paltGU5TQHgWD/view\">Download file</a>\n\n<b>Instructions:</b>\n1. Click the link above\n2. Click Download\n3. Open the file and follow the installer instructions");
        en.put("download_android", "📱 <b>Download for Android</b>\n\n📥 <a href=\"https://drive.google.com/file/d/1cHzF9ba-91bKSv_3G7Wn7-fEfNPDHETR/view\">Download APK</a>\n\n<b>Instructions:</b>\n1. Click the link above\n2. Allow installation from unknown sources in settings\n3. Open the downloaded APK file and tap Install");
        en.put("download_linux", "🐧 <b>Download for Linux</b>\n\n📥 <a href=\"https://drive.google.com/file/d/1F2324DmMfWR2UIQGD-sSMLliGevMKXsi/view\">Download file</a>\n\n<b>Instructions:</b>\n1. Download the file\n2. Open terminal in the file's folder\n3. Make executable: <code>chmod +x worldatlas-linux</code>\n4. Run: <code>./worldatlas-linux</code>");
        en.put("back", "⬅️ Back");
        en.put("banned_msg", "🚫 You are banned in this bot. Contact the administrator.");
        en.put("action_notify", "🚨 <b>Action Notification</b>\n\n👤 Admin/Mod: %s (ID: %d)\n🎯 Target: %s (ID: %d)\n⚡ Action: %s");
        en.put("ban_success", "✅ User %s has been successfully banned.");
        en.put("unban_success", "✅ User %s has been successfully unbanned.");
        en.put("kick_success", "✅ User %s has been kicked (banned and cleared).");
        en.put("cannot_ban_admin", "❌ Cannot ban an administrator!");

        translations.put("ru", ru);
        translations.put("en", en);
    }
    
    public String get(String lang, String key) {
        if (lang == null) lang = "ru";
        Map<String, String> langMap = translations.getOrDefault(lang, translations.get("ru"));
        return langMap.getOrDefault(key, key);
    }
}
