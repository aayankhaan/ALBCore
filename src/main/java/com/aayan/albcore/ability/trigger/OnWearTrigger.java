package com.aayan.albcore.ability.trigger;

import com.aayan.albcore.ALBCore;
import com.aayan.albcore.condition.ConditionSet;
import com.aayan.albcore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Trigger that fires while a player is wearing armor.
 * <p>
 * Each entry can provide:
 * <ul>
 *   <li>{@code onEquip}   — called once when the armor is first detected as worn</li>
 *   <li>{@code onUnequip} — called once when the armor is removed (or player logs out)</li>
 * </ul>
 * The trigger checks every 20 ticks (1 second) by scanning all armor slots.
 */
public final class OnWearTrigger implements Listener {

    private final ALBCore plugin;

    public OnWearTrigger(ALBCore plugin) {
        this.plugin = plugin;
        start();
    }

    private record WearEntry(String itemId, ConditionSet conditions,
                             Consumer<Player> onEquip,
                             Consumer<Player> onUnequip) {}

    private final List<WearEntry> entries = new ArrayList<>();

    // tracks which (player, entryIndex) combos are currently "active"
    // so we know when to fire equip vs unequip
    private final Map<UUID, Set<Integer>> activeEntries = new ConcurrentHashMap<>();

    /**
     * Register a wear callback.
     *
     * @param itemId    registered item ID to match in armor slots (null = match any armor)
     * @param onEquip   called once when armor is first detected as worn
     * @param onUnequip called once when armor is removed or player leaves
     */
    public void addCallback(String itemId, ConditionSet conditions,
                            Consumer<Player> onEquip,
                            Consumer<Player> onUnequip) {
        entries.add(new WearEntry(itemId, conditions, onEquip, onUnequip));
    }

    private void start() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Set<Integer> currentlyActive = activeEntries
                        .computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());

                for (int i = 0; i < entries.size(); i++) {
                    WearEntry entry = entries.get(i);
                    boolean wearing = isWearing(player, entry.itemId());
                    boolean conditionsMet = entry.conditions().evaluate(player);
                    boolean shouldBeActive = wearing && conditionsMet;
                    boolean wasActive = currentlyActive.contains(i);

                    if (shouldBeActive && !wasActive) {
                        // just equipped
                        currentlyActive.add(i);
                        if (entry.onEquip() != null) entry.onEquip().accept(player);
                    } else if (!shouldBeActive && wasActive) {
                        // just unequipped
                        currentlyActive.remove(i);
                        if (entry.onUnequip() != null) entry.onUnequip().accept(player);
                    }
                }
            }
        }, 0L, 20L); // check every second
    }

    private boolean isWearing(Player player, String itemId) {
        if (itemId == null) {
            // wildcard — wearing any armor at all
            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (armor != null && !armor.getType().isAir()) return true;
            }
            return false;
        }

        // check each armor slot for the specific registered item
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null || armor.getType().isAir()) continue;
            String id = ALBCore.api().registry().getId(armor).orElse(null);
            if (itemId.equals(id)) return true;
        }
        return false;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Set<Integer> active = activeEntries.remove(player.getUniqueId());
        if (active == null) return;

        // fire all unequip callbacks on logout so effects don't persist
        for (int idx : active) {
            if (idx < entries.size()) {
                WearEntry entry = entries.get(idx);
                if (entry.onUnequip() != null) entry.onUnequip().accept(player);
            }
        }
    }

    public void clear() {
        // fire unequip for all currently active before clearing
        for (var mapEntry : activeEntries.entrySet()) {
            Player player = Bukkit.getPlayer(mapEntry.getKey());
            if (player == null) continue;
            for (int idx : mapEntry.getValue()) {
                if (idx < entries.size()) {
                    WearEntry entry = entries.get(idx);
                    if (entry.onUnequip() != null) entry.onUnequip().accept(player);
                }
            }
        }
        activeEntries.clear();
        entries.clear();
    }
}
