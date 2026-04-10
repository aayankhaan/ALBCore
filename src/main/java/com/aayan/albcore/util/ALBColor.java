package com.aayan.albcore.util;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;

import java.util.HashMap;
import java.util.Map;

public final class ALBColor {

    private static final Map<Character, String> LEGACY_MAP = new HashMap<>();
    private static final Map<String, String> NAMED_MAP = new HashMap<>();
    private static final String FALLBACK_HEX = "#FFFFFF";

    static {
        LEGACY_MAP.put('0', "#000000");
        LEGACY_MAP.put('1', "#0000AA");
        LEGACY_MAP.put('2', "#00AA00");
        LEGACY_MAP.put('3', "#00AAAA");
        LEGACY_MAP.put('4', "#AA0000");
        LEGACY_MAP.put('5', "#AA00AA");
        LEGACY_MAP.put('6', "#FFAA00");
        LEGACY_MAP.put('7', "#AAAAAA");
        LEGACY_MAP.put('8', "#555555");
        LEGACY_MAP.put('9', "#5555FF");
        LEGACY_MAP.put('a', "#55FF55");
        LEGACY_MAP.put('b', "#55FFFF");
        LEGACY_MAP.put('c', "#FF5555");
        LEGACY_MAP.put('d', "#FF55FF");
        LEGACY_MAP.put('e', "#FFFF55");
        LEGACY_MAP.put('f', "#FFFFFF");

        NAMED_MAP.put("black", "#000000");
        NAMED_MAP.put("dark_blue", "#0000AA");
        NAMED_MAP.put("dark_green", "#00AA00");
        NAMED_MAP.put("dark_aqua", "#00AAAA");
        NAMED_MAP.put("dark_red", "#AA0000");
        NAMED_MAP.put("dark_purple", "#AA00AA");
        NAMED_MAP.put("gold", "#FFAA00");
        NAMED_MAP.put("gray", "#AAAAAA");
        NAMED_MAP.put("dark_gray", "#555555");
        NAMED_MAP.put("blue", "#5555FF");
        NAMED_MAP.put("green", "#55FF55");
        NAMED_MAP.put("aqua", "#55FFFF");
        NAMED_MAP.put("red", "#FF5555");
        NAMED_MAP.put("light_purple", "#FF55FF");
        NAMED_MAP.put("yellow", "#FFFF55");
        NAMED_MAP.put("white", "#FFFFFF");
    }

    private final String hex;
    private final boolean gradient;
    private final String gradientTag;

    public ALBColor(String input) {

        if (input == null || input.isBlank()) {
            warn("Empty color input");
            this.hex = FALLBACK_HEX;
            this.gradient = false;
            this.gradientTag = null;
            return;
        }

        input = input.trim();

        if (input.startsWith("<gradient:")) {
            this.hex = null;
            this.gradient = true;
            this.gradientTag = input;
            return;
        }

        String parsed = parseColor(input);

        this.hex = parsed;
        this.gradient = false;
        this.gradientTag = null;
    }

    // Apply color to text
    public String apply(String text) {
        if (text == null || text.isBlank()) return "";
        if (gradient) return gradientTag + text + "</gradient>";
        return "<color:" + hex + ">" + text + "</color>";
    }

    public String tag() {
        if (gradient) return gradientTag;
        return "<color:" + hex + ">";
    }

    public String hex() {
        return hex;
    }

    // Bukkit
    public Color toBukkit() {
        if (gradient) {
            warn("Cannot convert gradient to Bukkit Color");
            return Color.WHITE;
        }

        int[] rgb = hexToRgb(hex);
        return Color.fromRGB(rgb[0], rgb[1], rgb[2]);
    }

    public Particle.DustOptions toDust() {
        return new Particle.DustOptions(toBukkit(), 1.0f);
    }

    public Particle.DustOptions toDust(float size) {
        return new Particle.DustOptions(toBukkit(), size);
    }


    // Static factories
    public static ALBColor hex(String hex) {
        return new ALBColor(hex);
    }

    public static ALBColor rgb(int r, int g, int b) {
        return new ALBColor(r + ";" + g + ";" + b);
    }

    public static ALBColor gradient(String a, String b) {
        String colorA = new ALBColor(a).hex();
        String colorB = new ALBColor(b).hex();
        return new ALBColor("<gradient:" + colorA + ":" + colorB + ">");
    }

    // Presets
    public static final ALBColor WHITE = new ALBColor("#FFFFFF");
    public static final ALBColor RED = new ALBColor("#FF5555");
    public static final ALBColor GREEN = new ALBColor("#55FF55");
    public static final ALBColor AQUA = new ALBColor("#55FFFF");
    public static final ALBColor GOLD = new ALBColor("#FFAA00");
    public static final ALBColor GRAY = new ALBColor("#AAAAAA");
    public static final ALBColor DARK_GRAY = new ALBColor("#555555");
    public static final ALBColor DARK_RED = new ALBColor("#AA0000");
    public static final ALBColor YELLOW = new ALBColor("#FFFF55");
    public static final ALBColor LIGHT_PURPLE = new ALBColor("#FF55FF");


    // Parsing
    private String parseColor(String input) {


        if (input.startsWith("<#") && input.endsWith(">")) {
            String hex = input.substring(1, input.length() - 1);
            if (isValidHex(hex)) return hex.toUpperCase();
        }


        if (input.startsWith("<") && input.endsWith(">")) {
            String name = input.substring(1, input.length() - 1).toLowerCase();
            if (NAMED_MAP.containsKey(name)) return NAMED_MAP.get(name);
        }


        if (input.startsWith("&") && input.length() == 2) {
            char code = Character.toLowerCase(input.charAt(1));
            if (LEGACY_MAP.containsKey(code)) return LEGACY_MAP.get(code);
        }


        if (input.startsWith("#") && isValidHex(input)) {
            return input.toUpperCase();
        }


        if (input.contains(";")) {
            String[] rgb = input.split(";");
            if (rgb.length == 3) {
                try {
                    int r = Integer.parseInt(rgb[0].trim());
                    int g = Integer.parseInt(rgb[1].trim());
                    int b = Integer.parseInt(rgb[2].trim());
                    if (inRange(r) && inRange(g) && inRange(b))
                        return rgbToHex(r, g, b);
                } catch (NumberFormatException ignored) {}
            }
        }

        String lower = input.toLowerCase();
        if (NAMED_MAP.containsKey(lower)) return NAMED_MAP.get(lower);

        warn("Invalid color \"" + input + "\" — using white");
        return FALLBACK_HEX;
    }


    // Helpers
    private boolean isValidHex(String hex) {
        return hex.matches("#[0-9a-fA-F]{6}");
    }

    private boolean inRange(int v) {
        return v >= 0 && v <= 255;
    }

    private int[] hexToRgb(String hex) {

        String s = hex.substring(1);

        return new int[]{
                Integer.parseInt(s.substring(0, 2), 16),
                Integer.parseInt(s.substring(2, 4), 16),
                Integer.parseInt(s.substring(4, 6), 16)
        };
    }

    private String rgbToHex(int r, int g, int b) {
        return String.format("#%02X%02X%02X", r, g, b);
    }

    private void warn(String msg) {
        Bukkit.getLogger().warning("[ALBCore | ALBColor] " + msg);
    }

    @Override
    public String toString() {
        if (gradient) return gradientTag;
        return hex;
    }
}