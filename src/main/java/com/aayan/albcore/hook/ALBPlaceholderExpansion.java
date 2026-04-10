package com.aayan.albcore.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public abstract class ALBPlaceholderExpansion extends PlaceholderExpansion {

    private final String identifier;
    private final String author;
    private final String version;

    protected ALBPlaceholderExpansion(String identifier, String author, String version) {
        this.identifier = identifier;
        this.author = author;
        this.version = version;
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    @Override
    public @NotNull String getAuthor() {
        return author;
    }

    @Override
    public @NotNull String getVersion() {
        return version;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public abstract String onRequest(OfflinePlayer player, @NotNull String params);

    @Override
    public boolean register() {
        if (com.aayan.albcore.util.PlaceholderUtil.isEnabled()) {
            boolean registered = super.register();

            java.util.logging.Logger.getLogger("ALBCore")
                    .info("[ALBCore] Registered placeholder expansion: %" + identifier + "_...%");

            return registered;
        }
        return false;
    }
}