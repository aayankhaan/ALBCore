package com.aayan.albcore.effect;

import com.aayan.albcore.util.TextUtil;
import org.bukkit.entity.Player;

public final class MessageEffect {

    public void send(Player player, String message) {
        if (message == null || message.isBlank()) return;
        TextUtil.send(player, message);
    }
}