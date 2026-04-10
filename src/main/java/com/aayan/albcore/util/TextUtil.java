package com.aayan.albcore.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class TextUtil {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_AMP =
            LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer LEGACY_SECT =
            LegacyComponentSerializer.legacySection();

    private TextUtil() {}

    // Parse

    public static Component parse(String input) {
        if (input == null || input.isBlank()) return Component.empty();
        return MM.deserialize(convertLegacy(input));
    }

    public static Component parse(String input, Player player) {
        if (input == null || input.isBlank()) return Component.empty();
        String withPapi = PlaceholderUtil.parse(input, player);
        return MM.deserialize(convertLegacy(withPapi));
    }

    public static Component parse(String input, Map<String, String> placeholders) {
        if (input == null || input.isBlank()) return Component.empty();
        TagResolver.Builder resolver = TagResolver.builder();
        placeholders.forEach((k, v) -> resolver.resolver(Placeholder.parsed(k, v)));
        return MM.deserialize(convertLegacy(input), resolver.build());
    }

    public static Component parse(String input, Player player,
                                  Map<String, String> placeholders) {
        if (input == null || input.isBlank()) return Component.empty();
        String withPapi = PlaceholderUtil.parse(input, player);
        TagResolver.Builder resolver = TagResolver.builder();
        placeholders.forEach((k, v) -> resolver.resolver(Placeholder.parsed(k, v)));
        return MM.deserialize(convertLegacy(withPapi), resolver.build());
    }

    public static Component parse(ALBColor color, String text) {
        return parse(color.apply(text));
    }

    public static List<Component> parseList(List<String> lines) {
        return lines.stream().map(TextUtil::parse).collect(Collectors.toList());
    }

    public static List<Component> parseList(List<String> lines, Player player) {
        return lines.stream()
                .map(line -> parse(line, player))
                .collect(Collectors.toList());
    }

    // Send — chat

    public static void send(CommandSender sender, String message) {
        if (sender instanceof Player player) {
            sender.sendMessage(parse(message, player));
        } else {
            sender.sendMessage(parse(message));
        }
    }

    public static void send(CommandSender sender, String message,
                            Map<String, String> placeholders) {
        if (sender instanceof Player player) {
            sender.sendMessage(parse(message, player, placeholders));
        } else {
            sender.sendMessage(parse(message, placeholders));
        }
    }

    public static void send(CommandSender sender, ALBColor color, String text) {
        send(sender, color.apply(text));
    }

    // Send — actionbar

    public static void sendActionbar(Player player, String message) {
        player.sendActionBar(parse(message, player));
    }

    public static void sendActionbar(Player player, String message,
                                     Map<String, String> placeholders) {
        player.sendActionBar(parse(message, player, placeholders));
    }

    public static void sendActionbar(Player player, ALBColor color, String text) {
        sendActionbar(player, color.apply(text));
    }

    // Send — title

    public static void sendTitle(Player player,
                                 String title, String subtitle,
                                 int fadeIn, int stay, int fadeOut) {
        player.showTitle(net.kyori.adventure.title.Title.title(
                parse(title, player),
                parse(subtitle, player),
                net.kyori.adventure.title.Title.Times.times(
                        Duration.ofMillis(fadeIn  * 50L),
                        Duration.ofMillis(stay    * 50L),
                        Duration.ofMillis(fadeOut * 50L)
                )
        ));
    }

    public static void sendTitle(Player player,
                                 String title, String subtitle,
                                 int fadeIn, int stay, int fadeOut,
                                 Map<String, String> placeholders) {
        player.showTitle(net.kyori.adventure.title.Title.title(
                parse(title, player, placeholders),
                parse(subtitle, player, placeholders),
                net.kyori.adventure.title.Title.Times.times(
                        Duration.ofMillis(fadeIn  * 50L),
                        Duration.ofMillis(stay    * 50L),
                        Duration.ofMillis(fadeOut * 50L)
                )
        ));
    }

    // Serialize

    public static String toPlain(String input) {
        return PlainTextComponentSerializer.plainText().serialize(parse(input));
    }

    public static String toPlain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public static String toLegacy(String input) {
        return LEGACY_AMP.serialize(parse(input));
    }

    public static String toLegacy(Component component) {
        return LEGACY_AMP.serialize(component);
    }

    public static String strip(String input) {
        if (input == null) return "";
        return MM.stripTags(convertLegacy(input));
    }

    // Legacy converter

    public static String convertLegacy(String input) {
        if (input == null || input.isEmpty()) return "";
        if (!input.contains("&") && !input.contains("§")) return input;

        String result = input.replace("§", "&");

        return result.replaceAll("(?i)&0", "<black>")
                .replaceAll("(?i)&1", "<dark_blue>")
                .replaceAll("(?i)&2", "<dark_green>")
                .replaceAll("(?i)&3", "<dark_aqua>")
                .replaceAll("(?i)&4", "<dark_red>")
                .replaceAll("(?i)&5", "<dark_purple>")
                .replaceAll("(?i)&6", "<gold>")
                .replaceAll("(?i)&7", "<gray>")
                .replaceAll("(?i)&8", "<dark_gray>")
                .replaceAll("(?i)&9", "<blue>")
                .replaceAll("(?i)&a", "<green>")
                .replaceAll("(?i)&b", "<aqua>")
                .replaceAll("(?i)&c", "<red>")
                .replaceAll("(?i)&d", "<light_purple>")
                .replaceAll("(?i)&e", "<yellow>")
                .replaceAll("(?i)&f", "<white>")
                .replaceAll("(?i)&l", "<bold>")
                .replaceAll("(?i)&m", "<strikethrough>")
                .replaceAll("(?i)&n", "<underlined>")
                .replaceAll("(?i)&o", "<italic>")
                .replaceAll("(?i)&k", "<obfuscated>")
                .replaceAll("(?i)&r", "<reset>");
    }
}