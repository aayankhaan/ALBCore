package com.aayan.albcore.command;

import com.aayan.albcore.ALBCore;
import com.aayan.albcore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public final class ALBCoreCommand implements CommandExecutor, TabCompleter {

    private final ALBCore plugin;

    public ALBCoreCommand(ALBCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
                             String label, String[] args) {
        if (!sender.hasPermission("albcore.admin")) {
            sendError(sender, "No permission.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "help" -> sendHelp(sender);

            case "version" -> {
                TextUtil.send(sender, "<gray>--- <white>ALBCore Info <gray>---");
                TextUtil.send(sender, "  <gray>Version: <white>" + plugin.getDescription().getVersion());
                TextUtil.send(sender, "  <gray>Author: <white>" + String.join(", ", plugin.getDescription().getAuthors()));
                TextUtil.send(sender, "  <gray>Debug: <white>" + (ALBCore.api().config().isDebug() ? "<green>ON" : "<red>OFF"));
                TextUtil.send(sender, "  <gray>Registered items: <white>" + ALBCore.api().registry().size());
                TextUtil.send(sender, "  <gray>Loaded rarities: <white>" + ALBCore.api().rarities().getAll().size());
            }

            case "debug" -> {
                boolean current = ALBCore.api().config().isDebug();
                plugin.getConfig().set("debug", !current);
                plugin.saveConfig();
                ALBCore.api().config().load();
                sendSuccess(sender, "Debug mode: " + (!current ? "<green>ON" : "<red>OFF"));
            }

            case "reload" -> {
                ALBCore.api().config().load();
                ALBCore.api().rarities().load();
                int before = ALBCore.api().effects().getAllEffects().size();
                ALBCore.api().effects().reload();
                int after = ALBCore.api().effects().getAllEffects().size();
                sendSuccess(sender, "ALBCore reloaded. Effects: "
                        + before + " \u2192 " + after);
            }

            case "rarities" -> {
                TextUtil.send(sender, "<gray>--- <white>Loaded Rarities <gray>("
                        + ALBCore.api().rarities().getAll().size() + "<gray>) ---");
                ALBCore.api().rarities().getAll().forEach((key, display) ->
                        TextUtil.send(sender, "  <dark_gray>» " + display
                                + " <dark_gray>(" + key + ")"));
            }

            case "items" -> {
                TextUtil.send(sender, "<gray>--- <white>Registered Items <gray>("
                        + ALBCore.api().registry().size() + "<gray>) ---");
                if (ALBCore.api().registry().getIds().isEmpty()) {
                    TextUtil.send(sender, "  <dark_gray>No items registered.");
                } else {
                    ALBCore.api().registry().getIds().forEach(id ->
                            TextUtil.send(sender, "  <dark_gray>» <white>" + id));
                }
            }

            case "effects" -> {
                var all = ALBCore.api().effects().getAllEffects();
                TextUtil.send(sender, "<gray>--- <white>Registered Effects <gray>("
                        + all.size() + "<gray>) ---");
                if (all.isEmpty()) {
                    TextUtil.send(sender, "  <dark_gray>No effects registered.");
                } else {
                    for (var def : all) {
                        String src = def.isCodeDriven() ? "<aqua>code" : "<yellow>yml";
                        TextUtil.send(sender, "  <dark_gray>\u00bb <white>" + def.getId()
                                + " <dark_gray>(" + src + "<dark_gray>, trigger="
                                + def.getTrigger() + ", maxLevel=" + def.getMaxLevel() + ")");
                    }
                }
            }

            case "cooldowns" -> {
                if (args.length < 2) {
                    sendError(sender, "Usage: /albcore cooldowns <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sendError(sender, "Player not found: " + args[1]);
                    return true;
                }
                var all = ALBCore.api().cooldowns().getAll(target.getUniqueId());
                if (all.isEmpty()) {
                    TextUtil.send(sender, "  <gray>" + target.getName()
                            + " has no active cooldowns.");
                    return true;
                }
                TextUtil.send(sender, "<gray>--- <white>Cooldowns for "
                        + target.getName() + " <gray>---");
                all.forEach((key, expiry) -> {
                    String remaining = ALBCore.api().cooldowns()
                            .getFormatted(target.getUniqueId(), key);
                    TextUtil.send(sender, "  <dark_gray>» <white>" + key
                            + " <dark_gray>→ <yellow>" + remaining);
                });
            }

            default -> sendHelp(sender);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd,
                                      String label, String[] args) {
        if (args.length == 1) return List.of(
                "help", "version", "debug", "reload",
                "rarities", "items", "effects", "cooldowns");
        if (args.length == 2 && args[0].equalsIgnoreCase("cooldowns")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList();
        }
        return List.of();
    }

    // Helpers

    private void sendHelp(CommandSender sender) {
        TextUtil.send(sender, "<gray>--- <white>ALBCore Commands <gray>---");
        TextUtil.send(sender, "  <aqua>/albcore help <dark_gray>— show this menu");
        TextUtil.send(sender, "  <aqua>/albcore version <dark_gray>— plugin info");
        TextUtil.send(sender, "  <aqua>/albcore debug <dark_gray>— toggle debug mode");
        TextUtil.send(sender, "  <aqua>/albcore reload <dark_gray>— reload all configs");
        TextUtil.send(sender, "  <aqua>/albcore rarities <dark_gray>— list loaded rarities");
        TextUtil.send(sender, "  <aqua>/albcore items <dark_gray>\u2014 list registered items");
        TextUtil.send(sender, "  <aqua>/albcore effects <dark_gray>\u2014 list registered effects");
        TextUtil.send(sender, "  <aqua>/albcore cooldowns <player> <dark_gray>\u2014 view player cooldowns");
    }

    private void sendSuccess(CommandSender sender, String message) {
        TextUtil.send(sender, ALBCore.api().config().getPrefix() + "<green>" + message);
    }

    private void sendError(CommandSender sender, String message) {
        TextUtil.send(sender, ALBCore.api().config().getPrefix() + "<red>" + message);
    }
}