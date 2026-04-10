package com.aayan.albcore.hook;

import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.User;
import com.iridium.iridiumskyblock.dependencies.iridiumteams.PermissionType;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class IridiumHook {

    public static void init() {}

    public static boolean canBreak(Player player, Location location) {

        try {
            IridiumSkyblockAPI api = IridiumSkyblockAPI.getInstance();

            User user = api.getUser(player);
            Optional<Island> island = api.getIslandViaLocation(location);

            if (island.isPresent()) {
                return api.getIslandPermission(
                        island.get(),
                        user,
                        PermissionType.BLOCK_BREAK
                );
            }

        } catch (Exception ignored) {}

        return true;
    }
}