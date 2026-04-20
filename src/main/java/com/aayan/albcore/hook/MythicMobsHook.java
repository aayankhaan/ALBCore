package com.aayan.albcore.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.Optional;
import java.util.logging.Logger;

public final class MythicMobsHook {

    private static final Logger LOG = Logger.getLogger("ALBCore");
    private static boolean enabled = false;

    public static void init() {
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            enabled = true;
            LOG.info("[ALBCore] MythicMobs hooked!");
        } else {
            LOG.info("[ALBCore] MythicMobs not found — MythicMobs features disabled.");
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isMythicMob(Entity entity) {
        if (!enabled || entity == null) return false;
        try {
            return io.lumine.mythic.bukkit.MythicBukkit.inst()
                    .getMobManager()
                    .getActiveMob(entity.getUniqueId())
                    .isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    public static Optional<String> getMobType(Entity entity) {
        if (!enabled || entity == null) return Optional.empty();
        try {
            var activeMob = io.lumine.mythic.bukkit.MythicBukkit.inst()
                    .getMobManager()
                    .getActiveMob(entity.getUniqueId());
            return activeMob.map(mob -> mob.getMobType());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<String> getDisplayName(Entity entity) {
        if (!enabled || entity == null) return Optional.empty();
        try {
            var activeMob = io.lumine.mythic.bukkit.MythicBukkit.inst()
                    .getMobManager()
                    .getActiveMob(entity.getUniqueId());
            return activeMob.map(mob -> mob.getDisplayName());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}