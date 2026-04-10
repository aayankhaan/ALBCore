package com.aayan.albcore.hook;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class PlaceholderRegistry {

    // simple placeholders
    private final Map<String, Function<OfflinePlayer, String>> handlers =
            new ConcurrentHashMap<>();

    // dynamic placeholders (stat_x, skill_x, etc)
    private final Map<String, BiFunction<OfflinePlayer, String, String>> dynamicHandlers =
            new ConcurrentHashMap<>();

    // ownership for automatic cleanup
    private final Map<String, Plugin> owners =
            new ConcurrentHashMap<>();

    // register simple placeholder
    public void register(Plugin plugin, String key, Function<OfflinePlayer, String> handler) {
        key = key.toLowerCase();
        handlers.put(key, handler);
        owners.put(key, plugin);
    }

    // dynamic placeholder (stat_kills)
    public void registerDynamic(Plugin plugin, String prefix,
                                BiFunction<OfflinePlayer, String, String> handler) {

        prefix = prefix.toLowerCase();
        dynamicHandlers.put(prefix, handler);
        owners.put(prefix, plugin);
    }

    public void unregister(String key) {
        key = key.toLowerCase();
        handlers.remove(key);
        dynamicHandlers.remove(key);
        owners.remove(key);
    }

    // automatically remove all placeholders for a plugin
    public void unregisterPlugin(Plugin plugin) {
        owners.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(plugin)) {
                handlers.remove(entry.getKey());
                dynamicHandlers.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    public String resolve(String key, OfflinePlayer player) {
        key = key.toLowerCase();

        // direct placeholder
        Function<OfflinePlayer, String> handler = handlers.get(key);
        if (handler != null) {
            try {
                return handler.apply(player);
            } catch (Exception ignored) {}
        }

        // dynamic placeholders
        int split = key.indexOf('_');
        if (split != -1) {

            String prefix = key.substring(0, split);
            String param = key.substring(split + 1);

            BiFunction<OfflinePlayer, String, String> dynamic =
                    dynamicHandlers.get(prefix);

            if (dynamic != null) {
                try {
                    return dynamic.apply(player, param);
                } catch (Exception ignored) {}
            }
        }

        return "";
    }

    public boolean has(String key) {
        return handlers.containsKey(key.toLowerCase())
                || dynamicHandlers.containsKey(key.toLowerCase());
    }
}