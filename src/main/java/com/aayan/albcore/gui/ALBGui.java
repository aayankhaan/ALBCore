package com.aayan.albcore.gui;

import com.aayan.albcore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.function.Consumer;

public final class ALBGui {

    private final String title;
    private final int rows;
    private final ItemStack filler;
    private final Sound openSound;
    private final Sound closeSound;
    private final float soundVolume;
    private final float soundPitch;
    private final Map<Integer, ItemStack> slots;
    private final Map<Integer, Consumer<Player>> handlers;

    ALBGui(String title, int rows, ItemStack filler,
           Sound openSound, Sound closeSound,
           float soundVolume, float soundPitch,
           Map<Integer, ItemStack> slots,
           Map<Integer, Consumer<Player>> handlers) {
        this.title       = title;
        this.rows        = rows;
        this.filler      = filler;
        this.openSound   = openSound;
        this.closeSound  = closeSound;
        this.soundVolume = soundVolume;
        this.soundPitch  = soundPitch;
        this.slots       = slots;
        this.handlers    = handlers;
    }

    public void open(Player player) {
        GuiManager.unregister(player);

        Inventory inv = Bukkit.createInventory(null, rows * 9,
                TextUtil.parse(title, player));

        if (filler != null) {
            for (int i = 0; i < rows * 9; i++) {
                inv.setItem(i, filler);
            }
        }

        slots.forEach(inv::setItem);

        player.openInventory(inv);

        // register AFTER opening so handlers match current inventory
        GuiManager.register(player, this);

        if (openSound != null)
            player.playSound(player.getLocation(),
                    openSound, soundVolume, soundPitch);
    }

    public Map<Integer, Consumer<Player>> getHandlers() {
        return handlers;
    }

    public Sound getCloseSound() {
        return closeSound;
    }

    public float getSoundVolume() { return soundVolume; }
    public float getSoundPitch()  { return soundPitch; }
}
