package com.aayan.albcore.config;

import com.aayan.albcore.ALBCore;
import org.bukkit.configuration.file.FileConfiguration;

public final class CoreConfig {

    private final ALBCore plugin;

    private boolean debug;
    private String prefix;
    private String cooldownFormat;
    private boolean logItemRegistration;
    private boolean checkUpdates;
    private boolean metrics;

    public CoreConfig(ALBCore plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        debug               = cfg.getBoolean("debug", false);
        prefix              = cfg.getString("prefix",
                "<dark_gray>[<gradient:#00aaff:#aa00ff>ALBCore</gradient><dark_gray>] ");
        cooldownFormat      = cfg.getString("cooldown-format", "%.1fs");
        logItemRegistration = cfg.getBoolean("log-item-registration", false);
        checkUpdates        = cfg.getBoolean("check-updates", true);
        metrics             = cfg.getBoolean("metrics", true);
    }

    public boolean isDebug()               { return debug; }
    public String getPrefix()              { return prefix; }
    public String getCooldownFormat()      { return cooldownFormat; }
    public boolean isLogItemRegistration() { return logItemRegistration; }
    public boolean isCheckUpdates()        { return checkUpdates; }
    public boolean isMetrics()             { return metrics; }
}