package com.worldatlas.bot.controller;

import com.worldatlas.bot.entity.City;
import com.worldatlas.bot.entity.CustomCity;
import com.worldatlas.bot.entity.Reminder;
import com.worldatlas.bot.entity.User;
import com.worldatlas.bot.service.*;
import com.worldatlas.bot.entity.SupportMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class WebAppController {

    private final UserService userService;
    private final CityService cityService;
    private final CustomCityService customCityService;
    private final ReminderService reminderService;
    private final SupportService supportService;

    @Value("${telegram.bot.token}")
    private String botToken;

    public WebAppController(UserService userService, CityService cityService,
                            CustomCityService customCityService, ReminderService reminderService,
                            SupportService supportService) {
        this.userService = userService;
        this.cityService = cityService;
        this.customCityService = customCityService;
        this.reminderService = reminderService;
        this.supportService = supportService;
    }

    // ========== ПРОВЕРКА ПОДПИСИ TELEGRAM ==========
    private Long resolveChatId(String initData, String devChatId) {
        if (initData != null && !initData.isEmpty()) {
            try {
                Map<String, String> params = new HashMap<>();
                for (String pair : initData.split("&")) {
                    int idx = pair.indexOf('=');
                    if (idx > 0) params.put(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8),
                                            URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8));
                }
                String hash = params.remove("hash");
                if (hash == null) return null;
                List<String> keys = new ArrayList<>(params.keySet());
                Collections.sort(keys);
                StringBuilder sb = new StringBuilder();
                for (String k : keys) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append(k).append("=").append(params.get(k));
                }
                byte[] secret = hmac("WebAppData".getBytes(StandardCharsets.UTF_8), botToken.getBytes(StandardCharsets.UTF_8));
                String calc = toHex(hmac(secret, sb.toString().getBytes(StandardCharsets.UTF_8)));
                if (!calc.equals(hash)) return null;
                String userJson = params.get("user");
                if (userJson == null) return null;
                int idIdx = userJson.indexOf("\"id\":");
                if (idIdx < 0) return null;
                int start = idIdx + 5;
                int end = start;
                while (end < userJson.length() && Character.isDigit(userJson.charAt(end))) end++;
                return Long.parseLong(userJson.substring(start, end));
            } catch (Exception e) {
                return null;
            }
        }
        if (devChatId != null && !devChatId.isEmpty()) {
            try { return Long.parseLong(devChatId); } catch (Exception e) { return null; }
        }
        return null;
    }

    private byte[] hmac(byte[] key, byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data);
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private Long chatIdFromBody(Map<String, Object> body) {
        return resolveChatId((String) body.get("initData"), (String) body.get("devChatId"));
    }

    private String langOf(Long chatId) {
        User u = userService.getUser(chatId);
        return u != null ? u.getLanguage() : "ru";
    }

    private Map<String, Object> cityJson(City c, String lang) {
        if (c == null) return null;
        Map<String, Object> m = new HashMap<>();
        m.put("key", c.getName());
        m.put("name", cityService.getCityNameLocalized(c, lang));
        m.put("timezone", c.getTimezone());
        m.put("country", cityService.getCountryLocalized(c, lang));
        return m;
    }

    private Map<String, Object> reminderJson(Reminder r, String lang) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getId());
        City c = cityService.findCity(r.getCityName());
        m.put("city", c != null ? cityService.getCityNameLocalized(c, lang) : r.getCityName());
        m.put("time", r.getTime());
        m.put("text", r.getText());
        m.put("days", r.getDays());
        return m;
    }

    // ========== СОСТОЯНИЕ ПОЛЬЗОВАТЕЛЯ ==========
    @GetMapping("/state")
    public Map<String, Object> state(@RequestParam(required = false) String initData,
                                     @RequestParam(required = false) String devChatId) {
        Map<String, Object> out = new HashMap<>();
        Long chatId = resolveChatId(initData, devChatId);
        if (chatId == null) { out.put("error", "unauthorized"); return out; }
        User user = userService.getUser(chatId);
        if (user == null) user = userService.findOrCreate(chatId, "webapp", "", "");
        String lang = user.getLanguage();
        out.put("lang", lang);
        out.put("timeFormat", user.getTimeFormat());
        out.put("homeCity", cityJson(cityService.findCity(user.getHomeCity()), lang));
        List<Map<String, Object>> favs = new ArrayList<>();
        for (String f : user.getFavorites()) {
            Map<String, Object> c = cityJson(cityService.findCity(f), lang);
            if (c != null) favs.add(c);
        }
        out.put("favorites", favs);
        List<Map<String, Object>> customs = new ArrayList<>();
        for (CustomCity cc : customCityService.getUserCustomCities(chatId)) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", cc.getId());
            m.put("name", cc.getName());
            m.put("timezone", cc.getTimezone());
            customs.add(m);
        }
        out.put("customCities", customs);
        Optional<Reminder> rem = reminderService.getReminder(chatId);
        out.put("reminder", rem.map(r -> reminderJson(r, lang)).orElse(null));
        return out;
    }

    // ========== ПОИСК ГОРОДОВ ==========
    @GetMapping("/cities/search")
    public List<Map<String, Object>> search(@RequestParam String q,
                                            @RequestParam(required = false) String initData,
                                            @RequestParam(required = false) String devChatId) {
        Long chatId = resolveChatId(initData, devChatId);
        String lang = chatId != null ? langOf(chatId) : "ru";
        List<Map<String, Object>> out = new ArrayList<>();
        List<City> found = cityService.searchCities(q);
        for (int i = 0; i < found.size() && i < 20; i++) out.add(cityJson(found.get(i), lang));
        return out;
    }

    // ========== ИЗБРАННОЕ ==========
    @PostMapping("/favorites/toggle")
    public Map<String, Object> toggleFav(@RequestBody Map<String, Object> body) {
        Map<String, Object> out = new HashMap<>();
        Long chatId = chatIdFromBody(body);
        if (chatId == null) { out.put("ok", false); return out; }
        String key = (String) body.get("key");
        Boolean add = (Boolean) body.get("add");
        if (add != null && add) userService.addFavorite(chatId, key);
        else userService.removeFavorite(chatId, key);
        out.put("ok", true);
        return out;
    }

    // ========== ДОМАШНИЙ ГОРОД ==========
    @PostMapping("/home")
    public Map<String, Object> home(@RequestBody Map<String, Object> body) {
        Map<String, Object> out = new HashMap<>();
        Long chatId = chatIdFromBody(body);
        if (chatId == null) { out.put("ok", false); return out; }
        userService.setHomeCity(chatId, (String) body.get("key"));
        out.put("ok", true);
        return out;
    }

    // ========== НАПОМИНАЛКИ ==========
    @PostMapping("/reminders")
    public Map<String, Object> createReminder(@RequestBody Map<String, Object> body) {
        Map<String, Object> out = new HashMap<>();
        Long chatId = chatIdFromBody(body);
        if (chatId == null) { out.put("ok", false); return out; }
        String key = (String) body.get("key");
        String time = (String) body.get("time");
        String text = (String) body.get("text");
        String days = (String) body.get("days");
        if ("all".equals(days)) days = "mon,tue,wed,thu,fri,sat,sun";
        if (!ReminderScheduler.validateDays(days)) { out.put("ok", false); out.put("error", "bad_days"); return out; }
        reminderService.createReminder(chatId, key, time, text, days);
        String lang = langOf(chatId);
        Optional<Reminder> r = reminderService.getReminder(chatId);
        out.put("ok", true);
        out.put("reminder", r.map(rem -> reminderJson(rem, lang)).orElse(null));
        return out;
    }

    @PostMapping("/reminders/delete")
    public Map<String, Object> deleteReminder(@RequestBody Map<String, Object> body) {
        Map<String, Object> out = new HashMap<>();
        Long chatId = chatIdFromBody(body);
        if (chatId == null) { out.put("ok", false); return out; }
        reminderService.deleteReminder(chatId);
        out.put("ok", true);
        return out;
    }

    // ========== ПОЛЬЗОВАТЕЛЬСКИЕ ГОРОДА ==========
    @PostMapping("/custom")
    public Map<String, Object> createCustom(@RequestBody Map<String, Object> body) {
        Map<String, Object> out = new HashMap<>();
        Long chatId = chatIdFromBody(body);
        if (chatId == null) { out.put("ok", false); return out; }
        String name = (String) body.get("name");
        String timezone = (String) body.get("timezone");
        if (name == null || timezone == null || customCityService.isNameTakenByUser(chatId, name)) {
            out.put("ok", false); return out;
        }
        CustomCity cc = customCityService.createCustomCity(chatId, name, timezone);
        Map<String, Object> m = new HashMap<>();
        m.put("id", cc.getId());
        m.put("name", cc.getName());
        m.put("timezone", cc.getTimezone());
        out.put("ok", true);
        out.put("city", m);
        return out;
    }

    @PostMapping("/custom/delete")
    public Map<String, Object> deleteCustom(@RequestBody Map<String, Object> body) {
        Map<String, Object> out = new HashMap<>();
        Long chatId = chatIdFromBody(body);
        if (chatId == null) { out.put("ok", false); return out; }
        Object idObj = body.get("id");
        Long id = idObj instanceof Number ? ((Number) idObj).longValue() : null;
        if (id == null) { out.put("ok", false); return out; }
        for (CustomCity cc : customCityService.getUserCustomCities(chatId)) {
            if (cc.getId().equals(id)) {
                customCityService.deleteCustomCity(cc);
                out.put("ok", true);
                return out;
            }
        }
        out.put("ok", false);
        return out;
    }

    // ========== НАСТРОЙКИ ==========
    @PostMapping("/settings")
    public Map<String, Object> settings(@RequestBody Map<String, Object> body) {
        Map<String, Object> out = new HashMap<>();
        Long chatId = chatIdFromBody(body);
        if (chatId == null) { out.put("ok", false); return out; }
        String lang = (String) body.get("lang");
        String format = (String) body.get("format");
        if ("ru".equals(lang) || "en".equals(lang)) userService.setLanguage(chatId, lang);
        if ("12".equals(format) || "24".equals(format)) userService.setTimeFormat(chatId, format);
        out.put("ok", true);
        return out;
    }

    // ========== ПОДДЕРЖКА ==========
    @GetMapping("/support")
    public List<Map<String, Object>> getSupport(@RequestParam(required = false) String initData,
                                                 @RequestParam(required = false) String devChatId) {
        List<Map<String, Object>> out = new java.util.ArrayList<>();
        Long chatId = resolveChatId(initData, devChatId);
        if (chatId == null) return out;
        String lang = langOf(chatId);
        for (SupportMessage m : supportService.getUserMessages(chatId)) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", m.getId());
            map.put("text", m.getText());
            map.put("reply", m.getAdminReply());
            map.put("answered", m.isAnswered());
            map.put("date", m.getCreatedAt().toString());
            out.add(map);
        }
        return out;
    }

    @PostMapping("/support")
    public Map<String, Object> sendSupport(@RequestBody Map<String, Object> body) {
        Map<String, Object> out = new java.util.HashMap<>();
        Long chatId = chatIdFromBody(body);
        if (chatId == null) { out.put("ok", false); return out; }
        
        if (!supportService.canSendMessage(chatId)) {
            out.put("ok", false);
            out.put("error", "rate_limit");
            return out;
        }
        
        String text = (String) body.get("text");
        if (text == null || text.trim().isEmpty() || text.length() > 1000) {
            out.put("ok", false);
            return out;
        }
        
        User user = userService.getUser(chatId);
        String username = user != null ? user.getUsername() : "unknown";
        String firstName = user != null && user.getFirstName() != null ? user.getFirstName() : "";
        String displayName = !firstName.isEmpty() ? firstName : (username != null ? "@" + username : String.valueOf(chatId));
        
        SupportMessage msg = supportService.createMessage(chatId, username, text.trim());
        
        // Отправляем админу в Telegram
        try {
            com.worldatlas.bot.bot.WorldAtlasBot bot = com.worldatlas.bot.config.BotConfig.getBot();
            if (bot != null) {
                org.telegram.telegrambots.meta.api.methods.send.SendMessage sendMsg = 
                    new org.telegram.telegrambots.meta.api.methods.send.SendMessage();
                sendMsg.setChatId(UserService.MAIN_ADMIN_ID);
                sendMsg.setText("🆘 *Новое сообщение в поддержку*\n\n" +
                    "👤 " + displayName + " (`" + chatId + "`)\n" +
                    "💬 " + text.trim() + "\n\n" +
                    "Чтобы ответить, используй команду:\n" +
                    "`/reply " + msg.getId() + " твой ответ`");
                sendMsg.setParseMode("Markdown");
                bot.execute(sendMsg);
            }
        } catch (Exception e) {
            System.out.println("⚠️ Не удалось отправить сообщение админу: " + e.getMessage());
        }
        
        out.put("ok", true);
        out.put("messageId", msg.getId());
        return out;
    }

    // ========== РАЗНИЦА ВО ВРЕМЕНИ ==========
    @GetMapping("/diff")
    public Map<String, Object> timeDiff(@RequestParam String city1,
                                        @RequestParam String city2,
                                        @RequestParam(required = false) String initData,
                                        @RequestParam(required = false) String devChatId) {
        Map<String, Object> out = new HashMap<>();
        City c1 = cityService.findCity(city1);
        City c2 = cityService.findCity(city2);
        
        if (c1 == null || c2 == null) {
            out.put("ok", false);
            out.put("error", "city_not_found");
            return out;
        }
        
        try {
            java.time.ZoneId z1 = java.time.ZoneId.of(c1.getTimezone());
            java.time.ZoneId z2 = java.time.ZoneId.of(c2.getTimezone());
            java.time.ZonedDateTime now1 = java.time.ZonedDateTime.now(z1);
            java.time.ZonedDateTime now2 = now1.withZoneSameInstant(z2);
            
            long diffMinutes = java.time.Duration.between(now1, now2).toMinutes();
            long hours = Math.abs(diffMinutes) / 60;
            long minutes = Math.abs(diffMinutes) % 60;
            String sign = diffMinutes >= 0 ? "+" : "-";
            
            out.put("ok", true);
            out.put("city1", cityJson(c1, "en"));
            out.put("city2", cityJson(c2, "en"));
            out.put("diff", String.format("%s%dh %dm", sign, hours, minutes));
            out.put("time1", now1.toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
            out.put("time2", now2.toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
            out.put("date1", now1.toLocalDate().toString());
            out.put("date2", now2.toLocalDate().toString());
        } catch (Exception e) {
            out.put("ok", false);
            out.put("error", e.getMessage());
        }
        return out;
    }

    // ========== СПРАВКА ==========
    @GetMapping("/help")
    public Map<String, Object> help(@RequestParam(required = false) String lang) {
        Map<String, Object> out = new HashMap<>();
        String l = "en".equals(lang) ? "en" : "ru";
        
        if ("en".equals(l)) {
            out.put("title", "🌍 World Time Map Bot");
            out.put("features", java.util.Arrays.asList(
                "🕐 **World Clock** - Track time in multiple cities worldwide",
                "🔁 **Time Converter** - Convert time between any two cities",
                "🔢 **Time Difference** - See hours difference between cities",
                "⏰ **Reminders** - Set reminders for specific times in any city",
                "⭐ **Favorites** - Save cities for quick access",
                "🏙️ **Custom Cities** - Create your own cities with any timezone",
                "🌐 **Multi-language** - Switch between Russian and English",
                "💬 **Support** - Contact developer directly"
            ));
            out.put("commands", java.util.Arrays.asList(
                "/start - Start the bot",
                "/worldclock - Show world clock",
                "/convert - Convert time from home city",
                "/diff - Time difference between cities",
                "/remind - Create a reminder",
                "/remind_list - View your reminder",
                "/delete_remind - Delete reminder",
                "/home - Set home city",
                "/search_city - Search for a city",
                "/view_favorites - Your favorite cities",
                "/create_custom - Create custom city",
                "/list_custom - List custom cities",
                "/delete_custom - Delete custom city",
                "/lang - Change language",
                "/help - Bot help"
            ));
        } else {
            out.put("title", "🌍 World Time Map Бот");
            out.put("features", java.util.Arrays.asList(
                "🕐 **Мировые часы** - Отслеживайте время в городах по всему миру",
                "🔁 **Конвертер времени** - Переводите время между любыми городами",
                "🔢 **Разница во времени** - Разница в часах между городами",
                "⏰ **Напоминалки** - Установите напоминания на определённое время",
                "⭐ **Избранное** - Сохраняйте города для быстрого доступа",
                "🏙️ **Свои города** - Создавайте города с любым часовым поясом",
                "🌐 **Мультиязычность** - Переключение между русским и английским",
                "💬 **Поддержка** - Связь с разработчиком"
            ));
            out.put("commands", java.util.Arrays.asList(
                "/start - Запустить бота",
                "/worldclock - Показать мировые часы",
                "/convert - Конвертировать время из домашнего города",
                "/diff - Разница во времени между городами",
                "/remind - Создать напоминалку",
                "/remind_list - Посмотреть напоминалку",
                "/delete_remind - Удалить напоминалку",
                "/home - Установить домашний город",
                "/search_city - Поиск города",
                "/view_favorites - Ваши избранные города",
                "/create_custom - Создать свой город",
                "/list_custom - Список своих городов",
                "/delete_custom - Удалить свой город",
                "/lang - Сменить язык",
                "/help - Помощь"
            ));
        }
        return out;
    }

    // ========== ИНФОРМАЦИЯ О ГОРОДЕ ==========
    @GetMapping("/city/info")
    public Map<String, Object> cityInfo(@RequestParam String key,
                                       @RequestParam(required = false) String initData,
                                       @RequestParam(required = false) String devChatId) {
        Map<String, Object> out = new HashMap<>();
        Long chatId = resolveChatId(initData, devChatId);
        String lang = chatId != null ? langOf(chatId) : "ru";
        
        City c = cityService.findCity(key);
        if (c == null) {
            out.put("ok", false);
            return out;
        }
        
        out.put("ok", true);
        out.put("city", cityJson(c, lang));
        out.put("info", cityService.getCityInfo(c, lang));
        out.put("country", cityService.getCountryLocalized(c, lang));
        out.put("continent", cityService.getContinentLocalized(c, lang));
        
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.of(c.getTimezone()));
        out.put("currentTime", now.toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
        out.put("currentDate", now.toLocalDate().toString());
        out.put("dayOfWeek", now.getDayOfWeek().toString());
        
        return out;
    }
}
