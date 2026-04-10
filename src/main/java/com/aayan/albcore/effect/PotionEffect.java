package com.aayan.albcore.effect;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public final class PotionEffect {

    /**
     * Apply a potion effect to a player.
     */
    public void apply(Player player, PotionEffectType type, int duration, int amplifier) {
        apply((LivingEntity) player, type, duration, amplifier);
    }

    /**
     * Apply a potion effect to any living entity (player, mob, etc.).
     */
    public void apply(LivingEntity entity, PotionEffectType type, int duration, int amplifier) {
        if (entity == null || entity.isDead()) return;
        entity.addPotionEffect(new org.bukkit.potion.PotionEffect(
                type, duration, amplifier,
                true,   // ambient — subtle particles
                false,  // particles hidden
                false   // icon hidden
        ));
    }

    /**
     * Apply a potion effect by name string (case-insensitive).
     */
    public void apply(Player player, String typeName, int duration, int amplifier) {
        apply((LivingEntity) player, typeName, duration, amplifier);
    }

    /**
     * Apply a potion effect by name string to any living entity.
     */
    public void apply(LivingEntity entity, String typeName, int duration, int amplifier) {
        PotionEffectType type = PotionEffectType.getByName(typeName.toUpperCase());
        if (type == null) {
            java.util.logging.Logger.getLogger("ALBCore")
                    .warning("[ALBCore | PotionEffect] Unknown potion type: " + typeName);
            return;
        }
        apply(entity, type, duration, amplifier);
    }
}
