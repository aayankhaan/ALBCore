package com.aayan.albcore.util;

import org.bukkit.entity.Player;

public final class PlaceholderUtil {

    private static boolean papiEnabled = false;

    private PlaceholderUtil() {}

    public static void init() {
        papiEnabled = org.bukkit.Bukkit.getPluginManager()
                .getPlugin("PlaceholderAPI") != null;
        if (papiEnabled) {
            java.util.logging.Logger.getLogger("ALBCore")
                    .info("[ALBCore] PlaceholderAPI found — placeholder support enabled.");
        }
    }

    public static String parse(String text, Player player) {
        if (text == null) return "";
        if (!papiEnabled || player == null) return text;
        return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
    }

    public static boolean isEnabled() {
        return papiEnabled;
    }
    // PlaceholderUtil.java
    public static String setPlaceholders(Player player, String text) {
        if (!papiEnabled || player == null) return text;
        return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
    }
}