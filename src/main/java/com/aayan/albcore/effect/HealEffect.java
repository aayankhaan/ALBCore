package com.aayan.albcore.effect;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public final class HealEffect {

    /**
     * Instantly heal a player by a set amount of health points.
     * 1 heart = 2 health points.
     */
    public void heal(Player player, double amount) {
        heal((LivingEntity) player, amount);
    }

    /**
     * Heal any living entity by health points.
     */
    public void heal(LivingEntity entity, double amount) {
        if (entity == null || entity.isDead()) return;
        double maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double newHealth = Math.min(maxHealth, entity.getHealth() + amount);
        entity.setHealth(newHealth);
    }

    /**
     * Heal by hearts (1 heart = 2 HP).
     */
    public void healHearts(Player player, double hearts) {
        heal((LivingEntity) player, hearts * 2.0);
    }

    public void healHearts(LivingEntity entity, double hearts) {
        heal(entity, hearts * 2.0);
    }

    /** Fully heal to max health. */
    public void fullHeal(Player player) {
        fullHeal((LivingEntity) player);
    }

    public void fullHeal(LivingEntity entity) {
        if (entity == null || entity.isDead()) return;
        double maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        entity.setHealth(maxHealth);
    }
}
