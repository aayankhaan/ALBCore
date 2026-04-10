package com.aayan.albcore.input;


import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class InputListener implements Listener {

    // --- Anvil Handling ---
    private static final Map<UUID, Consumer<String>> anvilHandlers = new HashMap<>();

    public static void registerAnvil(Player player, Consumer<String> callback) {
        anvilHandlers.put(player.getUniqueId(), callback);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAnvilPrepare(PrepareAnvilEvent e) {
        if (!anvilHandlers.containsKey(e.getView().getPlayer().getUniqueId())) return;
        
        // Ensure there is always a result item so the client can click it
        ItemStack result = e.getResult();
        if (result == null || result.getType().isAir()) {
            ItemStack left = e.getInventory().getItem(0);
            if (left != null) {
                e.setResult(left.clone());
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAnvilClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        Consumer<String> handler = anvilHandlers.get(player.getUniqueId());
        if (handler == null) return;

        if (e.getClickedInventory() instanceof AnvilInventory && e.getRawSlot() == 2) {
            e.setCancelled(true);

            String text = ((AnvilView) e.getView()).getRenameText();

            anvilHandlers.remove(player.getUniqueId());
            player.closeInventory();

            handler.accept(text == null ? "" : text);
        }
    }

    @EventHandler
    public void onAnvilClose(InventoryCloseEvent e) {
        anvilHandlers.remove(e.getPlayer().getUniqueId());
    }

    // --- Sign Handling ---
    private static final Map<UUID, Consumer<String[]>> signHandlers = new HashMap<>();

    public static void registerSign(Player player, Consumer<String[]> callback) {
        signHandlers.put(player.getUniqueId(), callback);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSignChange(SignChangeEvent e) {
        Consumer<String[]> handler = signHandlers.remove(e.getPlayer().getUniqueId());
        if (handler != null) {
            handler.accept(e.getLines());
        }
    }
}
