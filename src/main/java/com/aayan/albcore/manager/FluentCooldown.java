package com.aayan.albcore.manager;

import com.aayan.albcore.ALBCore;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public final class FluentCooldown {

    private final String key;
    private long durationMs = 1000L;
    private Consumer<Player> action;

    private FluentCooldown(String key) {
        this.key = key;
    }

    public static FluentCooldown forKey(String key) {
        return new FluentCooldown(key);
    }

    public FluentCooldown duration(long ms) {
        this.durationMs = ms;
        return this;
    }

    public FluentCooldown run(Consumer<Player> action) {
        this.action = action;
        return this;
    }

    public boolean execute(Player player) {
        if (ALBCore.api().cooldowns().isOnCooldown(player.getUniqueId(), key)) {
            return false;
        }
        ALBCore.api().cooldowns().set(player.getUniqueId(), key, durationMs);
        if (action != null) action.accept(player);
        return true;
    }

    public boolean executeOrNotify(Player player, String cooldownMessage) {
        if (ALBCore.api().cooldowns().isOnCooldown(player.getUniqueId(), key)) {
            String remaining = ALBCore.api().cooldowns()
                    .getFormatted(player.getUniqueId(), key);
            ALBCore.api().message().send(player,
                    cooldownMessage.replace("{remaining}", remaining));
            return false;
        }
        ALBCore.api().cooldowns().set(player.getUniqueId(), key, durationMs);
        if (action != null) action.accept(player);
        return true;
    }
}