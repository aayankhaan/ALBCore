package com.aayan.albcore.input;

import com.aayan.albcore.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

public final class AnvilInput {

    private String title = "Enter text";
    private String defaultText = "";
    private Material itemMaterial = Material.PAPER;
    private Consumer<String> handler;

    public AnvilInput title(String title) {
        this.title = title;
        return this;
    }

    public AnvilInput text(String defaultText) {
        this.defaultText = defaultText;
        return this;
    }

    public AnvilInput material(Material material) {
        this.itemMaterial = material;
        return this;
    }

    public AnvilInput onComplete(Consumer<String> handler) {
        this.handler = handler;
        return this;
    }

    public void open(Player player) {
        if (handler == null) return;

        ItemStack item = new ItemStack(itemMaterial);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(TextUtil.parse(defaultText));
            item.setItemMeta(meta);
        }

        player.openAnvil(player.getLocation(), true).getTopInventory().setItem(0, item);
        InputListener.registerAnvil(player, handler);
    }
}
