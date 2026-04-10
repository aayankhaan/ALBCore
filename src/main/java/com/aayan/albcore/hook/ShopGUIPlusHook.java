package com.aayan.albcore.hook;

import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Logger;

public final class ShopGUIPlusHook {

    private static final Logger LOG = Logger.getLogger("ALBCore");
    private boolean enabled = false;

    public void init() {
        if (Bukkit.getPluginManager().getPlugin("ShopGUIPlus") != null) {
            enabled = true;
            LOG.info("[ALBCore] ShopGUIPlus hooked!");
        } else {
            LOG.info("[ALBCore] ShopGUIPlus not found — shop features disabled.");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the buy price of an item stack.
     * @param itemStack The item stack to check.
     * @return The buy price, or -1.0 if not in shop.
     */
    public double getBuyPrice(ItemStack itemStack) {
        if (!enabled) return -1.0;
        return ShopGuiPlusApi.getItemStackPriceBuy(itemStack);
    }

    /**
     * Get the sell price of an item stack.
     * @param itemStack The item stack to check.
     * @return The sell price, or -1.0 if not sellable.
     */
    public double getSellPrice(ItemStack itemStack) {
        if (!enabled) return -1.0;
        return ShopGuiPlusApi.getItemStackPriceSell(itemStack);
    }

    /**
     * Get the buy price of an item stack for a specific player (including modifiers).
     * @param player The player.
     * @param itemStack The item stack.
     * @return The buy price, or -1.0 if not in shop.
     */
    public double getBuyPrice(Player player, ItemStack itemStack) {
        if (!enabled) return -1.0;
        return ShopGuiPlusApi.getItemStackPriceBuy(player, itemStack);
    }

    /**
     * Get the sell price of an item stack for a specific player (including modifiers).
     * @param player The player.
     * @param itemStack The item stack.
     * @return The sell price, or -1.0 if not sellable.
     */
    public double getSellPrice(Player player, ItemStack itemStack) {
        if (!enabled) return -1.0;
        return ShopGuiPlusApi.getItemStackPriceSell(player, itemStack);
    }
}
