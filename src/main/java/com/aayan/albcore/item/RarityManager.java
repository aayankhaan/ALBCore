package com.aayan.albcore.item;

import com.aayan.albcore.ALBCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class RarityManager {

    private final ALBCore plugin;
    private final Map<String, String> rarities = new HashMap<>();
    private static final String NONE_DISPLAY = "<gray>None";

    public RarityManager(ALBCore plugin) {
        this.plugin = plugin;
        load();
    }

    // Load

    public void load() {
        rarities.clear();

        File file = new File(plugin.getDataFolder(), "rarities.yml");
        if (!file.exists()) {
            plugin.saveResource("rarities.yml", false);
        }

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        for (String key : cfg.getKeys(false)) {
            String display = cfg.getString(key + ".display");
            if (display == null || display.isBlank()) {
                warn("Rarity \"" + key + "\" has no display value — skipping");
                continue;
            }
            rarities.put(key.toUpperCase(), display);
        }

        plugin.getLogger().info("Loaded " + rarities.size() + " rarities.");
    }

    // Get

    public Optional<String> get(String key) {
        if (key == null || key.isBlank()) return Optional.empty();
        return Optional.ofNullable(rarities.get(key.toUpperCase()));
    }

    public String getDisplay(String key) {
        if (key == null || key.isBlank()) return NONE_DISPLAY;
        String display = rarities.get(key.toUpperCase());
        if (display == null) {
            warn("Rarity \"" + key + "\" does not exist in rarities.yml");
            return NONE_DISPLAY;
        }
        return display;
    }

    public boolean exists(String key) {
        if (key == null) return false;
        return rarities.containsKey(key.toUpperCase());
    }

    public Map<String, String> getAll() {
        return java.util.Collections.unmodifiableMap(rarities);
    }

    // Internal

    private void warn(String msg) {
        plugin.getLogger().warning("[ALBCore | RarityManager] " + msg);
    }
}