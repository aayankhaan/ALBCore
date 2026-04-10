package com.aayan.albcore.effect;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public final class LightningEffect {

    /**
     * Strike lightning at a location (visual only, no damage).
     */
    public void strike(Location location, boolean damage) {
        if (location == null || location.getWorld() == null) return;
        if (damage) {
            location.getWorld().strikeLightning(location);
        } else {
            location.getWorld().strikeLightningEffect(location);
        }
    }

    /**
     * Strike lightning at a location with custom damage to nearby entity.
     * Always spawns a visual lightning effect, then applies custom damage
     * to the target entity separately.
     *
     * @param location where to strike
     * @param target   entity to deal damage to (can be null for visual only)
     * @param damage   custom damage amount in HP (2 = 1 heart). 0 = visual only.
     */
    public void strike(Location location, Entity target, double damage) {
        if (location == null || location.getWorld() == null) return;

        // always visual effect — no vanilla lightning damage
        location.getWorld().strikeLightningEffect(location);

        // apply custom damage if > 0 and target is living
        if (damage > 0 && target instanceof LivingEntity living) {
            if (!living.isDead()) {
                living.damage(damage);
            }
        }
    }

    /** Strike at player's location (visual only). */
    public void strike(Player player) {
        strike(player.getLocation(), false);
    }

    /** Strike at player's location with vanilla damage toggle. */
    public void strike(Player player, boolean damage) {
        strike(player.getLocation(), damage);
    }

    /** Strike at entity's location with vanilla damage toggle. */
    public void strike(Entity entity, boolean damage) {
        if (entity == null) return;
        strike(entity.getLocation(), damage);
    }

    /** Strike at entity's location with custom damage amount. */
    public void strike(Entity entity, double damage) {
        if (entity == null) return;
        strike(entity.getLocation(), entity, damage);
    }
}
