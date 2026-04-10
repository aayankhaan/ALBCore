package com.aayan.albcore.hook;

import com.aayan.albcore.ALBCore;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public final class ALBCorePlaceholders extends ALBPlaceholderExpansion {

    public ALBCorePlaceholders() {
        super("albcore", "Aayan", "1.0.0");
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {

        // future-proof: allow nested placeholders
        if (player != null && params.contains("%")) {
            params = PlaceholderAPI.setPlaceholders(player, params);
        }

        return ALBCore.api().placeholders().resolve(params, player);
    }
}