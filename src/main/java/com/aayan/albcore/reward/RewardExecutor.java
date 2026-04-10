package com.aayan.albcore.reward;

import com.aayan.albcore.ALBCore;
import com.aayan.albcore.util.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class RewardExecutor {

    // Give item

    public void giveItem(Player player, ItemStack item) {
        if (item == null) return;
        player.getInventory().addItem(item.clone());
    }

    // Give money

    public boolean giveMoney(Player player, double amount) {
        if (!ALBCore.api().vault().isEnabled()) {
            ALBCore.getInstance().getLogger()
                    .warning("[ALBCore | RewardExecutor] Vault not enabled.");
            return false;
        }
        ALBCore.api().vault().give(player, amount);
        return true;
    }

    // Run console command

    public void runCommand(Player player, String command) {
        ALBCore.api().consoleCmd().run(command, player);
    }

    // Send message

    public void sendMessage(Player player, String message) {
        TextUtil.send(player, message);
    }

    // Give permission

    public void givePermission(Player player, String permission) {
        if (player.isOp()) return;
        ALBCore.getInstance().getServer().dispatchCommand(
                ALBCore.getInstance().getServer().getConsoleSender(),
                "lp user " + player.getName() + " permission set " + permission + " true");
    }
}