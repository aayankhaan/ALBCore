package com.aayan.albcore.manager;

import com.aayan.albcore.ALBCore;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MultiplierManager {

    private record Multiplier(double value, long expiresAt) {
        boolean isExpired() {
            return expiresAt > 0 && System.currentTimeMillis() > expiresAt;
        }
    }

    // uuid -> type -> multiplier
    private final Map<UUID, Map<String, Multiplier>> multipliers =
            new ConcurrentHashMap<>();

    // set timed multiplier
    public void set(UUID uuid, String type, double value, long durationMs) {
        multipliers
                .computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .put(type, new Multiplier(value,
                        System.currentTimeMillis() + durationMs));
    }

    // set permanent multiplier
    public void set(UUID uuid, String type, double value) {
        multipliers
                .computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .put(type, new Multiplier(value, -1));
    }

    // get current multiplier — returns 1.0 if none or expired
    public double get(UUID uuid, String type) {
        Map<String, Multiplier> playerMults = multipliers.get(uuid);
        if (playerMults == null) return 1.0;
        Multiplier m = playerMults.get(type);
        if (m == null || m.isExpired()) {
            if (m != null) playerMults.remove(type);
            return 1.0;
        }
        return m.value();
    }

    // apply multiplier to a value
    public double apply(UUID uuid, String type, double value) {
        return value * get(uuid, type);
    }

    // check if active
    public boolean hasActive(UUID uuid, String type) {
        return get(uuid, type) > 1.0;
    }

    // get remaining ms
    public long getRemaining(UUID uuid, String type) {
        Map<String, Multiplier> playerMults = multipliers.get(uuid);
        if (playerMults == null) return 0;
        Multiplier m = playerMults.get(type);
        if (m == null || m.isExpired()) return 0;
        if (m.expiresAt() < 0) return -1; // permanent
        return m.expiresAt() - System.currentTimeMillis();
    }

    public void remove(UUID uuid, String type) {
        Map<String, Multiplier> playerMults = multipliers.get(uuid);
        if (playerMults != null) playerMults.remove(type);
    }

    public void clearPlayer(UUID uuid) {
        multipliers.remove(uuid);
    }

    public void clearAll() {
        multipliers.clear();
    }
}