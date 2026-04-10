package com.aayan.albcore.effect;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

/**
 * Adds or removes bonus max health (hearts) from a player.
 * Uses attribute modifiers so changes stack properly and can be cleanly reverted.
 * <p>
 * 1 heart = 2 health points. So addHearts(player, "mykey", 2) adds 2 hearts (4 HP).
 * <p>
 * Compatible with Paper 1.21+ API (NamespacedKey-based AttributeModifier).
 */
public final class AddHeartsEffect {

    private static final Logger LOG = Logger.getLogger("ALBCore");

    /**
     * Add bonus hearts to a player.
     * If a modifier with this key already exists, it is replaced.
     *
     * @param player the player
     * @param key    unique key for this modifier (used to remove later)
     * @param hearts number of hearts to add (can be negative to remove hearts)
     */
    public void addHearts(Player player, String key, double hearts) {
        if (player == null) return;
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr == null) return;

        // remove existing modifier with same key first
        removeHearts(player, key);

        // 1 heart = 2 HP
        double healthPoints = hearts * 2.0;

        NamespacedKey nsKey = toNamespacedKey(key);
        AttributeModifier modifier = new AttributeModifier(
                nsKey, healthPoints,
                AttributeModifier.Operation.ADD_NUMBER);

        attr.addModifier(modifier);

        // if we added health, bump current health so player sees it immediately
        if (healthPoints > 0 && !player.isDead()) {
            player.setHealth(Math.min(attr.getValue(), player.getHealth() + healthPoints));
        }
    }

    /**
     * Remove bonus hearts that were added with the given key.
     *
     * @param player the player
     * @param key    the same key used in addHearts
     */
    public void removeHearts(Player player, String key) {
        if (player == null) return;
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr == null) return;

        NamespacedKey nsKey = toNamespacedKey(key);
        for (AttributeModifier mod : attr.getModifiers()) {
            if (mod.getKey().equals(nsKey)) {
                attr.removeModifier(mod);
                break;
            }
        }

        // clamp current health if it exceeds new max
        if (!player.isDead() && player.getHealth() > attr.getValue()) {
            player.setHealth(attr.getValue());
        }
    }

    /**
     * Check if a player currently has bonus hearts with the given key.
     */
    public boolean hasHearts(Player player, String key) {
        if (player == null) return false;
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr == null) return false;
        NamespacedKey nsKey = toNamespacedKey(key);
        for (AttributeModifier mod : attr.getModifiers()) {
            if (mod.getKey().equals(nsKey)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convert a string key to a NamespacedKey under the "albcore" namespace.
     * Sanitizes the key to only contain valid characters (lowercase, a-z, 0-9, /, ., _, -).
     */
    private NamespacedKey toNamespacedKey(String key) {
        // sanitize: lowercase, replace invalid chars with underscores
        String sanitized = key.toLowerCase()
                .replaceAll("[^a-z0-9/._-]", "_");
        return new NamespacedKey("albcore", sanitized);
    }
}
