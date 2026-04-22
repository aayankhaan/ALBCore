package com.aayan.albcore.effect.registry;

import com.aayan.albcore.ALBCore;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import java.util.logging.Logger;

/**
 * Executes EffectActions parsed from yml config.
 * Maps each action type string to the corresponding ALBCore effect call.
 */
public final class EffectActionExecutor {

    private static final Logger LOG = Logger.getLogger("ALBCore");

    /**
     * Execute a single action for an attack trigger.
     */
    public static void executeAttack(EffectAction action, Player player, Entity target, int level, EntityDamageByEntityEvent event) {
        switch (action.getType()) {
            case "DAMAGE_BOOST" -> {
                double multiplier = action.getDouble("multiplier", 1.0);
                // If "damage" is explicitly set in the config, use the old boost method (new damage event)
                if (action.getParams().containsKey("damage")) {
                    double damage = action.evaluateExpression("damage", level, 0);
                    if (target instanceof LivingEntity living) {
                        ALBCore.api().damageBoost().boost(living, player, damage, multiplier);
                    }
                } else if (event != null) {
                    // Otherwise, multiply the current attack damage
                    ALBCore.api().damageBoost().apply(event, multiplier);
                }
            }
            case "LIGHTNING" -> {
                double damage = action.evaluateExpression("damage", level, 0);
                if (target != null) {
                    ALBCore.api().lightning().strike(target.getLocation(), target, damage);
                }
            }
            case "HEAL" -> {
                double amount = action.evaluateExpression("amount", level, 2);
                ALBCore.api().heal().heal(player, amount);
            }
            case "POTION" -> applyPotion(action, player, target, level);
            case "POTION_TARGET" -> {
                if (target instanceof LivingEntity living) {
                    applyPotionToEntity(action, living, level);
                }
            }
            case "PARTICLE" -> spawnParticle(action, player, target, level);
            case "SOUND" -> playSound(action, player, level);
            case "MESSAGE" -> {
                String msg = action.getString("message", "");
                msg = msg.replace("{level}", String.valueOf(level));
                com.aayan.albcore.util.TextUtil.send(player, msg);
            }
            case "ACTIONBAR" -> {
                String msg = action.getString("message", "");
                msg = msg.replace("{level}", String.valueOf(level));
                com.aayan.albcore.util.TextUtil.sendActionbar(player, msg);
            }
            case "CONSOLE_COMMAND" -> {
                String cmd = action.getString("command", "")
                        .replace("{player}", player.getName())
                        .replace("{level}", String.valueOf(level));
                ALBCore.api().consoleCmd().run(cmd);
            }
            case "ADD_HEARTS" -> {
                double hearts = action.evaluateExpression("hearts", level, 1);
                String key = action.getString("key", "effect_hearts_" + level);
                ALBCore.api().addHearts().addHearts(player, key, hearts);
            }
            default -> LOG.warning("[ALBCore | Effects] Unknown action type: " + action.getType());
        }
    }

    /**
     * Execute a single action for a defend trigger.
     */
    public static void executeDefend(EffectAction action, Player player, Entity attacker, int level, EntityDamageEvent event) {
        if (action.getType().equals("DAMAGE_BOOST")) {
             double multiplier = action.getDouble("multiplier", 1.0);
             if (event != null && !action.getParams().containsKey("damage")) {
                 // For defense, we might want to REDUCE damage (multiplier < 1)
                 event.setDamage(event.getDamage() * multiplier);
                 return;
             }
        }
        // Most actions are the same, just swap target context
        executeAttack(action, player, attacker, level, event instanceof EntityDamageByEntityEvent e ? e : null);
    }

    /**
     * Execute a single action for a generic trigger (ON_HOLD, ON_SNEAK, ON_CLICK, etc).
     */
    public static void executeGeneric(EffectAction action, Player player, int level) {
        executeAttack(action, player, null, level, null);
    }

    // ── Helpers ──────────────────────────────────────

    private static void applyPotion(EffectAction action, Player player, Entity target, int level) {
        try {
            String effectName = action.getString("effect", "SPEED");
            int duration = (int) action.evaluateExpression("duration", level, 100);
            int amplifier = (int) action.evaluateExpression("amplifier", level, 0);
            PotionEffectType type = PotionEffectType.getByName(effectName.toUpperCase());
            if (type != null) {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(type, duration, amplifier));
            }
        } catch (Exception e) {
            LOG.warning("[ALBCore | Effects] Failed to apply potion: " + e.getMessage());
        }
    }

    private static void applyPotionToEntity(EffectAction action, LivingEntity target, int level) {
        try {
            String effectName = action.getString("effect", "SLOWNESS");
            int duration = (int) action.evaluateExpression("duration", level, 100);
            int amplifier = (int) action.evaluateExpression("amplifier", level, 0);
            PotionEffectType type = PotionEffectType.getByName(effectName.toUpperCase());
            if (type != null) {
                target.addPotionEffect(new org.bukkit.potion.PotionEffect(type, duration, amplifier));
            }
        } catch (Exception e) {
            LOG.warning("[ALBCore | Effects] Failed to apply potion to target: " + e.getMessage());
        }
    }

    private static void spawnParticle(EffectAction action, Player player, Entity target, int level) {
        try {
            String particleName = action.getString("particle", "FLAME");
            int count = action.getInt("count", 10);
            double offsetX = action.getDouble("offset-x", 0.3);
            double offsetY = action.getDouble("offset-y", 0.3);
            double offsetZ = action.getDouble("offset-z", 0.3);
            double speed = action.getDouble("speed", 0.05);

            Particle particle = Particle.valueOf(particleName.toUpperCase());
            org.bukkit.Location loc = target != null ? target.getLocation() : player.getLocation();

            ALBCore.api().particles(particle)
                    .location(loc.add(0, 1, 0))
                    .count(count)
                    .offset(offsetX, offsetY, offsetZ)
                    .speed(speed)
                    .spawn();
        } catch (Exception e) {
            LOG.warning("[ALBCore | Effects] Failed to spawn particle: " + e.getMessage());
        }
    }

    private static void playSound(EffectAction action, Player player, int level) {
        try {
            String soundName = action.getString("sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
            float volume = action.getFloat("volume", 1.0f);
            float pitch = action.getFloat("pitch", 1.0f);
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (Exception e) {
            LOG.warning("[ALBCore | Effects] Failed to play sound: " + e.getMessage());
        }
    }
}