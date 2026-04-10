package com.aayan.albcore.ability.trigger;

import com.aayan.albcore.ALBCore;
import com.aayan.albcore.condition.ConditionSet;
import com.aayan.albcore.util.TextUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public final class OnBreakTrigger implements Listener {

    private record BreakEntry(String itemId, long cooldownMs,
                              String cooldownKey, String cooldownMsg,
                              ConditionSet conditions,
                              BiConsumer<Player, Block> action) {}

    private final List<BreakEntry> entries = new ArrayList<>();

    public void addCallback(String itemId, long cooldownMs, String cooldownKey,
                            String cooldownMsg, ConditionSet conditions,
                            BiConsumer<Player, Block> action) {
        entries.add(new BreakEntry(itemId, cooldownMs, cooldownKey,
                cooldownMsg, conditions, action));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        Block block    = e.getBlock();

        String registryId = ALBCore.api().registry().getId(held).orElse(null);

        for (BreakEntry entry : entries) {
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

            entry.action().accept(player, block);
        }
    }

    public void clear() { entries.clear(); }
}