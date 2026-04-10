package com.aayan.albcore.ability.trigger;

import com.aayan.albcore.ALBCore;
import com.aayan.albcore.condition.ConditionSet;
import com.aayan.albcore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class OnHoldTrigger {

    private final ALBCore plugin;
    private final List<HoldEntry> entries = new ArrayList<>();

    public OnHoldTrigger(ALBCore plugin) {
        this.plugin = plugin;
        start();
    }

    private record HoldEntry(String itemId, long cooldownMs,
                             String cooldownKey, String cooldownMsg,
                             ConditionSet conditions,
                             Consumer<Player> action) {}

    public void addCallback(String itemId, long cooldownMs, String cooldownKey,
                            String cooldownMsg, ConditionSet conditions,
                            Consumer<Player> action) {
        entries.add(new HoldEntry(itemId, cooldownMs, cooldownKey,
                cooldownMsg, conditions, action));
    }

    private void start() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemStack held = player.getInventory().getItemInMainHand();
                if (held.getType().isAir()) continue;

                // resolve registered item id (may be empty for plain items)
                String registryId = ALBCore.api().registry().getId(held).orElse(null);

                for (HoldEntry entry : entries) {
                    // if entry has an itemId it must match; null = wildcard (fires for any item)
                    if (entry.itemId() != null && !entry.itemId().equals(registryId)) continue;
                    if (!entry.conditions().evaluate(player)) continue;

                    if (entry.cooldownMs() > 0 && ALBCore.api().cooldowns()
                            .isOnCooldown(player.getUniqueId(), entry.cooldownKey())) {
                        if (entry.cooldownMsg() != null) {
                            String remaining = ALBCore.api().cooldowns()
                                    .getFormatted(player.getUniqueId(), entry.cooldownKey());
                            TextUtil.sendActionbar(player,
                                    entry.cooldownMsg().replace("{remaining}", remaining));
                        }
                        continue;
                    }

                    if (entry.cooldownMs() > 0)
                        ALBCore.api().cooldowns().set(player.getUniqueId(),
                                entry.cooldownKey(), entry.cooldownMs());

                    entry.action().accept(player);
                }
            }
        }, 0L, 1L);
    }

    public void clear() {
        entries.clear();
    }
}