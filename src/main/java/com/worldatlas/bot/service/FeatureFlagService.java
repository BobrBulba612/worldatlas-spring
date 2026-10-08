package com.worldatlas.bot.service;

import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FeatureFlagService {
    private final Set<Long> betaUsers = ConcurrentHashMap.newKeySet();
    private volatile boolean newVersionGlobal = false;
    
    public FeatureFlagService() {
        betaUsers.add(1319065617L);
    }
    
    public boolean isNewVersion(Long chatId, String username) {
        if (newVersionGlobal) return true;
        if (chatId != null && chatId == 1319065617L) return true;
        if ("BobrArbuz".equalsIgnoreCase(username)) return true;
        return chatId != null && betaUsers.contains(chatId);
    }
    
    public void enableForAll() { newVersionGlobal = true; }
    public void disableForAll() { newVersionGlobal = false; }
    public void addBetaUser(Long chatId) { betaUsers.add(chatId); }
    public void removeBetaUser(Long chatId) { betaUsers.remove(chatId); }
    public boolean isGlobalEnabled() { return newVersionGlobal; }
    public int getBetaCount() { return betaUsers.size(); }
}
