package com.aayan.albcore.ability.trigger;

import com.aayan.albcore.ALBCore;
import com.aayan.albcore.condition.ConditionSet;
import com.aayan.albcore.util.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class OnFishTrigger implements Listener {

    private record FishEntry(String itemId, long cooldownMs,
                             String cooldownKey, String cooldownMsg,
                             ConditionSet conditions,
                             Consumer<Player> action) {}

    private final List<FishEntry> entries = new ArrayList<>();

    public void addCallback(String itemId, long cooldownMs, String cooldownKey,
                            String cooldownMsg, ConditionSet conditions,
                            Consumer<Player> action) {
        entries.add(new FishEntry(itemId, cooldownMs, cooldownKey,
                cooldownMsg, conditions, action));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFish(PlayerFishEvent e) {
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

        Player player = e.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();

        // check offhand too — fishing rod could be in either
        ItemStack offhand = player.getInventory().getItemInOffHand();

        // resolve id from main hand, then offhand
        String registryId = ALBCore.api().registry().getId(held)
                .orElseGet(() -> ALBCore.api().registry().getId(offhand).orElse(null));
        process(registryId, player);
    }

    private void process(String registryId, Player player) {
        for (FishEntry entry : entries) {
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
                return;
            }

            if (entry.cooldownMs() > 0)
                ALBCore.api().cooldowns().set(player.getUniqueId(),
                        entry.cooldownKey(), entry.cooldownMs());

            entry.action().accept(player);
        }
    }

    public void clear() { entries.clear(); }
}