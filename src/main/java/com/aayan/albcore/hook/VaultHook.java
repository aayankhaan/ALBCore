package com.aayan.albcore.hook;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Logger;

public final class VaultHook {

    private static final Logger LOG = Logger.getLogger("ALBCore");
    private Economy economy = null;
    private boolean enabled = false;

    public void init() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            LOG.info("[ALBCore] Vault not found — economy support disabled.");
            return;
        }

        RegisteredServiceProvider<Economy> rsp =
                Bukkit.getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            LOG.warning("[ALBCore] Vault found but no economy plugin detected.");
            return;
        }

        economy = rsp.getProvider();
        enabled = true;
        LOG.info("[ALBCore] Vault hooked — economy support enabled.");
    }

    // Give money

    public boolean give(Player player, double amount) {
        if (!enabled) {
            LOG.warning("[ALBCore | Vault] Economy not available.");
            return false;
        }
        economy.depositPlayer(player, amount);
        return true;
    }

    // Take money

    public boolean take(Player player, double amount) {
        if (!enabled) return false;
        if (!has(player, amount)) return false;
        economy.withdrawPlayer(player, amount);
        return true;
    }

    // Check balance

    public double getBalance(Player player) {
        if (!enabled) return 0;
        return economy.getBalance(player);
    }

    public boolean has(Player player, double amount) {
        if (!enabled) return false;
        return economy.has(player, amount);
    }

    // Format

    public String format(double amount) {
        if (!enabled) return String.valueOf(amount);
        return economy.format(amount);
    }

    public boolean isEnabled() {
        return enabled;
    }
}