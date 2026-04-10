package com.aayan.albcore.util;

import com.aayan.albcore.ALBCore;

public final class DebugLogger {

    private final ALBCore plugin;

    public DebugLogger(ALBCore plugin) {
        this.plugin = plugin;
    }

    public void log(String system, String message) {
        if (!ALBCore.api().config().isDebug()) return;
        plugin.getLogger().info("[DEBUG | " + system + "] " + message);
    }

    public void warn(String system, String message) {
        if (!ALBCore.api().config().isDebug()) return;
        plugin.getLogger().warning("[DEBUG | " + system + "] " + message);
    }
}