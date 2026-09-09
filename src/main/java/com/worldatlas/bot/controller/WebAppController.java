package com.worldatlas.bot.controller;

import com.worldatlas.bot.entity.City;
import com.worldatlas.bot.entity.CustomCity;
import com.worldatlas.bot.entity.Reminder;
import com.worldatlas.bot.entity.User;
import com.worldatlas.bot.service.*;
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

    @Value("${telegram.bot.token}")
    private String botToken;

    public WebAppController(UserService userService, CityService cityService,
                            CustomCityService customCityService, ReminderService reminderService) {
        this.userService = userService;
        this.cityService = cityService;
        this.customCityService = customCityService;
        this.reminderService = reminderService;
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
}
