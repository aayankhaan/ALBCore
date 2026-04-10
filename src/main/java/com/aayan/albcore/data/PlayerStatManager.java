package com.aayan.albcore.data;

import com.aayan.albcore.ALBCore;

import java.util.UUID;

public final class PlayerStatManager {

    private final DatabaseManager db;

    public PlayerStatManager(DatabaseManager db) {
        this.db = db;
    }

    // ── Sync ──────────────────────────────────────────────

    public void set(UUID uuid, String key, double value) {
        db.setData(uuid, "stat:" + key, String.valueOf(value));
    }

    public double get(UUID uuid, String key) {
        return get(uuid, key, 0.0);
    }

    public double get(UUID uuid, String key, double defaultValue) {
        String raw = db.getData(uuid, "stat:" + key);
        if (raw == null) return defaultValue;
        try { return Double.parseDouble(raw); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    public void increment(UUID uuid, String key, double amount) {
        set(uuid, key, get(uuid, key) + amount);
    }

    public void decrement(UUID uuid, String key, double amount) {
        set(uuid, key, Math.max(0, get(uuid, key) - amount));
    }

    public boolean has(UUID uuid, String key) {
        return db.getData(uuid, "stat:" + key) != null;
    }

    public void reset(UUID uuid, String key) {
        db.removeData(uuid, "stat:" + key);
    }

    // ── Async ─────────────────────────────────────────────

    public void setAsync(UUID uuid, String key, double value) {
        db.setDataAsync(uuid, "stat:" + key, String.valueOf(value));
    }

    public void setAsync(UUID uuid, String key, double value, Runnable callback) {
        db.setDataAsync(uuid, "stat:" + key, String.valueOf(value), callback);
    }

    public void incrementAsync(UUID uuid, String key, double amount) {
        db.executeAsync(() -> increment(uuid, key, amount));
    }

    public void incrementAsync(UUID uuid, String key, double amount, Runnable callback) {
        db.executeAsync(() -> increment(uuid, key, amount), callback);
    }

    public void decrementAsync(UUID uuid, String key, double amount) {
        db.executeAsync(() -> decrement(uuid, key, amount));
    }
}