package com.worldatlas.bot.bot;

import lombok.extern.slf4j.Slf4j;

import com.worldatlas.bot.entity.City;
import com.worldatlas.bot.entity.User;
import com.worldatlas.bot.service.CityService;
import com.worldatlas.bot.service.LocalizationService;
import com.worldatlas.bot.service.TimeService;
import com.worldatlas.bot.service.CustomCityService;
import com.worldatlas.bot.service.SupportService;
import com.worldatlas.bot.entity.SupportMessage;
import com.worldatlas.bot.service.ReminderService;
import com.worldatlas.bot.service.ReminderScheduler;
import com.worldatlas.bot.entity.CustomCity;
import com.worldatlas.bot.entity.Reminder;
import com.worldatlas.bot.service.UserService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllPrivateChats;

@Slf4j
public class WorldAtlasBot extends TelegramLongPollingBot {
    private final UserService userService;
    private final CityService cityService;
    private final ReminderService reminderService;
    private final SupportService supportService;
    private final LocalizationService localization;
    private final TimeService timeService;
    private final CustomCityService customCityService;
    private final String botUsername;
    private final String botToken;
    
    private final Map<Long, String> userStates = new ConcurrentHashMap<>();
    private int lastUpdateId = 0;
    private static final String STATE_WAITING_CITY = "WAITING_CITY";
    private static final String STATE_WAITING_REMOVE = "WAITING_REMOVE";
    private static final String STATE_WAITING_BROADCAST = "WAITING_BROADCAST";
    private static final String STATE_WAITING_CHANGE_UTC_CUSTOM = "WAITING_CHANGE_UTC_CUSTOM";
    private static final String STATE_WAITING_CHANGE_NAME_CUSTOM = "WAITING_CHANGE_NAME_CUSTOM";
    private static final String STATE_WAITING_HOME_CITY = "WAITING_HOME_CITY";
    private static final String STATE_SELECTING_LANGUAGE = "SELECTING_LANGUAGE";
    private static final String STATE_WAITING_SUBSCRIPTION = "WAITING_SUBSCRIPTION";
    private static final String STATE_WAITING_REMINDER_CITY = "WAITING_REMINDER_CITY";
    private static final String STATE_WAITING_REMINDER_TIME = "WAITING_REMINDER_TIME";
    private static final String STATE_WAITING_REMINDER_TEXT = "WAITING_REMINDER_TEXT";
    private static final String STATE_WAITING_REMINDER_DAYS = "WAITING_REMINDER_DAYS";
    private static final String STATE_WAITING_CUSTOM_CITY_NAME = "WAITING_CUSTOM_CITY_NAME";
    private static final String STATE_WAITING_CUSTOM_CITY_TIMEZONE = "WAITING_CUSTOM_CITY_TIMEZONE";
    private static final String STATE_WAITING_DELETE_CUSTOM_CITY = "WAITING_DELETE_CUSTOM_CITY";
    private static final String OWNER_SECRET_KEY = "DenisWorldAtlasSupreme2026!@#Owner";

    public WorldAtlasBot(DefaultBotOptions options, UserService userService, CityService cityService, LocalizationService localization, TimeService timeService, CustomCityService customCityService, ReminderService reminderService, SupportService supportService, String botUsername, String botToken) {
        super(options, botToken);
        this.userService = userService;
        this.cityService = cityService;
        this.localization = localization;
        this.timeService = timeService;
        this.customCityService = customCityService;
        this.reminderService = reminderService;
        this.supportService = supportService;
        this.botUsername = botUsername;
        this.botToken = botToken;
    
        // Отложенная установка команд в отдельном потоке
        new Thread(() -> {
            int attempts = 5;
            while (attempts > 0) {
                try {
                    Thread.sleep(10000);
                    setCommands();
                    System.out.println("✅ Команды успешно установлены");
                    break;
                } catch (Exception e) {
                    attempts--;
                    System.out.println("⚠️ Установка команд (попыток осталось: " + attempts + "): " + e.getMessage());
                }
            }
        }).start();
    }


    private void setCommands() {
        try {
            // Английские команды (по умолчанию)
            List<BotCommand> englishCommands = Arrays.asList(
                new BotCommand("start", "Start the bot"),
                new BotCommand("search_city", "Search for a city"),
                new BotCommand("view_favorites", "Your favorite cities"),
                new BotCommand("worldclock", "World clock"),
                new BotCommand("convert", "Convert time from home city"),
                new BotCommand("diff", "Time difference between cities"),
                new BotCommand("remind", "Create a reminder"),
                new BotCommand("remind_list", "View your reminder"),
                new BotCommand("delete_remind", "Delete reminder"),
                new BotCommand("home", "Set home city"),
                new BotCommand("create_custom", "Create custom city"),
                new BotCommand("list_custom", "List custom cities"),
                new BotCommand("delete_custom", "Delete custom city"),
                new BotCommand("lang", "Change language"),
                new BotCommand("download", "Download the app"),
                new BotCommand("help", "Bot help"),
                new BotCommand("support", "Contact support"),
                new BotCommand("cancel", "Cancel current action")
            );
            
            SetMyCommands requestEn = new SetMyCommands();
            requestEn.setCommands(englishCommands);
            requestEn.setScope(new BotCommandScopeAllPrivateChats());
            execute(requestEn);
            
            // Русские команды (для пользователей с языком "ru")
            List<BotCommand> russianCommands = Arrays.asList(
                new BotCommand("start", "Запустить бота"),
                new BotCommand("search_city", "Найти город"),
                new BotCommand("view_favorites", "Ваши избранные города"),
                new BotCommand("worldclock", "Мировые часы"),
                new BotCommand("convert", "Конвертировать время из домашнего города"),
                new BotCommand("diff", "Разница во времени между городами"),
                new BotCommand("remind", "Создать напоминалку"),
                new BotCommand("remind_list", "Показать напоминалку"),
                new BotCommand("delete_remind", "Удалить напоминалку"),
                new BotCommand("home", "Задать домашний город"),
                new BotCommand("create_custom", "Создать пользовательский город"),
                new BotCommand("list_custom", "Список пользовательских городов"),
                new BotCommand("delete_custom", "Удалить пользовательский город"),
                new BotCommand("lang", "Сменить язык"),
                new BotCommand("download", "Скачать приложение"),
                new BotCommand("help", "Помощь по боту"),
                new BotCommand("cancel", "Отменить текущее действие")
            );
            
            SetMyCommands requestRu = new SetMyCommands();
            requestRu.setCommands(russianCommands);
            requestRu.setScope(new BotCommandScopeAllPrivateChats());
            requestRu.setLanguageCode("ru");
            execute(requestRu);
            
            System.out.println("✅ Команды установлены на двух языках");
        } catch (Exception e) {
            System.out.println("❌ Ошибка установки команд: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() { return botUsername; }
    @Override
    public String getBotToken() { return botToken; }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasInlineQuery()) { handleInlineQuery(update.getInlineQuery()); return; }
            if (update.hasCallbackQuery()) { handleCallback(update); return; }
            if (update.hasMessage() && update.getMessage().hasText()) { handleMessage(update); }
        } catch (Exception e) { log.error("Error: {}", e.getMessage()); }
    }

    private void handleMessage(Update update) {
        Message message = update.getMessage();
        long chatId = message.getChatId();
        String text = message.getText().trim();
        User user = userService.findOrCreate(chatId, message.getFrom().getUserName(), message.getFrom().getFirstName(), message.getFrom().getLastName());
        if (user == null) return;
        
        if (user.isBanned()) {
            sendMsg(chatId, "🚫 Вы заблокированы / You are banned");
            return;
        }
        
        String lang = user.getLanguage();

        // Обработка команды /reply для админа
        if (text.startsWith("/reply ") && chatId == com.worldatlas.bot.service.UserService.MAIN_ADMIN_ID) {
            String rest = text.substring(7).trim();
            int spaceIdx = rest.indexOf(' ');
            if (spaceIdx > 0) {
                try {
                    String idStr = rest.substring(0, spaceIdx);
                    String replyText = rest.substring(spaceIdx + 1).trim();
                    
                    // Пробуем как chatId (прямой ответ пользователю)
                    try {
                        Long targetChatId = Long.parseLong(idStr);
                        User targetUser = userService.getUser(targetChatId);
                        String userLang = targetUser != null ? targetUser.getLanguage() : "ru";
                        
                        SendMessage userMsg = new SendMessage();
                        userMsg.setChatId(targetChatId);
                        userMsg.setText("🤖 " + ("en".equals(userLang) ? 
                            "*Support replied:*\n\n" + replyText :
                            "*Поддержка ответила:*\n\n" + replyText));
                        userMsg.setParseMode("Markdown");
                        userMsg.setReplyMarkup(getMainMenuKeyboard(userLang));
                        execute(userMsg);
                        
                        sendMsg(chatId, "✅ Ответ отправлен пользователю " + targetChatId);
                        return;
                    } catch (NumberFormatException ignored) {}
                    
                    // Пробуем как messageId (из Mini App)
                    try {
                        Long msgId = Long.parseLong(idStr);
                        SupportMessage msg = supportService.getById(msgId);
                        if (msg != null && !msg.isAnswered()) {
                            supportService.answerMessage(msgId, replyText);
                            
                            User targetUser = userService.getUser(msg.getChatId());
                            String userLang = targetUser != null ? targetUser.getLanguage() : "ru";
                            
                            SendMessage userMsg = new SendMessage();
                            userMsg.setChatId(msg.getChatId());
                            userMsg.setText("🤖 " + ("en".equals(userLang) ?
                                "*Support replied:*\n\n" + replyText :
                                "*Поддержка ответила:*\n\n" + replyText));
                            userMsg.setParseMode("Markdown");
                            userMsg.setReplyMarkup(getMainMenuKeyboard(userLang));
                            execute(userMsg);
                            
                            sendMsg(chatId, "✅ Ответ отправлен пользователю " + msg.getChatId());
                        } else {
                            sendMsg(chatId, "❌ Сообщение не найдено или уже отвечено");
                        }
                    } catch (Exception e) {
                        sendMsg(chatId, "❌ Ошибка: " + e.getMessage());
                    }
                } catch (Exception e) {
                    sendMsg(chatId, "❌ Ошибка: " + e.getMessage());
                }
            } else {
                sendMsg(chatId, "Используй:\n`/reply CHAT_ID твой ответ`\nили\n`/reply MSG_ID твой ответ`");
            }
            return;
        }
        
        // ========== ПРОВЕРКА ПОДПИСКИ НА КАНАЛ ==========
        try {
            org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember getMember = 
                new org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember();
            getMember.setChatId("@WorldTimeMap");
            getMember.setUserId(user.getChatId());
            var member = execute(getMember);
            String status = member.getStatus();
            boolean isSubscribed = "member".equals(status) || "administrator".equals(status) || "creator".equals(status);
            
            if (!isSubscribed && !text.equals("/start")) {
                SendMessage subMsg = new SendMessage();
                subMsg.setChatId(chatId);
                subMsg.setText("en".equals(lang) ?
                    "🔒 To use the bot, please subscribe to our channel first!\n\n📢 @WorldTimeMap" :
                    "🔒 Чтобы пользоваться ботом, сначала подпишитесь на наш канал!\n\n📢 @WorldTimeMap");
                
                org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup markup = 
                    new org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup();
                java.util.List<java.util.List<org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton>> keyboard = new java.util.ArrayList<>();
                java.util.List<org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton> row = new java.util.ArrayList<>();
                org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton btn = 
                    new org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton();
                btn.setText("en".equals(lang) ? "📢 Subscribe to @WorldTimeMap" : "📢 Подписаться на @WorldTimeMap");
                btn.setUrl("https://t.me/WorldTimeMap");
                row.add(btn);
                keyboard.add(row);
                
                java.util.List<org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton> row2 = new java.util.ArrayList<>();
                org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton btn2 = 
                    new org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton();
                btn2.setText("en".equals(lang) ? "✅ I subscribed" : "✅ Я подписался");
                btn2.setCallbackData("check_sub");
                row2.add(btn2);
                keyboard.add(row2);
                
                markup.setKeyboard(keyboard);
                subMsg.setReplyMarkup(markup);
                execute(subMsg);
                return;
            }
        } catch (Exception e) {
            System.out.println("⚠️ Ошибка проверки подписки: " + e.getMessage());
        }

        if (text.equals("/start")) {
            if (userService.isSubscribed(chatId)) {
                sendWelcome(chatId, lang);
                return;
            }
            
            String text_msg = "🚀 <b>Старт!</b>\n\nДобро пожаловать в WorldAtlas Bot!\n\nДля начала работы выберите язык:";
            
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(createInlineButton("🇷🇺 Русский", "start_lang_ru"));
            row1.add(createInlineButton("🇬🇧 English", "start_lang_en"));
            rows.add(row1);
            markup.setKeyboard(rows);
            
            sendMsg(chatId, text_msg, markup);
            userStates.put(chatId, STATE_SELECTING_LANGUAGE);
            return;
        }

        // НАСТРОЙКИ
        if (text.equals("⚙️ Настройки") || text.equals("⚙️ Settings") || text.equals("/settings")) {
            showSettings(chatId, lang, user);
            return;
        }

        // НАПОМИНАЛКА
        if (text.equals("/remind")) {
            if (reminderService.hasReminder(chatId)) {
                sendMsg(chatId, "en".equals(lang) ?
                    "❌ You already have a reminder. Use /delete_remind to delete it first." :
                    "❌ У вас уже есть напоминалка. Сначала удалите её с помощью /delete_remind.");
                return;
            }
            userStates.put(chatId, STATE_WAITING_REMINDER_CITY);
            sendMsg(chatId, "en".equals(lang) ?
                "⏰ <b>Create Reminder</b>\n\nStep 1: Enter the city name:" :
                "⏰ <b>Создать напоминалку</b>\n\nШаг 1: Введите название города:");
            return;
        }

        if (text.equals("/delete_remind")) {
            if (reminderService.hasReminder(chatId)) {
                reminderService.deleteReminder(chatId);
                sendMsg(chatId, "en".equals(lang) ?
                    "✅ Reminder deleted." :
                    "✅ Напоминалка удалена.");
            } else {
                sendMsg(chatId, "en".equals(lang) ?
                    "❌ You don't have any reminders." :
                    "❌ У вас нет напоминалок.");
            }
            return;
        }

        if (text.equals("/remind_list")) {
            var reminderOpt = reminderService.getReminder(chatId);
            if (reminderOpt.isPresent()) {
                Reminder reminder = reminderOpt.get();
                String info = "en".equals(lang) ?
                    "⏰ <b>Your Reminder</b>\n\n" +
                    "🌍 City: " + reminder.getCityName() + "\n" +
                    "🕐 Time: " + reminder.getTime() + "\n" +
                    "📝 Text: " + reminder.getText() + "\n" +
                    "📅 Days: " + reminder.getDays() :
                    "⏰ <b>Ваша напоминалка</b>\n\n" +
                    "🌍 Город: " + reminder.getCityName() + "\n" +
                    "🕐 Время: " + reminder.getTime() + "\n" +
                    "📝 Текст: " + reminder.getText() + "\n" +
                    "📅 Дни: " + reminder.getDays();

                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                List<InlineKeyboardButton> row1 = new ArrayList<>();
                row1.add(createInlineButton("en".equals(lang) ? "🗑️ Delete" : "🗑️ Удалить", "delete_reminder_btn"));
                rows.add(row1);
                markup.setKeyboard(rows);
                
                sendMsg(chatId, info, markup);
            } else {
                sendMsg(chatId, "en".equals(lang) ?
                    "❌ You don't have any reminders. Use /remind to create one." :
                    "❌ У вас нет напоминалок. Используйте /remind чтобы создать.");
            }
            return;
        }

        if (text.equals("/my_reminder")) {
            var reminderOpt = reminderService.getReminder(chatId);
            if (reminderOpt.isPresent()) {
                Reminder reminder = reminderOpt.get();
                String info = "en".equals(lang) ?
                    "⏰ <b>Your Reminder</b>\n\n" +
                    "🌍 City: " + reminder.getCityName() + "\n" +
                    "🕐 Time: " + reminder.getTime() + "\n" +
                    "📝 Text: " + reminder.getText() + "\n" +
                    "📅 Days: " + reminder.getDays() :
                    "⏰ <b>Ваша напоминалка</b>\n\n" +
                    "🌍 Город: " + reminder.getCityName() + "\n" +
                    "🕐 Время: " + reminder.getTime() + "\n" +
                    "📝 Текст: " + reminder.getText() + "\n" +
                    "📅 Дни: " + reminder.getDays();
                sendMsg(chatId, info);
            } else {
                sendMsg(chatId, "en".equals(lang) ?
                    "❌ You don't have any reminders. Use /remind to create one." :
                    "❌ У вас нет напоминалок. Используйте /remind чтобы создать.");
            }
            return;
        }

        // ДОМАШНИЙ ГОРОД
        if (text.startsWith("/home ")) {
            String cityName = text.substring("/home ".length()).trim().toLowerCase();
            City city = cityService.findCity(cityName);
            if (city != null) {
                userService.setHomeCity(chatId, cityName);
                String displayName = cityService.getCityNameLocalized(city, lang);
                sendMsg(chatId, "en".equals(lang) ?
                    "✅ Home city set to <b>" + displayName + "</b>" :
                    "✅ Домашний город установлен: <b>" + displayName + "</b>");
            } else {
                sendMsg(chatId, "en".equals(lang) ?
                    "❌ City not found." :
                    "❌ Город не найден.");
            }
            return;
        }

        if (text.equals("/home")) {
            String homeCity = userService.getHomeCity(chatId);
            if (homeCity != null) {
                City city = cityService.findCity(homeCity);
                if (city != null) {
                    String displayName = cityService.getCityNameLocalized(city, lang);
                    String countryName = cityService.getCountryLocalized(city, lang);
                    java.time.ZonedDateTime cityTime = java.time.ZonedDateTime.now(java.time.ZoneId.of(city.getTimezone()));
                    String timePattern = "12".equals(user.getTimeFormat()) ? "hh:mm a" : "HH:mm";
                    String timeStr = cityTime.format(java.time.format.DateTimeFormatter.ofPattern(timePattern, "en".equals(lang) ? java.util.Locale.ENGLISH : new java.util.Locale("ru")));
                    int offsetSeconds = cityTime.getOffset().getTotalSeconds();
                    int offsetHours = offsetSeconds / 3600;
                    String utcStr = String.format("UTC%s%02d", offsetHours >= 0 ? "+" : "-", Math.abs(offsetHours));
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append("🏠 <b>").append("en".equals(lang) ? "Your Home City" : "Ваш домашний город").append("</b>\n\n");
                    sb.append("🌍 <b>").append(displayName).append("</b>\n");
                    sb.append("   📍 ").append(countryName).append("\n");
                    sb.append("   🕐 ").append(timeStr).append(" | 🌐 ").append(utcStr).append("\n\n");
                    sb.append("en".equals(lang) ? "Use /home city to change it." : "Используйте /home город чтобы изменить.");
                    sendMsg(chatId, sb.toString());
                }
            } else {
                sendMsg(chatId, "en".equals(lang) ?
                    "🏠 You haven't set a home city yet.\n\nUse /home Moscow to set it." :
                    "🏠 Вы еще не задали домашний город.\n\nИспользуйте /home Москва чтобы задать.");
            }
            return;
        }

        // МИРОВЫЕ ЧАСЫ
        if (text.equals("/worldclock")) {
            String[] worldCities = {"moscow", "london", "new york", "tokyo", "dubai", "sydney", "los angeles", "beijing"};
            
            StringBuilder sb = new StringBuilder();
            sb.append("🌍 <b>").append("en".equals(lang) ? "World Clock" : "Мировые часы").append("</b>\n\n");
            
            for (String cityName : worldCities) {
                City city = cityService.findCity(cityName);
                if (city != null) {
                    java.time.ZonedDateTime cityTime = java.time.ZonedDateTime.now(java.time.ZoneId.of(city.getTimezone()));
                    String timePattern = "12".equals(user.getTimeFormat()) ? "hh:mm a" : "HH:mm";
                    String timeStr = cityTime.format(java.time.format.DateTimeFormatter.ofPattern(timePattern, "en".equals(lang) ? java.util.Locale.ENGLISH : new java.util.Locale("ru")));
                    String displayName = cityService.getCityNameLocalized(city, lang);
                    
                    int offsetSeconds = cityTime.getOffset().getTotalSeconds();
                    int offsetHours = offsetSeconds / 3600;
                    String utcStr = String.format("UTC%s%02d", offsetHours >= 0 ? "+" : "-", Math.abs(offsetHours));
                    
                    sb.append("🕐 <b>").append(displayName).append("</b>: ").append(timeStr).append(" (").append(utcStr).append(")\n");
                }
            }
            
            sendMsg(chatId, sb.toString());
            return;
        }

        // КОНВЕРТЕР ВРЕМЕНИ
        if (text.startsWith("/convert ")) {
            String homeCity = userService.getHomeCity(chatId);
            if (homeCity == null) {
                sendMsg(chatId, "en".equals(lang) ?
                    "❌ You haven't set a home city. Use /home city to set it." :
                    "❌ Вы не задали домашний город. Используйте /home город чтобы задать.");
                return;
            }
            
            City city1 = cityService.findCity(homeCity);
            if (city1 == null) {
                sendMsg(chatId, "en".equals(lang) ?
                    "❌ Home city not found in database." :
                    "❌ Домашний город не найден в базе данных.");
                return;
            }
            
            String args = text.substring("/convert ".length()).trim();
            String[] parts = args.split("\\s+");
            
            if (parts.length < 2) {
                sendMsg(chatId, "en".equals(lang) ?
                    "❌ Format: /convert Tokyo 15:00" :
                    "❌ Формат: /convert Токио 15:00");
                return;
            }
            
            // Последний элемент - время
            String timeStr = parts[parts.length - 1];
            if (!timeStr.matches("\\d{1,2}:\\d{2}")) {
                sendMsg(chatId, "en".equals(lang) ?
                    "❌ Invalid time format. Use HH:MM (e.g., 15:00)." :
                    "❌ Неверный формат времени. Используйте ЧЧ:ММ (например, 15:00).");
                return;
            }
            
            // Собираем строку города (все кроме последнего элемента)
            StringBuilder cityBuilder = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                if (i > 0) cityBuilder.append(" ");
                cityBuilder.append(parts[i]);
            }
            String cityStr = cityBuilder.toString().toLowerCase().trim();
            
            City city2 = cityService.findCity(cityStr);
            if (city2 == null) {
                sendMsg(chatId, "en".equals(lang) ?
                    "❌ City not found." :
                    "❌ Город не найден.");
                return;
            }
            
            // Конвертируем время
            java.time.ZoneId zone1 = java.time.ZoneId.of(city1.getTimezone());
            java.time.ZoneId zone2 = java.time.ZoneId.of(city2.getTimezone());
            
            String[] timeParts = timeStr.split(":");
            int hours = Integer.parseInt(timeParts[0]);
            int minutes = Integer.parseInt(timeParts[1]);
            
            java.time.ZonedDateTime timeInCity1 = java.time.ZonedDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.of(hours, minutes), zone1);
            java.time.ZonedDateTime timeInCity2 = timeInCity1.withZoneSameInstant(zone2);
            
            String timePattern = "12".equals(user.getTimeFormat()) ? "hh:mm a" : "HH:mm";
            String convertedTimeStr = timeInCity2.format(java.time.format.DateTimeFormatter.ofPattern(timePattern, "en".equals(lang) ? java.util.Locale.ENGLISH : new java.util.Locale("ru")));
            
            String displayName1 = cityService.getCityNameLocalized(city1, lang);
            String displayName2 = cityService.getCityNameLocalized(city2, lang);
            
            sendMsg(chatId, "en".equals(lang) ?
                "🕐 <b>" + timeStr + "</b> in " + displayName1 + " = <b>" + convertedTimeStr + "</b> in " + displayName2 :
                "🕐 <b>" + timeStr + "</b> в " + displayName1 + " = <b>" + convertedTimeStr + "</b> в " + displayName2);
            return;
        }

        // РАЗНИЦА ВО ВРЕМЕНИ
        if (text.startsWith("/diff ")) {
            String args = text.substring("/diff ".length()).trim().toLowerCase();
            
            City city1 = null;
            City city2 = null;
            
            for (int i = 1; i < args.length(); i++) {
                String part1 = args.substring(0, i).trim();
                String part2 = args.substring(i).trim();
                City c1 = cityService.findCity(part1);
                if (c1 != null) {
                    City c2 = cityService.findCity(part2);
                    if (c2 != null) {
                        city1 = c1;
                        city2 = c2;
                        break;
                    }
                }
            }
            
            if (city1 == null || city2 == null) {
                sendMsg(chatId, "en".equals(lang) ?
                    "❌ Cities not found. Format: /diff Moscow New York" :
                    "❌ Города не найдены. Формат: /diff Москва Нью-Йорк");
                return;
            }
            
            java.time.ZoneId zone1 = java.time.ZoneId.of(city1.getTimezone());
            java.time.ZoneId zone2 = java.time.ZoneId.of(city2.getTimezone());
            
            java.time.ZonedDateTime now1 = java.time.ZonedDateTime.now(zone1);
            int offset1 = zone1.getRules().getOffset(now1.toInstant()).getTotalSeconds();
            int offset2 = zone2.getRules().getOffset(now1.toInstant()).getTotalSeconds();
            int diffHours = (offset2 - offset1) / 3600;
            
            String displayName1 = cityService.getCityNameLocalized(city1, lang);
            String displayName2 = cityService.getCityNameLocalized(city2, lang);
            
            String diffStr = diffHours >= 0 ? "+" + diffHours : String.valueOf(diffHours);
            
            sendMsg(chatId, "en".equals(lang) ?
                "🕐 Time difference between <b>" + displayName1 + "</b> and <b>" + displayName2 + "</b>: " + diffStr + " hours" :
                "🕐 Разница между <b>" + displayName1 + "</b> и <b>" + displayName2 + "</b>: " + diffStr + " часов");
            return;
        }

        if (text.equals("/help") || text.equals("📖 Помощь") || text.equals("📖 Help")) {
            String helpText = "en".equals(lang) ?
                "📖 <b>WorldAtlas Bot Help</b>\n\n" +
                "<b>Main commands:</b>\n" +
                "• /start - Restart bot\n" +
                "• /search_city - Find a city\n" +
                "• /view_favorites - Your favorite cities\n" +
                "• /download - Download the app\n" +
                "• /lang - Change language\n" +
                "• /help - This help message\n" +
                "• /cancel - Cancel current action\n" +
                "• /worldclock - World clock\n" +
                "• /home city - Set home city\n\n" +
                "<b>Custom cities:</b>\n" +
                "• /create_custom - Create custom city\n" +
                "• /list_custom - List your custom cities\n" +
                "• /delete_custom - Delete custom city\n" +
                "• /change_UTC_custom name - Change timezone\n" +
                "• /change_name_custom name - Rename city\n\n" +
                "<b>Other:</b>\n" +
                "• /convert city1 city2 time - Time converter\n" +
                "• /diff city1 city2 - Time difference\n" +
                "• /remind - Create reminder\n\n" +
                "<b>Inline mode:</b>\n" +
                "Use @" + botUsername + " in any chat to search for cities" :
                "📖 <b>Помощь по боту WorldAtlas</b>\n\n" +
                "<b>Основные команды:</b>\n" +
                "• /start - Запустить бота\n" +
                "• /search_city - Найти город\n" +
                "• /view_favorites - Ваши избранные города\n" +
                "• /download - Скачать приложение\n" +
                "• /lang - Сменить язык\n" +
                "• /help - Эта справка\n" +
                "• /worldclock - Мировые часы\n" +
                "• /cancel - Отменить действие\n" +
                "• /home город - Задать домашний город\n\n" +
                "<b>Пользовательские города:</b>\n" +
                "• /create_custom - создать пользовательский город\n" +
                "• /list_custom - список ваших пользовательских городов\n" +
                "• /delete_custom - удалить пользовательский город\n" +
                "• /change_UTC_custom имя - Изменить часовой пояс\n" +
                "• /change_name_custom имя - Переименовать город\n\n" +
                "<b>Дополнительно:</b>\n" +
                "• /convert город1 город2 время - Конвертер времени\n" +
                "• /diff город1 город2 - Разница во времени\n" +
                "• /remind - создать напоминалку\n\n" +
                "<b>Inline режим:</b>\n" +
                "Используйте @" + botUsername + " в любом чате для поиска городов";
            sendMsg(chatId, helpText);
            return;
        }

        if (text.equals("/adminhelp")) {
            if (user.getRole() != User.Role.ADMIN && user.getRole() != User.Role.MODERATOR && user.getRole() != User.Role.OWNER) {
                sendMsg(chatId, "❌ Доступ запрещен / Access denied");
                return;
            }
            StringBuilder adminHelp = new StringBuilder();
            String roleLabel = user.getRole() == User.Role.OWNER ? "💎 OWNER" : (user.getRole() == User.Role.ADMIN ? "👑 ADMIN" : "🛡️ MODERATOR");
            String roleLabelRu = user.getRole() == User.Role.OWNER ? "💎 ВЛАДЕЛЕЦ" : (user.getRole() == User.Role.ADMIN ? "👑 АДМИН" : "🛡️ МОДЕРАТОР");
            adminHelp.append("🛡️ <b>").append("en".equals(lang) ? "Full Control Panel" : "Полная панель управления").append("</b>\n");
            adminHelp.append("en".equals(lang) ? "Your rank: " : "Твой ранг: ").append("<b>").append("en".equals(lang) ? roleLabel : roleLabelRu).append("</b>\n\n");
            
            if ("en".equals(lang)) {
                adminHelp.append("👤 <b>Basic commands (all staff):</b>\n");
                adminHelp.append("• /users - List of all users with clickable profiles\n");
                adminHelp.append("• /ban @username - Ban a user\n");
                adminHelp.append("• /unban @username - Unban a user\n\n");
                
                if (user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.OWNER) {
                    adminHelp.append("👑 <b>Admin commands:</b>\n");
                    adminHelp.append("• /makeadmin @username - Promote to admin (requires code confirmation)\n");
                    adminHelp.append("• CONFIRM 1234 - Confirm promotion with code\n");
                    adminHelp.append("• /makemod @username - Promote to moderator\n");
                    adminHelp.append("• /kick mod @username [reason] - Demote moderator (with reason)\n");
                    adminHelp.append("• /broadcast or /announce - Send message to all users\n\n");
                }
                
                if (user.getRole() == User.Role.OWNER) {
                    adminHelp.append("💎 <b>Owner commands (exclusive):</b>\n");
                    adminHelp.append("• /kick admin @username [reason] - Demote admin (with reason)\n");
                    adminHelp.append("• /makeowner @username [secret_key] - Promote to owner\n");
                    adminHelp.append("• /demoteowner @username - Demote owner to admin\n");
                    adminHelp.append("• /iamadmin - Secret command for bot creator\n\n");
                    adminHelp.append("🔐 <b>Secret key for /makeowner:</b>\n");
                    adminHelp.append("`" + OWNER_SECRET_KEY + "`");
                }
            } else {
                adminHelp.append("👤 <b>Базовые команды (весь персонал):</b>\n");
                adminHelp.append("• /users - Список всех пользователей с кликабельными профилями\n");
                adminHelp.append("• /ban @username - Заблокировать пользователя\n");
                adminHelp.append("• /unban @username - Разблокировать пользователя\n\n");
                
                if (user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.OWNER) {
                    adminHelp.append("👑 <b>Команды админа:</b>\n");
                    adminHelp.append("• /makeadmin @username - Выдать админку (требуется подтверждение кодом)\n");
                    adminHelp.append("• CONFIRM 1234 - Подтвердить выдачу админки кодом\n");
                    adminHelp.append("• /makemod @username - Выдать модератора\n");
                    adminHelp.append("• /kick mod @username [причина] - Снять модератора (с причиной)\n");
                    adminHelp.append("• /broadcast или /announce - Рассылка сообщения всем пользователям\n\n");
                }
                
                if (user.getRole() == User.Role.OWNER) {
                    adminHelp.append("💎 <b>Команды владельца (эксклюзивно):</b>\n");
                    adminHelp.append("• /kick admin @username [причина] - Снять админа (с причиной)\n");
                    adminHelp.append("• /makeowner @username [секретный_ключ] - Выдать роль владельца\n");
                    adminHelp.append("• /demoteowner @username - Понизить владельца до админа\n");
                    adminHelp.append("• /iamadmin - Секретная команда создателя бота\n\n");
                    adminHelp.append("🔐 <b>Секретный ключ для /makeowner:</b>\n");
                    adminHelp.append("`" + OWNER_SECRET_KEY + "`");
                }
            }
            try {
                sendMsg(chatId, adminHelp.toString());
            } catch (Exception e) {
                log.error("Admin help error: {}", e.getMessage());
                sendMsg(chatId, "❌ Ошибка отправки справки админа");
            }
            return;
        }


        // РАССЫЛКА ВСЕМ ПОЛЬЗОВАТЕЛЯМ (только владелец)
        if (text.startsWith("/broadcast ")) {
            if (user.getRole() != User.Role.OWNER) {
                sendMsg(chatId, "en".equals(lang) ?
                    "❌ Only the owner can use this command." :
                    "❌ Только владелец может использовать эту команду.");
                return;
            }
            
            String broadcastMessage = text.substring("/broadcast ".length()).trim();
            if (broadcastMessage.isEmpty()) {
                sendMsg(chatId, "en".equals(lang) ?
                    "❌ Usage: /broadcast Your message here" :
                    "❌ Использование: /broadcast Ваше сообщение");
                return;
            }
            
            sendMsg(chatId, "en".equals(lang) ?
                "📢 Starting broadcast to all users..." :
                "📢 Начинаю рассылку всем пользователям...");
            
            int sent = 0;
            int failed = 0;
            
            List<User> allUsers = userService.getAllUsers();
            for (User u : allUsers) {
                try {
                    SendMessage msg = new SendMessage();
                    msg.setChatId(String.valueOf(u.getChatId()));
                    msg.setText(broadcastMessage);
                    msg.setParseMode("HTML");
                    execute(msg);
                    sent++;
                    Thread.sleep(50);
                } catch (Exception e) {
                    failed++;
                    System.out.println("❌ Ошибка отправки пользователю " + u.getChatId() + ": " + e.getMessage());
                }
            }
            
            sendMsg(chatId, "en".equals(lang) ?
                "✅ Broadcast completed!\n\n📤 Sent: " + sent + "\n❌ Failed: " + failed :
                "✅ Рассылка завершена!\n\n📤 Отправлено: " + sent + "\n❌ Ошибок: " + failed);
            return;
        }

        if (text.equals("/iamadmin")) {
            String u = user.getUsername();
            if ("BobrArbuz".equalsIgnoreCase(u) || "ArbuznyBober".equalsIgnoreCase(u)) {
                userService.setRole(chatId, User.Role.OWNER);
                sendMsg(chatId, "💎 Вы назначены ВЛАДЕЛЬЦЕМ бота!");
                notifyAdmin("💎 OWNER ASSIGNED\nTo: @" + u + " (ID: " + chatId + ")");
            } else {
                sendMsg(chatId, "❌ Эта команда только для создателя бота.");
            }
            return;
        }

        if (text.startsWith("/makeowner ")) {
            if (user.getRole() != User.Role.OWNER) {
                sendMsg(chatId, "❌ Только владелец может выдавать эту роль");
                notifyAdmin("⚠️ ПОПЫТКА /makeowner\nBy: @" + user.getUsername() + " (ID: " + chatId + ")");
                return;
            }
            String[] parts = text.split(" ", 3);
            if (parts.length < 3 || !parts[2].equals(OWNER_SECRET_KEY)) {
                sendMsg(chatId, "❌ Неверный секретный ключ");
                notifyAdmin("🚨 НЕВЕРНЫЙ КЛЮЧ OWNER\nBy: @" + user.getUsername());
                return;
            }
            String targetUsername = parts[1].replace("@", "");
            User target = userService.findByUsername(targetUsername);
            if (target != null) {
                userService.setRole(target.getChatId(), User.Role.OWNER);
                sendMsg(chatId, "💎 @" + targetUsername + " теперь ВЛАДЕЛЕЦ!");
                notifyAdmin("💎 MAKE OWNER\nBy: @" + user.getUsername() + "\nTarget: @" + targetUsername);
            } else {
                sendMsg(chatId, "❌ Пользователь не найден");
            }
            return;
        }

        if (text.startsWith("/demoteowner ")) {
            if (user.getRole() != User.Role.OWNER) {
                sendMsg(chatId, "❌ Только владелец может использовать эту команду");
                return;
            }
            String targetUsername = text.substring(13).trim().replace("@", "");
            User target = userService.findByUsername(targetUsername);
            if (target == null) { sendMsg(chatId, "❌ Пользователь не найден"); return; }
            if (target.getChatId().equals(chatId)) { sendMsg(chatId, "❌ Нельзя снять самого себя"); return; }
            if (target.getRole() != User.Role.OWNER) { sendMsg(chatId, "❌ Этот пользователь не владелец"); return; }
            userService.setRole(target.getChatId(), User.Role.ADMIN);
            sendMsg(chatId, "✅ @" + targetUsername + " понижен до администратора");
            notifyAdmin("⬇️ DEMOTE OWNER\nBy: @" + user.getUsername() + "\nTarget: @" + targetUsername);
            return;
        }

        if (text.startsWith("/kick mod ")) {
            if (user.getRole() != User.Role.ADMIN && user.getRole() != User.Role.OWNER) {
                sendMsg(chatId, "❌ Только для админов и владельца");
                return;
            }
            String afterCmd = text.substring("/kick mod ".length()).trim();
            String[] parts = afterCmd.split("\\s+", 2);
            String targetUsername = parts[0].replace("@", "");
            String reason = parts.length > 1 ? parts[1] : "не указана";
            
            User target = userService.findByUsername(targetUsername);
            if (target == null) {
                sendMsg(chatId, "❌ Пользователь не найден");
                return;
            }
            if (target.getRole() != User.Role.MODERATOR) {
                sendMsg(chatId, "❌ Этот пользователь не является модератором");
                return;
            }
            userService.setRole(target.getChatId(), User.Role.USER);
            sendMsg(chatId, "✅ @" + targetUsername + " лишен прав модератора");
            
            String demoteMsg = "⚠️ <b>Вы были сняты с должности МОДЕРАТОР</b>\n\n" +
                "👤 Снял: @" + user.getUsername() + "\n" +
                "📝 Причина: " + reason;
            sendMsg(target.getChatId(), demoteMsg);
            
            notifyAdmin("🛡️ KICK MOD\nBy: @" + user.getUsername() + "\nTarget: @" + targetUsername + "\nReason: " + reason);
            return;
        }

        if (text.startsWith("/kick admin ")) {
            if (user.getRole() != User.Role.OWNER) {
                sendMsg(chatId, "❌ Только владелец может снимать админов");
                return;
            }
            String afterCmd = text.substring("/kick admin ".length()).trim();
            String[] parts = afterCmd.split("\\s+", 2);
            String targetUsername = parts[0].replace("@", "");
            String reason = parts.length > 1 ? parts[1] : "не указана";
            
            User target = userService.findByUsername(targetUsername);
            if (target == null) {
                sendMsg(chatId, "❌ Пользователь не найден");
                return;
            }
            if (target.getRole() != User.Role.ADMIN) {
                sendMsg(chatId, "❌ Этот пользователь не является администратором");
                return;
            }
            userService.setRole(target.getChatId(), User.Role.USER);
            sendMsg(chatId, "✅ @" + targetUsername + " лишен прав админа");
            
            String demoteMsg = "⚠️ <b>Вы были сняты с должности АДМИНИСТРАТОР</b>\n\n" +
                "👤 Снял: @" + user.getUsername() + "\n" +
                "📝 Причина: " + reason;
            sendMsg(target.getChatId(), demoteMsg);
            
            notifyAdmin("👢 KICK ADMIN\nBy: @" + user.getUsername() + "\nTarget: @" + targetUsername + "\nReason: " + reason);
            return;
        }

        if (text.startsWith("/makeadmin ")) {
            if (user.getRole() != User.Role.ADMIN && user.getRole() != User.Role.OWNER) {
                sendMsg(chatId, "❌ Только для администраторов и владельца");
                return;
            }
            String targetUsername = text.substring(11).trim().replace("@", "");
            User target = userService.findByUsername(targetUsername);
            if (target == null) { sendMsg(chatId, "❌ Пользователь не найден"); return; }
            if (target.getRole() == User.Role.OWNER || target.getRole() == User.Role.ADMIN) {
                sendMsg(chatId, "❌ Этот пользователь уже имеет высокий ранг");
                return;
            }
            String code = String.format("%04d", new Random().nextInt(10000));
            userStates.put(chatId, "CONFIRM_ADMIN:" + target.getChatId() + ":" + code);
            sendMsg(chatId, "⚠️ <b>Подтверждение выдачи админки</b>\n\nЦель: @" + targetUsername + "\n\nЧтобы подтвердить, напишите:\n<code>CONFIRM " + code + "</code>\n\nДля отмены: /cancel");
            return;
        }

        if (text.startsWith("CONFIRM ")) {
            String state = userStates.get(chatId);
            if (state != null && state.startsWith("CONFIRM_ADMIN:")) {
                String[] parts = state.split(":");
                if (parts[2].equals(text.substring(8).trim())) {
                    Long targetChatId = Long.parseLong(parts[1]);
                    userService.setRole(targetChatId, User.Role.ADMIN);
                    User target = userService.getUser(targetChatId);
                    userStates.remove(chatId);
                    sendMsg(chatId, "✅ @" + target.getUsername() + " теперь АДМИНИСТРАТОР");
                    notifyAdmin("👑 MAKE ADMIN\nBy: @" + user.getUsername() + "\nTarget: @" + target.getUsername());
                } else {
                    sendMsg(chatId, "❌ Неверный код подтверждения");
                }
                return;
            }
        }

        if (text.startsWith("/makemod ")) {
            if (user.getRole() != User.Role.ADMIN && user.getRole() != User.Role.OWNER) {
                sendMsg(chatId, "❌ Только для администраторов и владельца");
                return;
            }
            String targetUsername = text.substring(9).trim().replace("@", "");
            User target = userService.findByUsername(targetUsername);
            if (target != null) {
                userService.setRole(target.getChatId(), User.Role.MODERATOR);
                sendMsg(chatId, "✅ @" + targetUsername + " теперь МОДЕРАТОР");
                notifyAdmin("🛡️ MAKE MOD\nBy: @" + user.getUsername() + "\nTarget: @" + targetUsername);
            } else {
                sendMsg(chatId, "❌ Пользователь не найден");
            }
            return;
        }

        if (text.equals("/users")) {
            if (user.getRole() != User.Role.ADMIN && user.getRole() != User.Role.MODERATOR && user.getRole() != User.Role.OWNER) {
                sendMsg(chatId, "❌ Доступ запрещен / Access denied");
                return;
            }
            List<User> users = userService.getAllUsers();
            StringBuilder sb = new StringBuilder("👥 <b>Users List</b> (Total: " + users.size() + ")\n\n");
            for (User u : users) {
                String displayName = (u.getUsername() != null && !u.getUsername().isEmpty()) ? "@" + u.getUsername() : u.getFirstName();
                String roleIcon = u.getRole() == User.Role.OWNER ? "💎" : (u.getRole() == User.Role.ADMIN ? "👑" : (u.getRole() == User.Role.MODERATOR ? "🛡️" : "👤"));
                sb.append("• <a href=\"tg://user?id=").append(u.getChatId()).append("\">").append(displayName).append("</a> (ID: ").append(u.getChatId()).append(") ").append(roleIcon).append(u.isBanned() ? " 🚫" : "").append("\n");
            }
            sendMsg(chatId, sb.toString());
            return;
        }

        if (text.startsWith("/ban ")) {
            if (user.getRole() != User.Role.ADMIN && user.getRole() != User.Role.MODERATOR && user.getRole() != User.Role.OWNER) {
                sendMsg(chatId, "❌ Доступ запрещен / Access denied");
                return;
            }
            String targetUsername = text.substring(5).trim().replace("@", "");
            User target = userService.findByUsername(targetUsername);
            if (target == null) { sendMsg(chatId, "❌ Пользователь не найден"); return; }
            if (target.getRole() == User.Role.OWNER || (user.getRole() != User.Role.OWNER && target.getRole() == User.Role.ADMIN)) {
                sendMsg(chatId, "❌ Нельзя заблокировать пользователя с равным или высшим рангом");
                return;
            }
            userService.setBanned(target.getChatId(), true);
            sendMsg(chatId, "✅ Пользователь @" + targetUsername + " заблокирован");
            notifyAdmin("🔨 BAN\nBy: @" + user.getUsername() + "\nTarget: @" + targetUsername);
            return;
        }

        if (text.startsWith("/unban ")) {
            if (user.getRole() != User.Role.ADMIN && user.getRole() != User.Role.MODERATOR && user.getRole() != User.Role.OWNER) {
                sendMsg(chatId, "❌ Доступ запрещен / Access denied");
                return;
            }
            String targetUsername = text.substring(7).trim().replace("@", "");
            User target = userService.findByUsername(targetUsername);
            if (target == null) { sendMsg(chatId, "❌ Пользователь не найден"); return; }
            userService.setBanned(target.getChatId(), false);
            sendMsg(chatId, "✅ Пользователь @" + targetUsername + " разблокирован");
            notifyAdmin("🔓 UNBAN\nBy: @" + user.getUsername() + "\nTarget: @" + targetUsername);
            return;
        }

        if (text.equals("/download") || text.equals("📥 Скачать") || text.equals("📥 Download")) { showDownloadMenu(chatId, lang); return; }
        if (text.equals("/lang") || text.equals("🌐 Язык") || text.equals("🌐 Language")) {
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(createInlineButton("🇷🇺 Русский", "lang_ru"));
            row.add(createInlineButton("🇬🇧 English", "lang_en"));
            rows.add(row);
            markup.setKeyboard(rows);
            sendMsg(chatId, "🌍 Выберите язык / Choose language:", markup);
            return;
        }
        if (text.equals("/search_city") || text.equals("🔍 Поиск") || text.equals("🔍 Search")) {
            userStates.put(chatId, STATE_WAITING_CITY);
            sendMsg(chatId, "en".equals(lang) ? "🔍 Enter city name:" : "🔍 Введите название города:");
            return;
        }
                if (text.equals("/view_favorites") || text.equals("⭐ Избранное") || text.equals("⭐ Favorites")) {
            try {
                List<String> favs = userService.getFavorites(chatId);
                List<CustomCity> customCities = customCityService.getUserCustomCities(chatId);
                
                if ((favs == null || favs.isEmpty()) && (customCities == null || customCities.isEmpty())) {
                    sendMsg(chatId, "en".equals(lang) ? "⭐ Your favorites list is empty.\n\nUse /search_city to find cities and add them!" : "⭐ Ваш список избранных городов пуст.\n\nИспользуйте /search_city чтобы найти города и добавить их!");
                } else {
                    StringBuilder sb = new StringBuilder();
                    
                    if (favs != null && !favs.isEmpty()) {
                        sb.append("en".equals(lang) ? "⭐ <b>Your Favorites:</b>\n\n" : "⭐ <b>Ваше избранное:</b>\n\n");
                        for (String cityName : favs) {
                            City city = cityService.findCity(cityName);
                            if (city != null) {
                                String displayName = cityService.getCityNameLocalized(city, lang);
                                String countryName = cityService.getCountryLocalized(city, lang);
                                java.time.ZonedDateTime cityTime = java.time.ZonedDateTime.now(java.time.ZoneId.of(city.getTimezone()));
                                String timeStr = cityTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
                                int offsetSeconds = cityTime.getOffset().getTotalSeconds();
                                int offsetHours = offsetSeconds / 3600;
                                String utcStr = String.format("UTC%s%02d", offsetHours >= 0 ? "+" : "-", Math.abs(offsetHours));
                                sb.append("🌍 <b>").append(displayName).append("</b>\n")
                                  .append("   📍 ").append(countryName).append("\n")
                                  .append("   🕐 ").append(timeStr).append(" | 🌐 ").append(utcStr).append("\n\n");
                            }
                        }
                    }
                    
                    if (customCities != null && !customCities.isEmpty()) {
                        sb.append("🏙️ <b>").append("en".equals(lang) ? "Custom Cities:" : "Пользовательские города:").append("</b>\n\n");
                        for (CustomCity cc : customCities) {
                            ZonedDateTime now = customCityService.getTimeForCustomCity(cc);
                            String timeStr = now.format(DateTimeFormatter.ofPattern("HH:mm"));
                            String displayName = cc.getName().substring(0, 1).toUpperCase() + cc.getName().substring(1);
                            sb.append("🏙️ <b>").append(displayName).append("</b>\n")
                              .append("   🌐 UTC").append(cc.getTimezone()).append("\n")
                              .append("   🕐 ").append(timeStr).append("\n\n");
                        }
                    }
                    
                    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                    InlineKeyboardButton removeBtn = new InlineKeyboardButton();
                    removeBtn.setText("en".equals(lang) ? "🗑️ Remove city" : "🗑️ Удалить город");
                    removeBtn.setCallbackData("remove_fav:start");
                    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                    List<InlineKeyboardButton> row = new ArrayList<>();
                    row.add(removeBtn);
                    rows.add(row);
                    markup.setKeyboard(rows);
                    
                    sendMsg(chatId, sb.toString(), markup);
                }
            } catch (Exception e) {
                log.error("Favorites error: {}", e.getMessage(), e);
                sendMsg(chatId, "❌ Error loading favorites: " + e.getMessage());
            }
            return;
        }
                if (text.equals("/create_custom")) {
            userStates.put(chatId, STATE_WAITING_CUSTOM_CITY_NAME);
            sendMsg(chatId, "en".equals(lang) ? 
                "🏙️ Enter the name of your custom city:" :
                "🏙️ Введите название вашего пользовательского города:");
            return;
        }

        if (text.equals("/list_custom")) {
            List<CustomCity> customCities = customCityService.getUserCustomCities(chatId);
            if (customCities.isEmpty()) {
                sendMsg(chatId, "en".equals(lang) ? 
                    "🏙️ You have no custom cities." :
                    "🏙️ У вас нет пользовательских городов.");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("en".equals(lang) ? "🏙️ <b>Your Custom Cities:</b>\n\n" : "🏙️ <b>Ваши пользовательские города:</b>\n\n");
                for (CustomCity cc : customCities) {
                    ZonedDateTime now = customCityService.getTimeForCustomCity(cc);
                    String timeStr = now.format(DateTimeFormatter.ofPattern("HH:mm"));
                    String displayName = cc.getName().substring(0, 1).toUpperCase() + cc.getName().substring(1);
                    sb.append("🏙️ <b>").append(displayName).append("</b>\n")
                      .append("   🌐 UTC").append(cc.getTimezone()).append("\n")
                      .append("   🕐 ").append(timeStr).append("\n\n");
                }
                sendMsg(chatId, sb.toString());
            }
            return;
        }

        if (text.startsWith("/change_UTC_custom ")) {
            String cityName = text.substring("/change_UTC_custom ".length()).toLowerCase().trim();
            CustomCity customCity = customCityService.findCustomCityByNameAndUserId(chatId, cityName);
            if (customCity != null) {
                userStates.put(chatId, STATE_WAITING_CHANGE_UTC_CUSTOM + ":" + cityName);
                sendMsg(chatId, "en".equals(lang) ?
                    "🕐 Enter new timezone in format +07 or -11:" :
                    "🕐 Введите новый часовой пояс в формате +07 или -11:");
            } else {
                sendMsg(chatId, "en".equals(lang) ?
                    "❌ Custom city not found." :
                    "❌ Пользовательский город не найден.");
            }
            return;
        }

        if (text.startsWith("/change_name_custom ")) {
            String cityName = text.substring("/change_name_custom ".length()).toLowerCase().trim();
            CustomCity customCity = customCityService.findCustomCityByNameAndUserId(chatId, cityName);
            if (customCity != null) {
                userStates.put(chatId, STATE_WAITING_CHANGE_NAME_CUSTOM + ":" + cityName);
                sendMsg(chatId, "en".equals(lang) ?
                    "✏️ Enter new city name:" :
                    "✏️ Введите новое название города:");
            } else {
                sendMsg(chatId, "en".equals(lang) ?
                    "❌ Custom city not found." :
                    "❌ Пользовательский город не найден.");
            }
            return;
        }

        if (text.equals("/delete_custom")) {
            List<CustomCity> customCities = customCityService.getUserCustomCities(chatId);
            if (customCities.isEmpty()) {
                sendMsg(chatId, "en".equals(lang) ? 
                    "🏙️ You have no custom cities to delete." :
                    "🏙️ У вас нет пользовательских городов для удаления.");
                return;
            }
            userStates.put(chatId, STATE_WAITING_DELETE_CUSTOM_CITY);
            StringBuilder sb = new StringBuilder();
            sb.append("en".equals(lang) ? "🗑️ <b>Delete Custom City</b>\n\nYour custom cities:\n" : "🗑️ <b>Удалить пользовательский город</b>\n\nВаши пользовательские города:\n");
            for (CustomCity cc : customCities) {
                String displayName = cc.getName().substring(0, 1).toUpperCase() + cc.getName().substring(1);
                sb.append("• ").append(displayName).append("\n");
            }
            sb.append("\n").append("en".equals(lang) ? "Enter the name of the city to delete:" : "Введите название города для удаления:");
            sendMsg(chatId, sb.toString());
            return;
        }
        // ========== ПОДДЕРЖКА ==========
        if (text.equals("💬 Поддержка") || text.equals("💬 Support") || text.equals("/support")) {
            sendMsg(chatId, "en".equals(lang) ?
                "💬 Support\n\nSend your question like this:\n/support your question\n\nExample:\n/support the time converter is not working\n\n📧 Response time: up to 24 hours. Max 1 message per minute." :
                "💬 Поддержка\n\nОтправьте ваш вопрос так:\n/support ваш вопрос\n\nПример:\n/support не работает конвертер времени\n\n📧 Время ответа: до 24 часов. Не чаще 1 сообщения в минуту.",
                getMainMenuKeyboard(lang));
            return;
        }

        if (text.startsWith("/support ")) {
            String question = text.substring(9).trim();
            if (question.isEmpty()) {
                sendMsg(chatId, "en".equals(lang) ? "Enter your question:\n/support your question" : "Введите текст вопроса:\n/support ваш вопрос");
                return;
            }
            if (!supportService.canSendMessage(chatId)) {
                sendMsg(chatId, "en".equals(lang) ? "⏳ Too many messages. Try again in a minute." : "⏳ Слишком часто. Попробуйте через минуту.");
                return;
            }
            try {
                User sender = userService.getUser(chatId);
                String displayName = sender != null && sender.getFirstName() != null ? sender.getFirstName() : "@" + (sender != null && sender.getUsername() != null ? sender.getUsername() : String.valueOf(chatId));
                SupportMessage msg = supportService.createMessage(chatId, sender != null ? sender.getUsername() : null, question);
                
                SendMessage adminMsg = new SendMessage();
                adminMsg.setChatId(com.worldatlas.bot.service.UserService.MAIN_ADMIN_ID);
                adminMsg.setText("🆘 <b>Обращение в поддержку #" + msg.getId() + "</b>\n\n" +
                    "👤 " + displayName + " (<code>" + chatId + "</code>)\n" +
                    "💬 " + question + "\n\n" +
                    "Ответить:\n<code>/reply " + msg.getId() + " ваш ответ</code>\nили напрямую:\n<code>/reply " + chatId + " ваш ответ</code>");
                adminMsg.setParseMode("HTML");
                execute(adminMsg);
                
                sendMsg(chatId, "en".equals(lang) ?
                    "✅ Your message has been sent to support! We'll reply within 24 hours." :
                    "✅ Ваше сообщение отправлено в поддержку! Ответим в течение 24 часов.",
                    getMainMenuKeyboard(lang));
            } catch (Exception e) {
                sendMsg(chatId, "❌ " + e.getMessage());
            }
            return;
        }

        // ========== АДМИН-ПАНЕЛЬ ==========
        if ((text.equals("/admin") || text.equals("/adminhelp")) && chatId == com.worldatlas.bot.service.UserService.MAIN_ADMIN_ID) {
            try {
                long totalUsers = userService.getAllUsers().size();
                long totalTickets = supportService.getAllTickets().size();
                long newTickets = supportService.getUnanswered().size();
                
                String body = "en".equals(lang) ?
                    "🛡️ <b>ADMIN CONTROL PANEL</b>\n\n" +
                    "📊 <b>Quick Stats:</b>\n" +
                    "• 👥 Total users: <b>" + totalUsers + "</b>\n" +
                    "• 📨 Total tickets: <b>" + totalTickets + "</b>\n" +
                    "• 🆕 New tickets: <b>" + newTickets + "</b>\n\n" +
                    "🎯 <b>Available Commands:</b>\n" +
                    "• /stats - Detailed statistics\n" +
                    "• /tickets - All support tickets with buttons\n" +
                    "• /users - List of users\n" +
                    "• /reply ID text - Reply to ticket or user\n\n" +
                    "💡 Click buttons below for quick actions:" :
                    "🛡️ <b>ПАНЕЛЬ УПРАВЛЕНИЯ</b>\n\n" +
                    "📊 <b>Быстрая статистика:</b>\n" +
                    "• 👥 Всего пользователей: <b>" + totalUsers + "</b>\n" +
                    "• 📨 Всего обращений: <b>" + totalTickets + "</b>\n" +
                    "• 🆕 Новых обращений: <b>" + newTickets + "</b>\n\n" +
                    "🎯 <b>Доступные команды:</b>\n" +
                    "• /stats - Подробная статистика\n" +
                    "• /tickets - Все обращения с кнопками\n" +
                    "• /users - Список пользователей\n" +
                    "• /reply ID текст - Ответить на обращение\n\n" +
                    "💡 Нажмите кнопки ниже для быстрых действий:";
                
                SendMessage msg = new SendMessage();
                msg.setChatId(chatId);
                msg.setText(body);
                msg.setParseMode("HTML");
                
                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
                
                List<InlineKeyboardButton> row1 = new ArrayList<>();
                row1.add(createInlineButton("📊 " + ("en".equals(lang) ? "Statistics" : "Статистика"), "admin_stats"));
                row1.add(createInlineButton("📨 " + ("en".equals(lang) ? "Tickets" : "Обращения"), "admin_tickets"));
                keyboard.add(row1);
                
                List<InlineKeyboardButton> row2 = new ArrayList<>();
                row2.add(createInlineButton("👥 " + ("en".equals(lang) ? "Users" : "Пользователи"), "admin_users"));
                row2.add(createInlineButton("❌ " + ("en".equals(lang) ? "Close" : "Закрыть"), "admin_close"));
                keyboard.add(row2);
                
                markup.setKeyboard(keyboard);
                msg.setReplyMarkup(markup);
                
                execute(msg);
            } catch (Exception e) {
                sendMsg(chatId, "❌ " + e.getMessage());
            }
            return;
        }
        
        // ========== КОМАНДА /stats ==========
        if (text.equals("/stats") && chatId == com.worldatlas.bot.service.UserService.MAIN_ADMIN_ID) {
            try {
                long totalUsers = userService.getAllUsers().size();
                long totalReminders = reminderService.getAllReminders().size();
                long totalTickets = supportService.getAllTickets().size();
                long newTickets = supportService.getUnanswered().size();
                long totalCities = cityService.count();
                
                String body = "en".equals(lang) ?
                    "📊 <b>DETAILED STATISTICS</b>\n\n" +
                    "👥 Users: <b>" + totalUsers + "</b>\n" +
                    "⏰ Active reminders: <b>" + totalReminders + "</b>\n" +
                    "📨 Support tickets: <b>" + totalTickets + "</b>\n" +
                    "🆕 New tickets: <b>" + newTickets + "</b>\n" +
                    "🌍 Cities in DB: <b>" + totalCities + "</b>\n\n" +
                    "🟢 Bot status: <b>ONLINE 24/7</b>\n" +
                    "🗄️ Database: <b>Neon PostgreSQL</b>\n" +
                    "🚀 Hosting: <b>Render Cloud</b>" :
                    "📊 <b>ПОДРОБНАЯ СТАТИСТИКА</b>\n\n" +
                    "👥 Пользователей: <b>" + totalUsers + "</b>\n" +
                    "⏰ Активных напоминаний: <b>" + totalReminders + "</b>\n" +
                    "📨 Обращений в поддержку: <b>" + totalTickets + "</b>\n" +
                    "🆕 Новых обращений: <b>" + newTickets + "</b>\n" +
                    "🌍 Городов в базе: <b>" + totalCities + "</b>\n\n" +
                    "🟢 Статус бота: <b>ОНЛАЙН 24/7</b>\n" +
                    "🗄️ База данных: <b>Neon PostgreSQL</b>\n" +
                    "🚀 Хостинг: <b>Render Cloud</b>";
                
                sendMsg(chatId, body);
            } catch (Exception e) {
                sendMsg(chatId, "❌ " + e.getMessage());
            }
            return;
        }
        
        // ========== КОМАНДА /tickets ==========
        if (text.equals("/tickets") && chatId == com.worldatlas.bot.service.UserService.MAIN_ADMIN_ID) {
            try {
                List<SupportMessage> tickets = supportService.getAllTickets();
                
                if (tickets.isEmpty()) {
                    sendMsg(chatId, "en".equals(lang) ? "✅ No support tickets." : "✅ Нет обращений в поддержку.");
                    return;
                }
                
                int count = 0;
                for (int i = tickets.size() - 1; i >= 0 && count < 10; i--, count++) {
                    SupportMessage t = tickets.get(i);
                    
                    SupportMessage.TicketStatus status = t.getStatus() != null ? t.getStatus() : SupportMessage.TicketStatus.NEW;
                    
                    String statusEmoji = switch (status) {
                        case NEW -> "🆕";
                        case IN_PROGRESS -> "⏳";
                        case RESOLVED -> "✅";
                    };
                    
                    String statusText = switch (status) {
                        case NEW -> "en".equals(lang) ? "New" : "Новое";
                        case IN_PROGRESS -> "en".equals(lang) ? "In Progress" : "В процессе";
                        case RESOLVED -> "en".equals(lang) ? "Resolved" : "Решено";
                    };
                    
                    String body = statusEmoji + " <b>Ticket #" + t.getId() + "</b> | " + statusText + "\n\n" +
                        "👤 User: <code>" + t.getChatId() + "</code>\n" +
                        "💬 " + (t.getText().length() > 100 ? t.getText().substring(0, 100) + "..." : t.getText()) + "\n" +
                        "📅 " + t.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM HH:mm"));
                    
                    if (t.isAnswered() && t.getAdminReply() != null) {
                        body += "\n\n✅ <b>Reply:</b> " + t.getAdminReply();
                    }
                    
                    SendMessage msg = new SendMessage();
                    msg.setChatId(chatId);
                    msg.setText(body);
                    msg.setParseMode("HTML");
                    
                    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
                    
                    List<InlineKeyboardButton> row1 = new ArrayList<>();
                    row1.add(createInlineButton("💬 " + ("en".equals(lang) ? "Reply" : "Ответить"), "ticket_reply_" + t.getId()));
                    keyboard.add(row1);
                    
                    List<InlineKeyboardButton> row2 = new ArrayList<>();
                    row2.add(createInlineButton("🆕 " + ("en".equals(lang) ? "New" : "Новое"), "ticket_status_" + t.getId() + "_NEW"));
                    row2.add(createInlineButton("⏳ " + ("en".equals(lang) ? "In Progress" : "В процессе"), "ticket_status_" + t.getId() + "_IN_PROGRESS"));
                    row2.add(createInlineButton("✅ " + ("en".equals(lang) ? "Resolved" : "Решено"), "ticket_status_" + t.getId() + "_RESOLVED"));
                    keyboard.add(row2);
                    
                    markup.setKeyboard(keyboard);
                    msg.setReplyMarkup(markup);
                    
                    execute(msg);
                }
                
                sendMsg(chatId, "en".equals(lang) ? 
                    "📨 Showing last " + count + " tickets." :
                    "📨 Показано последних " + count + " обращений.");
            } catch (Exception e) {
                sendMsg(chatId, "❌ " + e.getMessage());
            }
            return;
        }
        
        // ========== КОМАНДА /users ==========
        if (text.equals("/users") && chatId == com.worldatlas.bot.service.UserService.MAIN_ADMIN_ID) {
            try {
                List<User> users = userService.getAllUsers();
                StringBuilder body = new StringBuilder();
                body.append("en".equals(lang) ? "👥 <b>LAST 20 USERS</b>\n\n" : "👥 <b>ПОСЛЕДНИЕ 20 ПОЛЬЗОВАТЕЛЕЙ</b>\n\n");
                
                int count = 0;
                for (int i = users.size() - 1; i >= 0 && count < 20; i--, count++) {
                    User u = users.get(i);
                    String name = u.getFirstName() != null ? u.getFirstName() : (u.getUsername() != null ? "@" + u.getUsername() : "ID:" + u.getChatId());
                    String roleEmoji = switch (u.getRole()) {
                        case OWNER -> "💎";
                        case ADMIN -> "👑";
                        case MODERATOR -> "🛡️";
                        case USER -> "👤";
                    };
                    body.append(count + 1).append(". ").append(roleEmoji).append(" ").append(name).append(" (<code>").append(u.getChatId()).append("</code>)\n");
                }
                
                if (users.isEmpty()) {
                    body.append("en".equals(lang) ? "No users yet." : "Пока нет пользователей.");
                }
                
                sendMsg(chatId, body.toString());
            } catch (Exception e) {
                sendMsg(chatId, "❌ " + e.getMessage());
            }
            return;
        }


        if (text.equals("🏙️ Пользовательский город") || text.equals("🏙️ Custom City")) {
            String desc = "en".equals(lang) ?
                "🏙️ <b>Custom City</b>\n\nA custom city is a city that is not in our database.\n\nYou can create your own city by specifying:\n• Name (must be unique)\n• Timezone (e.g., +07 or -11)\n\nTo create a custom city, use the button below:" :
                "🏙️ <b>Пользовательский город</b>\n\nПользовательский город — это город, которого нет в нашей базе.\n\nВы можете создать свой город, указав:\n• Название (должно быть уникальным)\n• Часовой пояс (например, +07 или -11)\n\nЧтобы создать пользовательский город, используйте кнопку ниже:";
            
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText("en".equals(lang) ? "➕ Create Custom City" : "➕ Создать пользовательский город");
            btn.setCallbackData("create_custom_city");
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(btn);
            rows.add(row);
            markup.setKeyboard(rows);
            sendMsg(chatId, desc, markup);
            return;
        }
        if (text.equals("/cancel") || text.equals("❌ Отмена") || text.equals("❌ Cancel")) {
            userStates.remove(chatId);
            sendMsg(chatId, "en".equals(lang) ? "✅ Action cancelled." : "✅ Действие отменено.");
            return;
        }

        String state = userStates.get(chatId);
        if (state != null) {
            if (state.equals(STATE_WAITING_CUSTOM_CITY_NAME)) {
                userStates.remove(chatId);
                String cityName = text.toLowerCase().trim();
                
                // Проверяем обычные города
                City existingCity = cityService.findCity(cityName);
                if (existingCity != null) {
                    String msg = "en".equals(lang) ? 
                        "❌ The name is already taken. Perhaps you were looking for this city:" :
                        "❌ Название занято. Возможно, вы искали этот город:";
                    sendMsg(chatId, msg);
                    sendMsg(chatId, cityService.getCityInfo(existingCity, lang, user.getTimeFormat()), getCityActionsKeyboard(existingCity.getName(), lang));
                    return;
                }
                
                // Проверяем пользовательские города ЭТОГО пользователя
                if (customCityService.isNameTakenByUser(chatId, cityName)) {
                    sendMsg(chatId, "en".equals(lang) ?
                        "❌ You already have a custom city with this name." :
                        "❌ У вас уже есть пользовательский город с таким названием.");
                    return;
                }
                
                userStates.put(chatId, "CUSTOM_CITY_NAME:" + cityName);
                sendMsg(chatId, "en".equals(lang) ? 
                    "✅ Name is available!\n\nNow enter the timezone in format:\n<code>+07</code> or <code>-11</code>" :
                    "✅ Название свободно!\n\nТеперь введите часовой пояс в формате:\n<code>+07</code> или <code>-11</code>");
                return;
            }
            
            if (state.startsWith("CUSTOM_CITY_NAME:")) {
                String cityName = state.substring("CUSTOM_CITY_NAME:".length());
                String timezone = text.trim();
                
                if (!timezone.matches("[+-]\\d{1,2}")) {
                    sendMsg(chatId, "en".equals(lang) ? 
                        "❌ Invalid timezone format. Use +07 or -11" :
                        "❌ Неверный формат часового пояса. Используйте +07 или -11");
                    return;
                }
                
                int hours = Integer.parseInt(timezone.replace("+", "").replace("-", ""));
                if (timezone.startsWith("-")) hours = -hours;
                if (hours > 12 || hours < -12) {
                    sendMsg(chatId, "en".equals(lang) ? 
                        "❌ Timezone must be between -12 and +12" :
                        "❌ Часовой пояс должен быть между -12 и +12");
                    return;
                }
                
                CustomCity customCity = customCityService.createCustomCity(chatId, cityName, timezone);
                userStates.remove(chatId);
                
                String msg = "en".equals(lang) ?
                    "✅ Custom city <b>" + cityName + "</b> created successfully!\n\n🌐 Timezone: UTC" + timezone :
                    "✅ Пользовательский город <b>" + cityName + "</b> успешно создан!\n\n🌐 Часовой пояс: UTC" + timezone;
                sendMsg(chatId, msg);
                sendMsg(chatId, customCityService.getCustomCityInfo(customCity, lang));
                return;
            }
                        if (state.equals(STATE_WAITING_DELETE_CUSTOM_CITY)) {
                userStates.remove(chatId);
                String cityName = text.toLowerCase().trim();
                List<CustomCity> customCities = customCityService.getUserCustomCities(chatId);
                CustomCity cityToDelete = null;
                for (CustomCity cc : customCities) {
                    if (cc.getName().equalsIgnoreCase(cityName)) {
                        cityToDelete = cc;
                        break;
                    }
                }
                if (cityToDelete != null) {
                    customCityService.deleteCustomCity(cityToDelete);
                    sendMsg(chatId, "en".equals(lang) ? 
                        "✅ Custom city deleted successfully!" :
                        "✅ Пользовательский город успешно удалён!");
                } else {
                    sendMsg(chatId, "en".equals(lang) ? 
                        "❌ Custom city not found in your list." :
                        "❌ Пользовательский город не найден в вашем списке.");
                }
                return;
            }
            if (state.startsWith(STATE_WAITING_CHANGE_UTC_CUSTOM + ":")) {
                String cityName = state.substring((STATE_WAITING_CHANGE_UTC_CUSTOM + ":").length());
                CustomCity customCity = customCityService.findCustomCityByNameAndUserId(chatId, cityName);
                if (customCity != null) {
                    String newTz = text.trim();
                    if (!newTz.matches("[+-]\\d{1,2}")) {
                        sendMsg(chatId, "en".equals(lang) ?
                            "❌ Invalid timezone format. Use +07 or -11" :
                            "❌ Неверный формат часового пояса. Используйте +07 или -11");
                        userStates.remove(chatId);
                        return;
                    }
                    
                    int hours = Integer.parseInt(newTz.replace("+", "").replace("-", ""));
                    if (newTz.startsWith("-")) hours = -hours;
                    if (hours > 12 || hours < -12) {
                        sendMsg(chatId, "en".equals(lang) ?
                            "❌ Timezone must be between -12 and +12" :
                            "❌ Часовой пояс должен быть между -12 и +12");
                        userStates.remove(chatId);
                        return;
                    }
                    
                    customCity.setTimezone(newTz);
                    customCityService.updateCustomCity(customCity);
                    String displayName = customCity.getName().substring(0, 1).toUpperCase() + customCity.getName().substring(1);
                    sendMsg(chatId, "en".equals(lang) ?
                        "✅ Timezone for <b>" + displayName + "</b> updated to UTC" + newTz :
                        "✅ Часовой пояс для <b>" + displayName + "</b> обновлён на UTC" + newTz);
                }
                userStates.remove(chatId);
                return;
            }

            if (state.equals(STATE_WAITING_HOME_CITY)) {
                userStates.remove(chatId);
                String cityName = text.toLowerCase().trim();
                City city = cityService.findCity(cityName);
                if (city != null) {
                    userService.setHomeCity(chatId, cityName);
                    String displayName = cityService.getCityNameLocalized(city, lang);
                    sendMsg(chatId, "en".equals(lang) ?
                        "✅ Home city set to <b>" + displayName + "</b>" :
                        "✅ Домашний город установлен: <b>" + displayName + "</b>");
                } else {
                    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                    List<InlineKeyboardButton> row = new ArrayList<>();
                    row.add(createInlineButton("en".equals(lang) ? "❌ Cancel" : "❌ Отмена", "cancel_home_city"));
                    rows.add(row);
                    markup.setKeyboard(rows);
                    sendMsg(chatId, "en".equals(lang) ?
                        "❌ This city is not in the database. Please enter another city." :
                        "❌ Данного города нет в базе данных. Введите другой город.", markup);
                    userStates.put(chatId, STATE_WAITING_HOME_CITY);
                }
                return;
            }

            if (state.startsWith(STATE_WAITING_CHANGE_NAME_CUSTOM + ":")) {
                String cityName = state.substring((STATE_WAITING_CHANGE_NAME_CUSTOM + ":").length());
                CustomCity customCity = customCityService.findCustomCityByNameAndUserId(chatId, cityName);
                if (customCity != null) {
                    String newName = text.toLowerCase().trim();
                    String oldName = customCity.getName();
                    customCity.setName(newName);
                    customCityService.updateCustomCity(customCity);
                    String displayName = newName.substring(0, 1).toUpperCase() + newName.substring(1);
                    sendMsg(chatId, "en".equals(lang) ?
                        "✅ City renamed from <b>" + oldName + "</b> to <b>" + displayName + "</b>" :
                        "✅ Город переименован из <b>" + oldName + "</b> в <b>" + displayName + "</b>");
                }
                userStates.remove(chatId);
                return;
            }

            // ===== НАПОМИНАЛКА: ОЖИДАНИЕ ГОРОДА =====
            if (state.equals(STATE_WAITING_REMINDER_CITY)) {
                String cityName = text.toLowerCase().trim();
                City city = cityService.findCity(cityName);
                if (city != null) {
                    userStates.put(chatId, "REMINDER_WAIT_TIME:" + cityName);
                    sendMsg(chatId, "en".equals(lang) ?
                        "⏰ Step 2: Enter the time (e.g., 15:00):" :
                        "⏰ Шаг 2: Введите время (например, 15:00):");
                } else {
                    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                    List<InlineKeyboardButton> row = new ArrayList<>();
                    row.add(createInlineButton("en".equals(lang) ? "❌ Cancel" : "❌ Отмена", "cancel_reminder"));
                    rows.add(row);
                    markup.setKeyboard(rows);
                    sendMsg(chatId, "en".equals(lang) ?
                        "❌ City not found. Try again or cancel." :
                        "❌ Город не найден. Попробуйте снова или отмените.", markup);
                }
                return;
            }

            // ===== НАПОМИНАЛКА: ОЖИДАНИЕ ВРЕМЕНИ =====
            if (state.startsWith("REMINDER_WAIT_TIME:")) {
                String cityName = state.substring("REMINDER_WAIT_TIME:".length());
                String time = text.trim();
                
                if (!time.matches("\\d{1,2}:\\d{2}")) {
                    sendMsg(chatId, "en".equals(lang) ?
                        "❌ Invalid time format. Use HH:MM (e.g., 15:00)." :
                        "❌ Неверный формат времени. Используйте ЧЧ:ММ (например, 15:00).");
                    return;
                }
                
                // Добавляем ведущий ноль если нужно (9:00 -> 09:00)
                if (time.length() == 4) {
                    time = "0" + time;
                }
                
                userStates.put(chatId, "REMINDER_WAIT_TEXT:" + cityName + ";;" + time);
                sendMsg(chatId, "en".equals(lang) ?
                    "⏰ Step 3: Enter the reminder text:" :
                    "⏰ Шаг 3: Введите текст напоминалки:");
                return;
            }

            // ===== НАПОМИНАЛКА: ОЖИДАНИЕ ТЕКСТА =====
            if (state.startsWith("REMINDER_WAIT_TEXT:")) {
                String data = state.substring("REMINDER_WAIT_TEXT:".length());
                String[] parts = data.split(";;", 2);
                String cityName = parts[0];
                String time = parts[1];
                String reminderText = text.trim();
                
                userStates.put(chatId, "REMINDER_WAIT_DAYS:" + cityName + ";;" + time + ";;" + reminderText);
                
                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                
                if ("en".equals(lang)) {
                    rows.add(Arrays.asList(
                        createInlineButton("Monday", "day_mon"),
                        createInlineButton("Tuesday", "day_tue")
                    ));
                    rows.add(Arrays.asList(
                        createInlineButton("Wednesday", "day_wed"),
                        createInlineButton("Thursday", "day_thu")
                    ));
                    rows.add(Arrays.asList(
                        createInlineButton("Friday", "day_fri"),
                        createInlineButton("Saturday", "day_sat")
                    ));
                    rows.add(Arrays.asList(
                        createInlineButton("Sunday", "day_sun"),
                        createInlineButton("✅ All Days", "day_all")
                    ));
                } else {
                    rows.add(Arrays.asList(
                        createInlineButton("Понедельник", "day_пн"),
                        createInlineButton("Вторник", "day_вт")
                    ));
                    rows.add(Arrays.asList(
                        createInlineButton("Среда", "day_ср"),
                        createInlineButton("Четверг", "day_чт")
                    ));
                    rows.add(Arrays.asList(
                        createInlineButton("Пятница", "day_пт"),
                        createInlineButton("Суббота", "day_сб")
                    ));
                    rows.add(Arrays.asList(
                        createInlineButton("Воскресенье", "day_вс"),
                        createInlineButton("✅ Все дни", "day_all")
                    ));
                }
                
                markup.setKeyboard(rows);
                sendMsg(chatId, "en".equals(lang) ?
                    "⏰ Step 4: Select the days:" :
                    "⏰ Шаг 4: Выберите дни:", markup);
                return;
            }

            // ===== НАПОМИНАЛКА: ОЖИДАНИЕ ДНЕЙ =====
            if (state.startsWith("REMINDER_WAIT_DAYS:")) {
                String data = state.substring("REMINDER_WAIT_DAYS:".length());
                String[] parts = data.split(";;", 3);
                String cityName = parts[0];
                String time = parts[1];
                String reminderText = parts[2];
                String days = text.toLowerCase().trim();
                
                // Валидация дней
                if (!ReminderScheduler.validateDays(days)) {
                    sendMsg(chatId, "en".equals(lang) ?
                        "❌ Invalid days format. Use: mon,tue,wed or пн,вт,ср or all" :
                        "❌ Неверный формат дней. Используйте: пн,вт,ср или mon,tue,wed или все");
                    return;
                }
                
                reminderService.createReminder(chatId, cityName, time, reminderText, days);
                userStates.remove(chatId);
                
                sendMsg(chatId, "en".equals(lang) ?
                    "✅ Reminder created!\n\n🌍 City: " + cityName + "\n🕐 Time: " + time + "\n📝 Text: " + reminderText + "\n📅 Days: " + days + "\n\nYou will receive a notification when it's time." :
                    "✅ Напоминалка создана!\n\n🌍 Город: " + cityName + "\n🕐 Время: " + time + "\n📝 Текст: " + reminderText + "\n📅 Дни: " + days + "\n\nВы получите уведомление, когда придёт время.");
                return;
            }

            if (state.equals(STATE_WAITING_CITY)) {
                userStates.remove(chatId);
                City city = cityService.findCity(text);
                if (city != null) sendMsg(chatId, cityService.getCityInfo(city, lang, user.getTimeFormat()), getCityActionsKeyboard(city.getName(), lang));
                else sendMsg(chatId, "en".equals(lang) ? "❌ City not found." : "❌ Город не найден.");
            } else if (state.equals(STATE_WAITING_REMOVE)) {
                userStates.remove(chatId);
                String cityToRemove = text.toLowerCase().trim();
                
                // Проверяем обычные города
                if (userService.hasFavorite(chatId, cityToRemove)) {
                    userService.removeFavorite(chatId, cityToRemove);
                    sendMsg(chatId, "en".equals(lang) ? "✅ City removed from favorites." : "✅ Город удален из избранных.");
                    return;
                }
                
                // Проверяем пользовательские города
                List<CustomCity> customCities = customCityService.getUserCustomCities(chatId);
                CustomCity customCityToRemove = null;
                for (CustomCity cc : customCities) {
                    if (cc.getName().equalsIgnoreCase(cityToRemove)) {
                        customCityToRemove = cc;
                        break;
                    }
                }
                
                if (customCityToRemove != null) {
                    customCityService.deleteCustomCity(customCityToRemove);
                    String displayName = cityToRemove.substring(0, 1).toUpperCase() + cityToRemove.substring(1);
                    sendMsg(chatId, "en".equals(lang) ?
                        "✅ Custom city <b>" + displayName + "</b> deleted." :
                        "✅ Пользовательский город <b>" + displayName + "</b> удалён.");
                } else {
                    sendMsg(chatId, "en".equals(lang) ?
                        "❌ City not found in favorites." :
                        "❌ Город не найден в избранном.");
                }
            } else if (state.equals(STATE_WAITING_BROADCAST)) {
                userStates.remove(chatId);
                if (user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.OWNER) sendBroadcast(text, lang);
            }
            return;
        }

        City city = cityService.findCity(text);
        if (city != null) {
            sendMsg(chatId, cityService.getCityInfo(city, lang, user.getTimeFormat()), getCityActionsKeyboard(city.getName(), lang));
        } else {
            sendMsg(chatId, "en".equals(lang) ? "❓ Unknown command or city not found. Use /help" : "❓ Неизвестная команда или город не найден. Используйте /help");
        }
    }

    private void handleCallback(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String data = update.getCallbackQuery().getData();
        User user = userService.getUser(chatId);
        if (user == null) return;
        String lang = user.getLanguage();
        answerCallback(update.getCallbackQuery().getId());

        if (data.equals("lang_ru")) {
            userService.setLanguage(chatId, "ru");
            editMessageText(chatId, messageId, "✅ Язык изменён на Русский!");
            sendWelcome(chatId, "ru");
        } else if (data.equals("lang_en")) {
            userService.setLanguage(chatId, "en");
            editMessageText(chatId, messageId, "✅ Language changed to English!");
            sendWelcome(chatId, "en");
        } else if (data.equals("settings_lang")) {
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(createInlineButton("🇷🇺 Русский", "settings_lang_ru"));
            row1.add(createInlineButton("🇬🇧 English", "settings_lang_en"));
            rows.add(row1);
            List<InlineKeyboardButton> row2 = new ArrayList<>();
            row2.add(createInlineButton("en".equals(lang) ? "🔙 Back" : "🔙 Назад", "settings_back"));
            rows.add(row2);
            markup.setKeyboard(rows);
            editMessageText(chatId, messageId, "en".equals(lang) ? "🌐 Choose language:" : "🌐 Выберите язык:", markup);
        } else if (data.equals("settings_lang_ru")) {
            userService.setLanguage(chatId, "ru");
            editMessageText(chatId, messageId, "✅ Язык успешно изменен на русский.");
            User updatedUser = userService.getUser(chatId);
            showSettings(chatId, "ru", updatedUser);
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("🏠 Главное меню:");
            msg.setReplyMarkup(getMainMenuKeyboard("ru"));
            try {
                execute(msg);
            } catch (TelegramApiException e) {
                log.error("Send error: " + e.getMessage());
            }
        } else if (data.equals("settings_lang_en")) {
            userService.setLanguage(chatId, "en");
            editMessageText(chatId, messageId, "✅ Language successfully changed to English.");
            User updatedUser = userService.getUser(chatId);
            showSettings(chatId, "en", updatedUser);
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("🏠 Main menu:");
            msg.setReplyMarkup(getMainMenuKeyboard("en"));
            try {
                execute(msg);
            } catch (TelegramApiException e) {
                log.error("Send error: " + e.getMessage());
            }
        } else if (data.equals("settings_home")) {
            String homeCity = userService.getHomeCity(chatId);
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            
            if (homeCity != null) {
                City city = cityService.findCity(homeCity);
                if (city != null) {
                    String displayName = cityService.getCityNameLocalized(city, lang);
                    String countryName = cityService.getCountryLocalized(city, lang);
                    java.time.ZonedDateTime cityTime = java.time.ZonedDateTime.now(java.time.ZoneId.of(city.getTimezone()));
                    String timePattern = "12".equals(user.getTimeFormat()) ? "hh:mm a" : "HH:mm";
                    String timeStr = cityTime.format(java.time.format.DateTimeFormatter.ofPattern(timePattern, "en".equals(lang) ? java.util.Locale.ENGLISH : new java.util.Locale("ru")));
                    int offsetSeconds = cityTime.getOffset().getTotalSeconds();
                    int offsetHours = offsetSeconds / 3600;
                    String utcStr = String.format("UTC%s%02d", offsetHours >= 0 ? "+" : "-", Math.abs(offsetHours));
                    
                    String text = "🏠 <b>" + ("en".equals(lang) ? "Your Home City" : "Ваш домашний город") + "</b>\n\n" +
                        "🌍 <b>" + displayName + "</b>\n" +
                        "   📍 " + countryName + "\n" +
                        "   🕐 " + timeStr + " | 🌐 " + utcStr;
                    
                    List<InlineKeyboardButton> row1 = new ArrayList<>();
                    row1.add(createInlineButton("en".equals(lang) ? "✏️ Change" : "✏️ Изменить", "settings_home_change"));
                    rows.add(row1);
                    List<InlineKeyboardButton> row2 = new ArrayList<>();
                    row2.add(createInlineButton("🗑️ " + ("en".equals(lang) ? "Delete" : "Удалить"), "settings_home_delete"));
                    rows.add(row2);
                    List<InlineKeyboardButton> row3 = new ArrayList<>();
                    row3.add(createInlineButton("🔙 " + ("en".equals(lang) ? "Back" : "Назад"), "settings_back"));
                    rows.add(row3);
                    markup.setKeyboard(rows);
                    editMessageText(chatId, messageId, text, markup);
                }
            } else {
                String text = "en".equals(lang) ?
                    "🏠 You haven't set a home city yet." :
                    "🏠 Вы еще не задали домашний город.";
                
                List<InlineKeyboardButton> row1 = new ArrayList<>();
                row1.add(createInlineButton("➕ " + ("en".equals(lang) ? "Set home city" : "Задать домашний город"), "settings_home_change"));
                rows.add(row1);
                List<InlineKeyboardButton> row2 = new ArrayList<>();
                row2.add(createInlineButton("🔙 " + ("en".equals(lang) ? "Back" : "Назад"), "settings_back"));
                rows.add(row2);
                markup.setKeyboard(rows);
                editMessageText(chatId, messageId, text, markup);
            }
            return;
        } else if (data.equals("settings_home_change")) {
            userStates.put(chatId, STATE_WAITING_HOME_CITY);
            editMessageText(chatId, messageId, "en".equals(lang) ?
                "🏠 Enter your home city name:" :
                "🏠 Введите название вашего домашнего города:");
            return;
        } else if (data.equals("cancel_home_city")) {
            userStates.remove(chatId);
            editMessageText(chatId, messageId, "en".equals(lang) ? "✅ Action cancelled." : "✅ Действие отменено.");
        } else if (data.equals("delete_reminder_btn")) {
            reminderService.deleteReminder(chatId);
            editMessageText(chatId, messageId, "en".equals(lang) ? "✅ Reminder deleted." : "✅ Напоминалка удалена.");
        } else if (data.equals("cancel_reminder")) {
            userStates.remove(chatId);
            editMessageText(chatId, messageId, "en".equals(lang) ? "✅ Reminder creation cancelled." : "✅ Создание напоминалки отменено.");
        } else if (data.startsWith("day_")) {
            String state = userStates.get(chatId);
            if (state != null && state.startsWith("REMINDER_WAIT_DAYS:")) {
                String daysData = state.substring("REMINDER_WAIT_DAYS:".length());
                String[] parts = daysData.split(";;", 3);
                String cityName = parts[0];
                String time = parts[1];
                String reminderText = parts[2];
                
                String day = data.substring("day_".length());
                
                reminderService.createReminder(chatId, cityName, time, reminderText, day);
                userStates.remove(chatId);
                
                editMessageText(chatId, messageId, "en".equals(lang) ?
                    "✅ Reminder created!\n\n🌍 City: " + cityName + "\n🕐 Time: " + time + "\n📝 Text: " + reminderText + "\n📅 Days: " + day + "\n\nYou will receive a notification when it's time." :
                    "✅ Напоминалка создана!\n\n🌍 Город: " + cityName + "\n🕐 Время: " + time + "\n📝 Текст: " + reminderText + "\n📅 Дни: " + day + "\n\nВы получите уведомление, когда придёт время.");
            }
        } else if (data.equals("settings_home_delete")) {
            userService.removeHomeCity(chatId);
            User updatedUser = userService.getUser(chatId);
            showSettings(chatId, updatedUser.getLanguage(), updatedUser);
            return;
        } else if (data.equals("settings_timeformat")) {
            String currentFormat = user.getTimeFormat();
            String text = "en".equals(lang) ?
                "🕐 <b>Time format</b>\n\nChoose your preferred format:" :
                "🕐 <b>Формат времени</b>\n\nВыберите предпочтительный формат:";
            
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            String format24text = "24".equals(currentFormat) ? "✅ 24 " + ("en".equals(lang) ? "hours" : "часа") + " (14:30)" : "24 " + ("en".equals(lang) ? "hours" : "часа") + " (14:30)";
            row1.add(createInlineButton(format24text, "timeformat_24"));
            rows.add(row1);
            
            List<InlineKeyboardButton> row2 = new ArrayList<>();
            String format12text = "12".equals(currentFormat) ? "✅ 12 " + ("en".equals(lang) ? "hours (AM/PM)" : "часов (AM/PM)") + " (02:30 PM)" : "12 " + ("en".equals(lang) ? "hours (AM/PM)" : "часов (AM/PM)") + " (02:30 PM)";
            row2.add(createInlineButton(format12text, "timeformat_12"));
            rows.add(row2);
            
            List<InlineKeyboardButton> row3 = new ArrayList<>();
            row3.add(createInlineButton("🔙 " + ("en".equals(lang) ? "Back" : "Назад"), "settings_back_to_main"));
            rows.add(row3);
            
            markup.setKeyboard(rows);
            editMessageText(chatId, messageId, text, markup);
        } else if (data.equals("timeformat_24")) {
            userService.setTimeFormat(chatId, "24");
            User updatedUser = userService.getUser(chatId);
            editMessageText(chatId, messageId, updatedUser.getLanguage().equals("en") ? 
                "✅ Time format changed to 24 hours" : 
                "✅ Формат времени изменен на 24 часа");
        } else if (data.equals("timeformat_12")) {
            userService.setTimeFormat(chatId, "12");
            User updatedUser = userService.getUser(chatId);
            editMessageText(chatId, messageId, updatedUser.getLanguage().equals("en") ? 
                "✅ Time format changed to 12 hours (AM/PM)" : 
                "✅ Формат времени изменен на 12 часов (AM/PM)");
        } else if (data.equals("settings_back_to_main")) {
            User updatedUser = userService.getUser(chatId);
            showSettings(chatId, updatedUser.getLanguage(), updatedUser);
        } else if (data.equals("settings_back")) {
            editMessageText(chatId, messageId, "en".equals(lang) ? "🏠 Main menu:" : "🏠 Главное меню:");
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("en".equals(lang) ? "🏠 Main menu:" : "🏠 Главное меню:");
            msg.setReplyMarkup(getMainMenuKeyboard(lang));
            try {
                execute(msg);
            } catch (TelegramApiException e) {
                log.error("Send error: " + e.getMessage());
            }
        } else if (data.equals("show_download")) {
            showDownloadMenu(chatId, lang);
        } else if (data.equals("download_windows")) {
            showWindowsInstructions(chatId, lang);
        } else if (data.equals("download_android")) {
            showAndroidInstructions(chatId, lang);
        } else if (data.equals("download_linux")) {
            showLinuxInstructions(chatId, lang);
        } else if (data.equals("remove_fav:start")) {
            userStates.put(chatId, STATE_WAITING_REMOVE);
            editMessageText(chatId, messageId, "en".equals(lang) ? "🗑️ Enter city name to remove.\nCancel: /cancel" : "🗑️ Напишите название города для удаления.\nДля отмены: /cancel");
        } else if (data.equals("admin_broadcast")) {
            if (user.getRole() != User.Role.ADMIN && user.getRole() != User.Role.OWNER) {
                editMessageText(chatId, messageId, "❌ Admins/Owner only");
                return;
            }
        } else if (data.equals("start_lang_ru")) {
            userService.setLanguage(chatId, "ru");
            String text_msg = "📢 <b>Подписка на канал</b>\n\nЧтобы продолжить, подпишитесь на наш канал:\n\n👉 <a href=\"https://t.me/WorldAtlasMap\">@WorldTimeMap</a>\n\nПосле подписки нажмите кнопку ниже:";
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(createInlineButton("📢 Открыть канал", "https://t.me/WorldAtlasMap"));
            rows.add(row1);
            List<InlineKeyboardButton> row2 = new ArrayList<>();
            row2.add(createInlineButton("✅ Проверить подписку", "check_subscription"));
            rows.add(row2);
            markup.setKeyboard(rows);
            editMessageText(chatId, messageId, text_msg, markup);
            userStates.put(chatId, STATE_WAITING_SUBSCRIPTION);
        } else if (data.equals("start_lang_en")) {
            userService.setLanguage(chatId, "en");
            String text_msg = "📢 <b>Subscribe to channel</b>\n\nTo continue, subscribe to our channel:\n\n👉 <a href=\"https://t.me/WorldAtlasMap\">@WorldTimeMap</a>\n\nAfter subscribing, click the button below:";
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(createInlineButton("📢 Open channel", "https://t.me/WorldAtlasMap"));
            rows.add(row1);
            List<InlineKeyboardButton> row2 = new ArrayList<>();
            row2.add(createInlineButton("✅ Check subscription", "check_subscription"));
            rows.add(row2);
            markup.setKeyboard(rows);
            editMessageText(chatId, messageId, text_msg, markup);
            userStates.put(chatId, STATE_WAITING_SUBSCRIPTION);
        } else if (data.equals("check_subscription")) {
            try {
                GetChatMember getChatMember = new GetChatMember();
                getChatMember.setChatId("@WorldTimeMap");
                getChatMember.setUserId(chatId);
                ChatMember member = execute(getChatMember);
                String status = member.getStatus();
                boolean isSubscribed = "member".equals(status) || "administrator".equals(status) || "creator".equals(status);
                
                if (isSubscribed) {
                    userService.setSubscribed(chatId, true);
                    userStates.remove(chatId);
                    editMessageText(chatId, messageId, "en".equals(lang) ? "✅ Subscription confirmed!" : "✅ Подписка подтверждена!");
                    User updatedUser = userService.getUser(chatId);
                    sendWelcome(chatId, updatedUser.getLanguage());
                } else {
                    String errorMsg = "en".equals(lang) ?
                        "❌ You are not subscribed to @WorldTimeMap yet.\n\nPlease subscribe and try again." :
                        "❌ Вы еще не подписаны на @WorldTimeMap.\n\nПожалуйста, подпишитесь и попробуйте снова.";
                    editMessageText(chatId, messageId, errorMsg);
                }
            } catch (TelegramApiException e) {
                log.error("Check subscription error: " + e.getMessage());
                editMessageText(chatId, messageId, "❌ Error checking subscription. Try again.");
            }
        } else if (data.startsWith("add_fav:")) {
            String cityName = data.substring(8);
            
            // Проверяем, есть ли город уже в избранном
            if (userService.hasFavorite(chatId, cityName)) {
                String msg = "en".equals(lang) ? 
                    "⚠️ This city is already in your favorites." :
                    "⚠️ Этот город уже есть в вашем избранном.";
                sendMsg(chatId, msg);
            } else {
                userService.addFavorite(chatId, cityName);
                String displayName = cityName.substring(0, 1).toUpperCase() + cityName.substring(1);
                String msg = "en".equals(lang) ?
                    "✅ City <b>" + displayName + "</b> successfully added to favorites!" :
                    "✅ Город <b>" + displayName + "</b> успешно добавлен в избранное!";
                sendMsg(chatId, msg);
            }
        }

    }

    private void addCityToFavorites(long chatId, String cityName, String lang) {
        if (cityName == null || cityName.isEmpty()) return;
        City city = cityService.findCity(cityName);
        if (city == null) { 
            sendMsg(chatId, "en".equals(lang) ? "❌ City not found." : "❌ Город не найден."); 
            return; 
        }
        if (userService.hasFavorite(chatId, cityName)) {
            sendMsg(chatId, "en".equals(lang) ? "⚠️ Already in favorites." : "⚠️ Этот город уже в вашем избранном.");
        } else {
            userService.addFavorite(chatId, cityName);
            String displayName = cityService.getCityNameLocalized(city, lang);
            String msg = "en".equals(lang) ? 
                "✅ City " + displayName + " successfully added to favorites!" :
                "✅ Город " + displayName + " успешно добавлен в избранное!";
            sendMsg(chatId, msg);
        }
    }


    private void showSettings(long chatId, String lang, User user) {
        String currentLang = "en".equals(user.getLanguage()) ? "English" : "Русский";
        String currentTimeFormat = "24".equals(user.getTimeFormat()) ?
            ("en".equals(lang) ? "24 hours" : "24 часа") :
            ("en".equals(lang) ? "12 hours (AM/PM)" : "12 часов (AM/PM)");
        
        String text = "en".equals(lang) ?
            "⚙️ <b>Settings</b>\n\n" +
            "🌐 Language: " + currentLang + "\n" +
            "🕐 Time format: " + currentTimeFormat :
            "⚙️ <b>Настройки</b>\n\n" +
            "🌐 Язык: " + currentLang + "\n" +
            "🕐 Формат времени: " + currentTimeFormat;
        
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInlineButton("🌐 " + ("en".equals(lang) ? "Language" : "Язык"), "settings_lang"));
        rows.add(row1);
        
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInlineButton("🕐 " + ("en".equals(lang) ? "Time format" : "Формат времени"), "settings_timeformat"));
        rows.add(row2);
        
        List<InlineKeyboardButton> row2b = new ArrayList<>();
        row2b.add(createInlineButton("🏠 " + ("en".equals(lang) ? "Home city" : "Домашний город"), "settings_home"));
        rows.add(row2b);
        
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createInlineButton("🔙 " + ("en".equals(lang) ? "Back" : "Назад"), "settings_back"));
        rows.add(row3);
        
        markup.setKeyboard(rows);
        sendMsg(chatId, text, markup);
    }

    private void sendWelcome(long chatId, String lang) {
        String text = "en".equals(lang) ?
            "🌍 <b>Welcome to WorldAtlas Bot!</b>\n\n👋 Hello! I track time anywhere in the world.\n\n✨ <b>Features:</b>\n🕐 Exact time in any city\n📅 Current date and day\n🗺️ Country and continent\n🌐 Timezone and difference\n⭐ Favorite cities\n📥 Download app\n🌐 Change language\n🔍 Inline mode via @" + botUsername + "\n\n📋 <b>Commands:</b>\n• /search_city Moscow\n• /view_favorites\n• /download - Get the app\n• /lang - Change language\n• /help - Help\n\n🚀 <b>Ready?</b> Use menu below 👇" :
            "🌍 <b>Добро пожаловать в WorldAtlas Bot!</b>\n\n👋 Привет! Я отслеживаю время в любой точке мира.\n\n✨ <b>Возможности:</b>\n🕐 Точное время в любом городе\n📅 Текущая дата и день\n🗺️ Страна и материк\n🌐 Часовой пояс и разница\n⭐ Избранные города\n📥 Скачать приложение\n🌐 Сменить язык\n🔍 Inline-режим через @" + botUsername + "\n\n📋 <b>Команды:</b>\n• /search_city Москва\n• /view_favorites\n• /worldclock - Мировые часы\n• /download - Скачать приложение\n• /lang - Сменить язык\n• /help - Помощь\n\n🚀 <b>Готовы?</b> Используйте меню ниже 👇";
        sendMsg(chatId, text, getMainMenuKeyboard(lang));
    }

    private void showDownloadMenu(long chatId, String lang) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(createInlineButton("💻 Windows", "download_windows")));
        rows.add(List.of(createInlineButton("📱 Android", "download_android")));
        rows.add(List.of(createInlineButton("🐧 Linux", "download_linux")));
        markup.setKeyboard(rows);
        String text = "en".equals(lang) ? "📥 <b>Download WorldAtlas App</b>\n\nChoose your platform:" : "📥 <b>Скачать приложение WorldAtlas</b>\n\nВыберите вашу платформу:";
        sendMsg(chatId, text, markup);
    }

    private void showWindowsInstructions(long chatId, String lang) {
        String text = "en".equals(lang) ? 
            "💻 <b>Download for Windows</b>\n\n📥 <a href=\"https://drive.google.com/file/d/1H_-f3yTqhGvstVwLw37paltGU5TQHgWD/view\">Download file</a>\n\n<b>Instructions:</b>\n1. Click the link above\n2. Click the Download button\n3. Open the downloaded file\n4. Follow the installer instructions\n5. Launch the application" :
            "💻 <b>Скачать для Windows</b>\n\n📥 <a href=\"https://drive.google.com/file/d/1H_-f3yTqhGvstVwLw37paltGU5TQHgWD/view\">Скачать файл</a>\n\n<b>Инструкция:</b>\n1. Нажмите на ссылку выше\n2. Нажмите кнопку Скачать\n3. Откройте скачанный файл\n4. Следуйте инструкциям установщика\n5. Запустите приложение";
        sendMsg(chatId, text, getBackKeyboard(lang));
    }

    private void showAndroidInstructions(long chatId, String lang) {
        String text = "en".equals(lang) ? 
            "📱 <b>Download for Android</b>\n\n📥 <a href=\"https://drive.google.com/file/d/1cHzF9ba-91bKSv_3G7Wn7-fEfNPDHETR/view\">Download APK</a>\n\n<b>Instructions:</b>\n1. Click the link above\n2. Click Download\n3. Open phone settings\n4. Allow installation from unknown sources\n5. Open the downloaded APK file\n6. Tap Install" :
            "📱 <b>Скачать для Android</b>\n\n📥 <a href=\"https://drive.google.com/file/d/1cHzF9ba-91bKSv_3G7Wn7-fEfNPDHETR/view\">Скачать APK</a>\n\n<b>Инструкция:</b>\n1. Нажмите на ссылку выше\n2. Нажмите Скачать\n3. Откройте настройки телефона\n4. Разрешите установку из неизвестных источников\n5. Откройте скачанный APK файл\n6. Нажмите Установить";
        sendMsg(chatId, text, getBackKeyboard(lang));
    }

    private void showLinuxInstructions(long chatId, String lang) {
        String text = "en".equals(lang) ? 
            "🐧 <b>Download for Linux</b>\n\n📥 <a href=\"https://drive.google.com/file/d/1F2324DmMfWR2UIQGD-sSMLliGevMKXsi/view\">Download file</a>\n\n<b>Instructions:</b>\n1. Click the link above\n2. Click Download\n3. Open terminal in the folder with the file\n4. Make the file executable: <code>chmod +x worldatlas-linux</code>\n5. Run: <code>./worldatlas-linux</code>" :
            "🐧 <b>Скачать для Linux</b>\n\n📥 <a href=\"https://drive.google.com/file/d/1F2324DmMfWR2UIQGD-sSMLliGevMKXsi/view\">Скачать файл</a>\n\n<b>Инструкция:</b>\n1. Нажмите на ссылку выше\n2. Нажмите Скачать\n3. Откройте терминал в папке с файлом\n4. Сделайте файл исполняемым: <code>chmod +x worldatlas-linux</code>\n5. Запустите: <code>./worldatlas-linux</code>";
        sendMsg(chatId, text, getBackKeyboard(lang));
    }

    private void sendBroadcast(String text, String lang) {
        List<User> users = userService.getAllUsers();
        int successCount = 0;
        for (User u : users) {
            if (!u.isBanned() || u.getChatId().equals(UserService.MAIN_ADMIN_ID)) {
                try {
                    SendMessage msg = new SendMessage();
                    msg.setChatId(u.getChatId());
                    msg.setText("📢 <b>Объявление / Announcement</b>\n\n" + text);
                    msg.setParseMode("HTML");
                    execute(msg);
                    successCount++;
                    Thread.sleep(30);
                } catch (Exception e) { log.error("Failed to send to {}", u.getChatId()); }
            }
        }
        sendMsg(UserService.MAIN_ADMIN_ID, "✅ Рассылка завершена. Отправлено: " + successCount + " / " + users.size());
    }

    private void notifyAdmin(String message) {
        try {
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(UserService.MAIN_ADMIN_ID));
            msg.setText(message);
            msg.setParseMode("HTML");
            execute(msg);
        } catch (Exception e) { log.warn("Не удалось отправить уведомление админу: {}", e.getMessage()); }
    }

    private InlineKeyboardButton createInlineButton(String text, String callbackData) {
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(text);
        if (callbackData.startsWith("http://") || callbackData.startsWith("https://")) {
            btn.setUrl(callbackData);
        } else {
            btn.setCallbackData(callbackData);
        }
        return btn;
    }


    private ReplyKeyboardMarkup getMainMenuKeyboard(String lang) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();
        if ("en".equals(lang)) {
            rows.add(createRow("🔍 Search", "⭐ Favorites"));
            rows.add(createRow("📥 Download", "⚙️ Settings"));
            rows.add(createRow("🏙️ Custom City", "📖 Help"));
            rows.add(createRow("💬 Support", "❌ Cancel"));
        } else {
            rows.add(createRow("🔍 Поиск", "⭐ Избранное"));
            rows.add(createRow("📥 Скачать", "⚙️ Настройки"));
            rows.add(createRow("🏙️ Пользовательский город", "📖 Помощь"));
            rows.add(createRow("💬 Поддержка", "❌ Отмена"));
        }
        
        markup.setKeyboard(rows);
        return markup;
    }

    private KeyboardRow createRow(String btn1, String btn2) {
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton(btn1));
        row.add(new KeyboardButton(btn2));
        return row;
    }

    private InlineKeyboardMarkup getCityActionsKeyboard(String cityName, String lang) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createInlineButton("en".equals(lang) ? "⭐ Add to Favorites" : "⭐ В избранное", "add_fav:" + cityName));
        rows.add(row);
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup getBackKeyboard(String lang) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        InlineKeyboardButton backBtn = new InlineKeyboardButton();
        backBtn.setText("en".equals(lang) ? "⬅️ Back" : "⬅️ Назад");
        backBtn.setCallbackData("show_download");
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(backBtn);
        rows.add(row);
        markup.setKeyboard(rows);
        return markup;
    }

    private void sendMsg(long chatId, String text) {
        try {
            SendMessage msg = new SendMessage();
            msg.setChatId(chatId);
            msg.setText(text);
            msg.setParseMode("HTML");
            execute(msg);
        } catch (TelegramApiException e) { log.error("Send error: {}", e.getMessage()); }
    }

    private void sendMsg(long chatId, String text, ReplyKeyboardMarkup markup) {
        try {
            SendMessage msg = new SendMessage();
            msg.setChatId(chatId);
            msg.setText(text);
            msg.setParseMode("HTML");
            msg.setReplyMarkup(markup);
            execute(msg);
        } catch (TelegramApiException e) { log.error("Send error: {}", e.getMessage()); }
    }

    private void sendMsg(long chatId, String text, InlineKeyboardMarkup markup) {
        try {
            SendMessage msg = new SendMessage();
            msg.setChatId(chatId);
            msg.setText(text);
            msg.setParseMode("HTML");
            msg.setReplyMarkup(markup);
            execute(msg);
        } catch (TelegramApiException e) { log.error("Send error: {}", e.getMessage()); }
    }

    private void editMessageText(long chatId, int messageId, String text, InlineKeyboardMarkup markup) {
        try {
            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(String.valueOf(chatId));
            editMessage.setMessageId(messageId);
            editMessage.setText(text);
            editMessage.setParseMode("HTML");
            editMessage.setReplyMarkup(markup);
            execute(editMessage);
        } catch (TelegramApiException e) {
            log.error("Edit message error: " + e.getMessage());
        }
    }

    private void editMessageText(long chatId, int messageId, String text) {
        try {
            EditMessageText msg = new EditMessageText();
            msg.setChatId(chatId);
            msg.setMessageId(messageId);
            msg.setText(text);
            msg.setParseMode("HTML");
            execute(msg);
        } catch (TelegramApiException e) { log.error("Edit error: {}", e.getMessage()); }
    }

    private void answerCallback(String callbackId) {
        try {
            org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery answer = new org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery();
            answer.setCallbackQueryId(callbackId);
            answer.setShowAlert(false);
            execute(answer);
        } catch (Exception ignored) {}
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private void handleInlineQuery(InlineQuery inlineQuery) {
        String query = inlineQuery.getQuery().trim();
        if (query.length() < 2) return;
        List<City> cities = cityService.searchCities(query);
        List<InlineQueryResult> results = new ArrayList<>();
        int limit = Math.min(cities.size(), 30);
        for (int i = 0; i < limit; i++) {
            City city = cities.get(i);
            InlineQueryResultArticle article = new InlineQueryResultArticle();
            article.setId("city_" + i);
            article.setTitle(capitalize(city.getName()));
            article.setDescription(city.getCountry());
            InputTextMessageContent content = new InputTextMessageContent();
            content.setMessageText(cityService.getCityInfo(city, "ru", "24"));
            content.setParseMode("HTML");
            article.setInputMessageContent(content);
            results.add(article);
        }
        try {
            AnswerInlineQuery answer = new AnswerInlineQuery();
            answer.setInlineQueryId(inlineQuery.getId());
            answer.setResults(results);
            answer.setCacheTime(60);
            execute(answer);
        } catch (TelegramApiException e) { log.error("Inline error: {}", e.getMessage()); }
    }

    public void onCallbackQueryReceived(org.telegram.telegrambots.meta.api.objects.CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String callbackId = callbackQuery.getId();
        
        try {
            if (data.equals("admin_stats")) {
                answerCallback(callbackId);
                long totalUsers = userService.getAllUsers().size();
                long totalTickets = supportService.getAllTickets().size();
                long newTickets = supportService.getUnanswered().size();
                
                String body = "📊 <b>STATS</b>\n\n👥 Users: " + totalUsers + "\n📨 Tickets: " + totalTickets + "\n🆕 New: " + newTickets;
                editMessageText(chatId, messageId, body);
            }
            else if (data.equals("admin_close")) {
                answerCallback(callbackId);
                try {
                    org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage del = 
                        new org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage();
                    del.setChatId(chatId);
                    del.setMessageId(messageId);
                    execute(del);
                } catch (Exception ignored) {}
            }
            else if (data.startsWith("ticket_status_")) {
                answerCallback(callbackId);
                String[] parts = data.split("_");
                if (parts.length >= 4) {
                    Long ticketId = Long.parseLong(parts[2]);
                    SupportMessage.TicketStatus status = SupportMessage.TicketStatus.valueOf(parts[3]);
                    supportService.updateStatus(ticketId, status);
                    editMessageText(chatId, messageId, "✅ Status updated to " + parts[3]);
                }
            }
            else if (data.startsWith("ticket_delete_")) {
                answerCallback(callbackId);
                Long ticketId = Long.parseLong(data.replace("ticket_delete_", ""));
                supportService.deleteById(ticketId);
                editMessageText(chatId, messageId, "✅ Deleted");
            }
        } catch (Exception e) {
            answerCallback(callbackId);
        }
    }

}
