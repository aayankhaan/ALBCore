package com.aayan.albcore.gui;

import com.aayan.albcore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class GuiBuilder {

    private String title = "<dark_gray>Menu";
    private int rows = 3;
    private ItemStack filler = null;
    private Sound openSound = null;
    private Sound closeSound = null;
    private float soundVolume = 1.0f;
    private float soundPitch = 1.0f;

    private final Map<Integer, ItemStack> slots = new HashMap<>();
    private final Map<Integer, Consumer<Player>> handlers = new HashMap<>();

    // Title

    public GuiBuilder title(String title) {
        this.title = title;
        return this;
    }

    // Rows

    public GuiBuilder rows(int rows) {
        this.rows = Math.max(1, Math.min(6, rows));
        return this;
    }

    // Filler

    public GuiBuilder filler(ItemStack item) {
        this.filler = item;
        return this;
    }

    // Sounds

    public GuiBuilder openSound(Sound sound, float volume, float pitch) {
        this.openSound   = sound;
        this.soundVolume = volume;
        this.soundPitch  = pitch;
        return this;
    }

    public GuiBuilder openSound(Sound sound) {
        return openSound(sound, 1.0f, 1.0f);
    }

    public GuiBuilder closeSound(Sound sound, float volume, float pitch) {
        this.closeSound  = sound;
        this.soundVolume = volume;
        this.soundPitch  = pitch;
        return this;
    }

    public GuiBuilder closeSound(Sound sound) {
        return closeSound(sound, 1.0f, 1.0f);
    }

    // Slots

    public GuiBuilder slot(int slot, ItemStack item) {
        slots.put(slot, item);
        return this;
    }

    public GuiBuilder slot(int slot, ItemStack item, Consumer<Player> onClick) {
        slots.put(slot, item);
        handlers.put(slot, onClick);
        return this;
    }

    public GuiBuilder slot(int slot, Consumer<Player> onClick) {
        handlers.put(slot, onClick);
        return this;
    }

    // Border

    public GuiBuilder border(ItemStack item) {
        int size = rows * 9;
        for (int i = 0; i < 9; i++) slots.put(i, item);
        for (int i = size - 9; i < size; i++) slots.put(i, item);
        for (int i = 0; i < size; i += 9) slots.put(i, item);
        for (int i = 8; i < size; i += 9) slots.put(i, item);
        return this;
    }

    // Build

    public ALBGui build() {
        return new ALBGui(title, rows, filler, openSound, closeSound,
                soundVolume, soundPitch,
                new HashMap<>(slots), new HashMap<>(handlers));
    }

    public void open(Player player) {
        build().open(player);
    }
}