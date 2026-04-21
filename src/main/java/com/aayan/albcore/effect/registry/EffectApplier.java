package com.aayan.albcore.effect.registry;

import com.aayan.albcore.ALBCore;
import com.aayan.albcore.util.TextUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.logging.Logger;

public final class EffectApplier {

    private static final Logger LOG = Logger.getLogger("ALBCore");
    private static final String EFFECTS_NBT_KEY = "alb_effects";
    private static final String LORE_COUNT_NBT_KEY = "alb_effects_lore_count";

    private final EffectRegistry registry;
    private final ItemStack item;
    private final Map<String, Integer> toAdd = new LinkedHashMap<>();
    private final Set<String> toRemove = new HashSet<>();

    // Optional lore per effect id
    private final Map<String, LoreEntry> loreEntries = new LinkedHashMap<>();

    public EffectApplier(EffectRegistry registry, ItemStack item) {
        this.registry = registry;
        this.item = item;
    }

    /**
     * Add or upgrade an effect on the item. No lore is added.
     */
    public EffectApplier addEffect(String effectId, int level) {
        effectId = effectId.toLowerCase();
        EffectDefinition def = registry.getEffect(effectId);
        if (def == null) {
            LOG.warning("[ALBCore | Effects] Cannot apply unknown effect: " + effectId);
            return this;
        }

        level = Math.max(1, Math.min(level, def.getMaxLevel()));

        if (!def.canApplyTo(item.getType().name())) {
            LOG.warning("[ALBCore | Effects] Effect '" + effectId
                    + "' cannot be applied to " + item.getType().name());
            return this;
        }

        toAdd.put(effectId, level);
        toRemove.remove(effectId);
        return this;
    }

    /**
     * Add effect AND add lore using the effect's default displayName and description.
     */
    public EffectApplier addEffectWithLore(String effectId, int level) {
        addEffect(effectId, level);
        effectId = effectId.toLowerCase();
        EffectDefinition def = registry.getEffect(effectId);
        if (def != null) {
            int clampedLevel = Math.max(1, Math.min(level, def.getMaxLevel()));
            loreEntries.put(effectId, new LoreEntry(
                    def.formatDisplayName(clampedLevel),
                    def.formatDescription(clampedLevel)
            ));
        }
        return this;
    }

    /**
     * Add effect with custom displayName and lore on the item.
     */
    public EffectApplier addEffectWithLore(String effectId, int level, String displayName, List<String> lore) {
        addEffect(effectId, level);
        effectId = effectId.toLowerCase();
        loreEntries.put(effectId, new LoreEntry(displayName, lore != null ? lore : List.of()));
        return this;
    }

    /**
     * Set custom lore for an effect that was already added via addEffect.
     */
    public EffectApplier withLore(String effectId, String displayName, List<String> lore) {
        effectId = effectId.toLowerCase();
        loreEntries.put(effectId, new LoreEntry(displayName, lore != null ? lore : List.of()));
        return this;
    }

    /**
     * Remove an effect from the item (also removes its lore lines).
     */
    public EffectApplier removeEffect(String effectId) {
        effectId = effectId.toLowerCase();
        toRemove.add(effectId);
        toAdd.remove(effectId);
        loreEntries.remove(effectId);
        return this;
    }

    /**
     * Apply all pending changes to the item.
     */
    public ItemStack apply() {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // Load existing effects from NBT
        Map<String, Integer> currentEffects = loadEffects(meta);

        // Apply changes
        for (Map.Entry<String, Integer> entry : toAdd.entrySet()) {
            currentEffects.put(entry.getKey(), entry.getValue());
        }
        for (String id : toRemove) {
            currentEffects.remove(id);
        }

        // Save effects to NBT
        saveEffects(meta, currentEffects);

        // Handle lore only if there are lore entries to add or effects to remove
        if (!loreEntries.isEmpty() || !toRemove.isEmpty()) {
            rebuildLore(meta, currentEffects);
        }

        item.setItemMeta(meta);
        return item;
    }

    // ── NBT Storage ──────────────────────────────────

    public static Map<String, Integer> loadEffects(ItemMeta meta) {
        Map<String, Integer> effects = new LinkedHashMap<>();
        if (meta == null) return effects;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(ALBCore.getInstance(), EFFECTS_NBT_KEY);
        String raw = pdc.get(key, PersistentDataType.STRING);
        if (raw == null || raw.isEmpty()) return effects;

        for (String entry : raw.split(",")) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                try {
                    effects.put(parts[0].trim().toLowerCase(), Integer.parseInt(parts[1].trim()));
                } catch (NumberFormatException ignored) {}
            }
        }
        return effects;
    }

    public static Map<String, Integer> loadEffects(ItemStack item) {
        if (item == null) return new LinkedHashMap<>();
        return loadEffects(item.getItemMeta());
    }

    private void saveEffects(ItemMeta meta, Map<String, Integer> effects) {
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(ALBCore.getInstance(), EFFECTS_NBT_KEY);

        if (effects.isEmpty()) {
            pdc.remove(key);
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : effects.entrySet()) {
            if (!sb.isEmpty()) sb.append(",");
            sb.append(entry.getKey()).append(":").append(entry.getValue());
        }
        pdc.set(key, PersistentDataType.STRING, sb.toString());
    }

    // ── Lore line count tracking ─────────────────────

    private int getEffectLoreCount(ItemMeta meta) {
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(ALBCore.getInstance(), LORE_COUNT_NBT_KEY);
        Integer count = pdc.get(key, PersistentDataType.INTEGER);
        return count != null ? count : 0;
    }

    private void setEffectLoreCount(ItemMeta meta, int count) {
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(ALBCore.getInstance(), LORE_COUNT_NBT_KEY);
        if (count <= 0) {
            pdc.remove(key);
        } else {
            pdc.set(key, PersistentDataType.INTEGER, count);
        }
    }

    // ── Lore Management ──────────────────────────────

    private void rebuildLore(ItemMeta meta, Map<String, Integer> currentEffects) {
        List<net.kyori.adventure.text.Component> existingLore = meta.lore();
        List<net.kyori.adventure.text.Component> baseLore = new ArrayList<>();

        // Strip old effect lore lines (they're always at the end)
        int oldEffectLineCount = getEffectLoreCount(meta);
        if (existingLore != null) {
            int keepCount = Math.max(0, existingLore.size() - oldEffectLineCount);
            for (int i = 0; i < keepCount; i++) {
                baseLore.add(existingLore.get(i));
            }
        }

        // Build new effect lore lines
        List<net.kyori.adventure.text.Component> effectLines = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : currentEffects.entrySet()) {
            LoreEntry loreEntry = loreEntries.get(entry.getKey());
            if (loreEntry == null) continue;

            if (loreEntry.displayName != null && !loreEntry.displayName.isEmpty()) {
                effectLines.add(TextUtil.parse("<i:false>" + loreEntry.displayName));
            }
            for (String descLine : loreEntry.description) {
                effectLines.add(TextUtil.parse("<i:false>" + descLine));
            }
        }

        // Combine base lore + effect lore
        List<net.kyori.adventure.text.Component> finalLore = new ArrayList<>(baseLore);
        int effectLineCount = 0;

        if (!effectLines.isEmpty()) {
            // Add empty separator line between base lore and effect lore
            if (!finalLore.isEmpty()) {
                effectLines.add(0, TextUtil.parse(""));
            }
            effectLineCount = effectLines.size();
            finalLore.addAll(effectLines);
        }

        // Track how many lines at the end are effect lines (so we can strip them later)
        setEffectLoreCount(meta, effectLineCount);

        meta.lore(finalLore.isEmpty() ? null : finalLore);
    }

    // ── Internal ─────────────────────────────────────

    private record LoreEntry(String displayName, List<String> description) {}
}