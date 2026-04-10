package com.aayan.albcore.gui;

import com.aayan.albcore.ALBCore;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GuiManager implements Listener {

    private static final Map<UUID, ALBGui> openGuis = new HashMap<>();

    public static void register(Player player, ALBGui gui) {
        openGuis.put(player.getUniqueId(), gui);
    }

    public static void unregister(Player player) {
        openGuis.remove(player.getUniqueId());
    }

    public static boolean hasGui(Player player) {
        return openGuis.containsKey(player.getUniqueId());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        ALBGui gui = openGuis.get(player.getUniqueId());
        if (gui == null) return;

        e.setCancelled(true);

        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory() != e.getView().getTopInventory()) return;

        int slot = e.getSlot();
        if (slot < 0) return;

        gui.getHandlers().getOrDefault(slot, p -> {}).accept(player);
    }

    @EventHandler
    public void onDrag(org.bukkit.event.inventory.InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (openGuis.containsKey(player.getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;

        ALBGui gui = openGuis.get(player.getUniqueId());
        if (gui == null) return;

        if (gui.getCloseSound() != null)
            player.playSound(player.getLocation(),
                    gui.getCloseSound(),
                    gui.getSoundVolume(),
                    gui.getSoundPitch());

        unregister(player);
    }
}