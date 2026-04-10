package com.aayan.albcore.hook;

import org.bukkit.Bukkit;
import java.util.logging.Logger;

public final class ProtocolLibHook {

    private static final Logger LOG = Logger.getLogger("ALBCore");
    private boolean enabled = false;

    public void init() {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            enabled = true;
            LOG.info("[ALBCore] ProtocolLib hooked!");
        } else {
            LOG.info("[ALBCore] ProtocolLib not found — certain features like custom sign input will be disabled.");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
