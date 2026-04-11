package com.aayan.albcore.input;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class InputListener implements Listener {

    private static final Map<UUID, Consumer<String>> anvilHandlers = new HashMap<>();

    public static void registerAnvil(Player player, Consumer<String> callback) {
        anvilHandlers.put(player.getUniqueId(), callback);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAnvilPrepare(PrepareAnvilEvent e) {
        if (!(e.getView() instanceof AnvilView view)) return;
        if (!anvilHandlers.containsKey(view.getPlayer().getUniqueId())) return;

        ItemStack left = e.getInventory().getItem(0);
        if (left != null) {
            e.setResult(left.clone());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAnvilClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        Consumer<String> handler = anvilHandlers.get(player.getUniqueId());
        if (handler == null) return;

        if (!(e.getView() instanceof AnvilView view)) return;

        if (!(e.getClickedInventory() instanceof AnvilInventory anvilInv)) {
            e.setCancelled(true);
            return;
        }

        if (e.getRawSlot() != 2) {
            e.setCancelled(true);
            return;
        }

        e.setCancelled(true);

        String text = view.getRenameText();

        // Clear ALL anvil slots BEFORE closing so Bukkit has nothing to return
        anvilInv.setItem(0, null);
        anvilInv.setItem(1, null);
        anvilInv.setItem(2, null);

        player.setItemOnCursor(null);

        anvilHandlers.remove(player.getUniqueId());
        player.closeInventory();
        player.updateInventory();

        handler.accept(text == null ? "" : text);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAnvilDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!anvilHandlers.containsKey(player.getUniqueId())) return;

        if (e.getView() instanceof AnvilView) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAnvilClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;

        // Only runs if closed WITHOUT clicking result (e.g. pressing Escape)
        if (!anvilHandlers.containsKey(player.getUniqueId())) return;

        if (e.getInventory() instanceof AnvilInventory inv) {
            inv.setItem(0, null);
            inv.setItem(1, null);
            inv.setItem(2, null);
        }

        player.setItemOnCursor(null);
        player.updateInventory();

        anvilHandlers.remove(player.getUniqueId());
    }

}