package com.aayan.albcore.hook;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class WorldGuardHook {

    public static void init() {}

    public static boolean canBreak(Player player, Location location) {
        try {
            var wgPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

            var query = WorldGuard.getInstance()
                    .getPlatform()
                    .getRegionContainer()
                    .createQuery();

            return query.testBuild(BukkitAdapter.adapt(location), wgPlayer);

        } catch (Exception e) {
            return true;
        }
    }
}