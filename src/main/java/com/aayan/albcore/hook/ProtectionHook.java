package com.aayan.albcore.hook;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public final class ProtectionHook {

    private static final Logger LOG = Logger.getLogger("ALBCore");

    private static boolean iridiumEnabled = false;
    private static boolean worldGuardEnabled = false;

    public static void init() {

        if (Bukkit.getPluginManager().getPlugin("IridiumSkyblock") != null) {
            iridiumEnabled = true;
            IridiumHook.init();
            LOG.info("[ALBCore] IridiumSkyblock hooked!");
        }

        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            worldGuardEnabled = true;
            WorldGuardHook.init();
            LOG.info("[ALBCore] WorldGuard hooked!");
        }
    }

    public static boolean canBreakBlock(Player player, Location location) {

        if (iridiumEnabled && !IridiumHook.canBreak(player, location)) {
            return false;
        }

        if (worldGuardEnabled && !WorldGuardHook.canBreak(player, location)) {
            return false;
        }

        return true;
    }
}