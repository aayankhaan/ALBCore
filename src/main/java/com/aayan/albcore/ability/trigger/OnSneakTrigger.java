package com.aayan.albcore.ability.trigger;

import com.aayan.albcore.ALBCore;
import com.aayan.albcore.condition.ConditionSet;
import com.aayan.albcore.util.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class OnSneakTrigger implements Listener {

    private record SneakEntry(String itemId, long cooldownMs,
                              String cooldownKey, String cooldownMsg,
                              ConditionSet conditions,
                              Consumer<Player> action) {}

    private final List<SneakEntry> entries = new ArrayList<>();

    public void addCallback(String itemId, long cooldownMs, String cooldownKey,
                            String cooldownMsg, ConditionSet conditions,
                            Consumer<Player> action) {
        entries.add(new SneakEntry(itemId, cooldownMs, cooldownKey,
                cooldownMsg, conditions, action));
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        if (!e.isSneaking()) return;

        Player player = e.getPlayer();
        var held = player.getInventory().getItemInMainHand();
        if (held.getType().isAir()) return;

        String registryId = ALBCore.api().registry().getId(held).orElse(null);

        for (SneakEntry entry : entries) {
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

    public void clear() {
        entries.clear();
    }
}