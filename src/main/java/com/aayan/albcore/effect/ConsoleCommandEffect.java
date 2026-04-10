package com.aayan.albcore.effect;

import com.aayan.albcore.ALBCore;
import org.bukkit.entity.Player;

public final class ConsoleCommandEffect {

    public void run(String command, Player player) {
        if (command == null || command.isBlank()) return;
        String parsed = command.replace("{player}", player.getName());
        ALBCore.getInstance().getServer().dispatchCommand(
                ALBCore.getInstance().getServer().getConsoleSender(), parsed);
    }

    public void run(String command) {
        if (command == null || command.isBlank()) return;
        ALBCore.getInstance().getServer().dispatchCommand(
                ALBCore.getInstance().getServer().getConsoleSender(), command);
    }
}