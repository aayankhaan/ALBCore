package com.aayan.albcore.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    // Set

    public void set(UUID player, String key, long durationMs) {
        cooldowns
                .computeIfAbsent(player, k -> new ConcurrentHashMap<>())
                .put(key, System.currentTimeMillis() + durationMs);
    }

    /**
     * Set a persistent cooldown that survives server restarts.
     */
    public void setPersistent(UUID player, String key, long durationMs) {
        long expiry = System.currentTimeMillis() + durationMs;
        cooldowns
                .computeIfAbsent(player, k -> new ConcurrentHashMap<>())
                .put(key, expiry);

        com.aayan.albcore.ALBCore.api().db().saveCooldown(player, key, expiry);
    }

    /**
     * Loads all persistent cooldowns from the database.
     * Should be called on plugin enable.
     */
    public void loadFromDatabase() {
        com.aayan.albcore.ALBCore.api().db().loadCooldowns(cooldowns);
    }

    //  Check 

    public boolean isOnCooldown(UUID player, String key) {
        Map<String, Long> map = cooldowns.get(player);
        if (map == null) return false;
        Long expiry = map.get(key);
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            map.remove(key);
            com.aayan.albcore.ALBCore.api().db().removeCooldown(player, key);
            return false;
        }
        return true;
    }

    //  Remaining 

    public long getRemaining(UUID player, String key) {
        Map<String, Long> map = cooldowns.get(player);
        if (map == null) return 0L;
        Long expiry = map.get(key);
        if (expiry == null) return 0L;
        long remaining = expiry -  System.currentTimeMillis();
        if (remaining <= 0) {
            map.remove(key);
            com.aayan.albcore.ALBCore.api().db().removeCooldown(player, key);
            return 0L;
        }
        return remaining;
    }

    public String getFormatted(UUID player, String key) {
        long remaining = getRemaining(player, key);
        if (remaining <= 0) return "Ready";
        double seconds = remaining / 1000.0;
        if (seconds >= 60) {
            int mins = (int)(seconds / 60);
            int secs = (int)(seconds % 60);
            return mins + "m " + secs + "s";
        }
        return String.format("%.1fs", seconds);
    }

    //  Clear 

    public void clear(UUID player, String key) {
        Map<String, Long> map = cooldowns.get(player);
        if (map != null) {
            map.remove(key);
            com.aayan.albcore.ALBCore.api().db().removeCooldown(player, key);
        }
    }

    public void clearPlayer(UUID player) {
        cooldowns.remove(player);
        // We don't remove from DB here because it would require a scan or specific API
        // Better to let them expire or be cleared individually if needed.
        // Or we can add a removeAllForPlayer to DB.
        com.aayan.albcore.ALBCore.api().db().execute("DELETE FROM cooldowns WHERE uuid = '" + player.toString() + "'");
    }

    public void clearAll() {
        cooldowns.clear();
    }

    //  Info 

    public Map<String, Long> getAll(UUID player) {
        return Collections.unmodifiableMap(
                cooldowns.getOrDefault(player, new HashMap<>()));
    }
}