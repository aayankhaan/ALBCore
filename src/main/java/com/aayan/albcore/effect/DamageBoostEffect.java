package com.aayan.albcore.effect;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class DamageBoostEffect {


    public void apply(EntityDamageByEntityEvent event, double multiplier) {
        if (event == null || multiplier <= 0) return;
        event.setDamage(event.getDamage() * multiplier);
    }

    public double calculate(double originalDamage, double multiplier) {
        return originalDamage * multiplier;
    }


    public void boost(LivingEntity target, double originalDamage, double multiplier) {
        if (target == null || target.isDead()) return;
        double boosted = originalDamage * multiplier;
        target.damage(boosted);
    }

    public void boost(LivingEntity target, Player attacker, double originalDamage, double multiplier) {
        if (target == null || target.isDead() || attacker == null) return;
        double boosted = originalDamage * multiplier;
        target.damage(boosted, attacker);
    }
}