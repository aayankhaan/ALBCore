package com.aayan.albcore.item;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class CustomItemRegistry {

    private static final String PDC_KEY = "albcore_item_id";

    private final Map<String, ItemStack> items = new HashMap<>();
    private final NamespacedKey key;

    public CustomItemRegistry(JavaPlugin plugin) {
        this.key = new NamespacedKey(plugin, PDC_KEY);
    }

    // Register

    public void register(String id, ItemStack item) {
        if (id == null || id.isBlank()) {
            warn("Tried to register item with null/empty ID");
            return;
        }
        if (item == null) {
            warn("Tried to register null item for ID: " + id);
            return;
        }

        ItemStack stamped = stamp(item.clone(), id);
        items.put(id.toLowerCase(), stamped);
    }

    // Get

    public Optional<ItemStack> get(String id) {
        if (id == null) return Optional.empty();
        ItemStack item = items.get(id.toLowerCase());
        return Optional.ofNullable(item != null ? item.clone() : null);
    }

    // Identify

    public Optional<String> getId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return Optional.empty();
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        if (!pdc.has(key, PersistentDataType.STRING)) return Optional.empty();
        return Optional.ofNullable(pdc.get(key, PersistentDataType.STRING));
    }

    // Check

    public boolean isCustomItem(ItemStack item) {
        return getId(item).isPresent();
    }

    public boolean isRegistered(String id) {
        if (id == null) return false;
        return items.containsKey(id.toLowerCase());
    }

    // Remove

    public void unregister(String id) {
        if (id == null) return;
        items.remove(id.toLowerCase());
    }

    public void clear() {
        items.clear();
    }

    // Info

    public Set<String> getIds() {
        return Collections.unmodifiableSet(items.keySet());
    }

    public int size() {
        return items.size();
    }

    // Internal

    private ItemStack stamp(ItemStack item, String id) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id.toLowerCase());
        item.setItemMeta(meta);
        return item;
    }

    private void warn(String msg) {
        java.util.logging.Logger.getLogger("ALBCore")
                .warning("[ALBCore | CustomItemRegistry] " + msg);
    }
}