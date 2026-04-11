package com.aayan.albtest;

import com.aayan.albcore.ALBCore;
import com.aayan.albcore.condition.Condition;
import com.aayan.albcore.gui.GuiBuilder;
import com.aayan.albcore.gui.PagedGuiBuilder;
import com.aayan.albcore.item.ItemBuilder;
import com.aayan.albcore.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class ALBTest extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        registerVoidSword();
        getLogger().info("ALBTest enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ALBTest disabled!");
    }

    private void registerVoidSword() {
        new ItemBuilder(Material.NETHERITE_SWORD)
                .name("<dark_purple>Void Sword")
                .lore("<gray>Forged in the void.", "", "%rarity%")
                .glow()
                .unbreakable(true)
                .mending()
                .hideAllAttributes()
                .unbreaking(3)
                .rarity("EPIC")
                .condition(Condition.playerHealth().greaterThan(10))
                .onSneak(3000L, "<gray>Cooldown: <white>{remaining}", player ->
                        ALBCore.api().message()
                                .send(player, "<dark_purple>The void whispers..."))
                .buildAndRegister("albtest:void_sword");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
                             String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Players only!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(p);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "voidsword" -> {
                ALBCore.api().registry().get("albtest:void_sword")
                        .ifPresent(item -> p.getInventory().addItem(item));
                TextUtil.send(p, "<green>Void Sword given!");
            }
            case "gui" -> openTestGui(p);
            case "paged" -> openPagedGui(p);
            case "anvil" -> openAnvilTest(p);
            case "sign" -> openSignTest(p);
            default -> sendHelp(p);
        }

        return true;
    }

    private void sendHelp(Player p) {
        TextUtil.send(p, "<gray>--- <white>ALBTest <gray>---");
        TextUtil.send(p, "  <aqua>/albtest voidsword <dark_gray>— get void sword");
        TextUtil.send(p, "  <aqua>/albtest gui <dark_gray>— open test gui");
        TextUtil.send(p, "  <aqua>/albtest paged <dark_gray>— open paged gui");
        TextUtil.send(p, "  <aqua>/albtest anvil <dark_gray>— test anvil input");
        TextUtil.send(p, "  <aqua>/albtest sign <dark_gray>— test sign input");
    }

    private void openAnvilTest(Player p) {
        ALBCore.api().anvilInput()
                .title("Rename me!")
                .text("Type here...")
                .onComplete(text -> {
                    TextUtil.send(p, "<green>Anvil Input Success!");
                    TextUtil.send(p, "<gray>You entered: <white>" + text);
                    ALBCore.api().sound().play(p, Sound.ENTITY_VILLAGER_YES);
                })
                .open(p);
    }

    private void openSignTest(Player p) {
        ALBCore.api().signInput()
                .lines("", "^^^^^^^^^^^^^", "Enter Text", "")
                .onComplete(lines -> {
                    String result = lines[0];
                    TextUtil.send(p, "<green>Sign Input Success!");
                    TextUtil.send(p, "<gray>Line 1: <white>" + result);
                    ALBCore.api().sound().play(p, Sound.ENTITY_VILLAGER_YES);
                })
                .open(p);
    }

    private void openTestGui(Player p) {
        new GuiBuilder()
                .title("<dark_purple>Test Menu")
                .rows(3)
                .filler(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                        .name(" ")
                        .build())
                .border(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                        .name(" ")
                        .build())
                .openSound(Sound.BLOCK_CHEST_OPEN)
                .closeSound(Sound.BLOCK_CHEST_CLOSE)
                .slot(11, new ItemBuilder(Material.NETHER_STAR)
                                .name("<gold>Message Test")
                                .lore(
                                        "<gray>Click to receive a message.",
                                        "",
                                        "%rarity%"
                                )
                                .rarity("LEGENDARY")
                                .build(),
                        player -> {
                            player.closeInventory();
                            ALBCore.api().message()
                                    .send(player, "<gold>✦ You clicked the star!");
                            ALBCore.api().sound()
                                    .play(player, Sound.ENTITY_PLAYER_LEVELUP);
                        })
                .slot(13, new ItemBuilder(Material.ENDER_PEARL)
                                .name("<dark_aqua>Sound Test")
                                .lore(
                                        "<gray>Click to hear a sound.",
                                        "",
                                        "%rarity%"
                                )
                                .rarity("RARE")
                                .build(),
                        player -> ALBCore.api().sound()
                                .play(player, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f))
                .slot(15, new ItemBuilder(Material.COMMAND_BLOCK)
                                .name("<red>Command Test")
                                .lore(
                                        "<gray>Click to run console command.",
                                        "",
                                        "%rarity%"
                                )
                                .rarity("EPIC")
                                .build(),
                        player -> {
                            player.closeInventory();
                            ALBCore.api().consoleCmd()
                                    .run("say {player} clicked the command button!", player);
                            ALBCore.api().message()
                                    .send(player, "<red>Console command fired!");
                        })
                .open(p);
    }

    private void openPagedGui(Player p) {
        PagedGuiBuilder paged = new PagedGuiBuilder()
                .title("<dark_purple>Paged Menu")
                .rows(6)
                .filler(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                        .name(" ")
                        .build())
                .nextButton(new ItemBuilder(Material.ARROW)
                        .name("<green>Next Page →")
                        .lore("<gray>Click to go to next page")
                        .build(), 53)
                .prevButton(new ItemBuilder(Material.ARROW)
                        .name("<red>← Previous Page")
                        .lore("<gray>Click to go back")
                        .build(), 45);

        String[] rarities = {"COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC"};
        Material[] materials = {
                Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD,
                Material.DIAMOND_SWORD, Material.NETHERITE_SWORD, Material.BLAZE_ROD,
                Material.NETHER_STAR, Material.ENDER_PEARL, Material.DRAGON_EGG,
                Material.BEACON, Material.TOTEM_OF_UNDYING, Material.ELYTRA
        };

        for (int i = 0; i < 50; i++) {
            String rarity = rarities[i % rarities.length];
            Material mat  = materials[i % materials.length];
            int index     = i + 1;

            paged.addItem(
                    new ItemBuilder(mat)
                            .name("<white>Test Item " + index)
                            .lore("<gray>Item number " + index, "", "%rarity%")
                            .rarity(rarity)
                            .build(),
                    player -> {
                        player.closeInventory();
                        ALBCore.api().message()
                                .send(player, "<green>You selected item " + index + "!");
                    });
        }

        paged.open(p);
    }
}