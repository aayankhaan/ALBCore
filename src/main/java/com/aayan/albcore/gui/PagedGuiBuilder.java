package com.aayan.albcore.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class PagedGuiBuilder {

    private String title = "<dark_gray>Menu";
    private int rows = 6;
    private ItemStack filler = null;
    private ItemStack nextButton = null;
    private ItemStack prevButton = null;
    private int nextSlot = 53;
    private int prevSlot = 45;

    private final List<ItemStack> items = new ArrayList<>();
    private final List<Consumer<Player>> itemHandlers = new ArrayList<>();

    // Title

    public PagedGuiBuilder title(String title) {
        this.title = title;
        return this;
    }

    // Rows

    public PagedGuiBuilder rows(int rows) {
        this.rows = Math.max(2, Math.min(6, rows));
        return this;
    }

    // Filler

    public PagedGuiBuilder filler(ItemStack item) {
        this.filler = item;
        return this;
    }

    // Navigation buttons

    public PagedGuiBuilder nextButton(ItemStack item, int slot) {
        this.nextButton = item;
        this.nextSlot   = slot;
        return this;
    }

    public PagedGuiBuilder prevButton(ItemStack item, int slot) {
        this.prevButton = item;
        this.prevSlot   = slot;
        return this;
    }

    // Add items to pages

    public PagedGuiBuilder addItem(ItemStack item, Consumer<Player> onClick) {
        items.add(item);
        itemHandlers.add(onClick);
        return this;
    }

    public PagedGuiBuilder addItem(ItemStack item) {
        return addItem(item, p -> {});
    }

    // Open at page

    public void open(Player player, int page) {
        // last row reserved for navigation buttons
        int contentSlots = (rows - 1) * 9;
        int totalPages   = (int) Math.ceil((double) items.size() / contentSlots);
        int clampedPage  = Math.max(0, Math.min(page, Math.max(0, totalPages - 1)));
        int startIndex   = clampedPage * contentSlots;

        GuiBuilder builder = new GuiBuilder()
                .title(title + " <dark_gray>(" + (clampedPage + 1)
                        + "/" + Math.max(1, totalPages) + ")")
                .rows(rows)
                .filler(filler);

        // fill content area only (not last row)
        for (int i = 0; i < contentSlots; i++) {
            int itemIndex = startIndex + i;
            if (itemIndex >= items.size()) break;

            final int finalIndex = itemIndex;
            builder.slot(i, items.get(itemIndex),
                    p -> itemHandlers.get(finalIndex).accept(p));
        }

        // fill last row with filler
        if (filler != null) {
            int lastRowStart = (rows - 1) * 9;
            for (int i = lastRowStart; i < rows * 9; i++) {
                builder.slot(i, filler);
            }
        }

        // prev button
        if (clampedPage > 0 && prevButton != null) {
            final int prevPage = clampedPage - 1;
            builder.slot(prevSlot, prevButton, p -> open(p, prevPage));
        }

        // next button
        if (clampedPage < totalPages - 1 && nextButton != null) {
            final int nextPage = clampedPage + 1;
            builder.slot(nextSlot, nextButton, p -> open(p, nextPage));
        }

        builder.open(player);
    }

    public void open(Player player) {
        open(player, 0);
    }
}