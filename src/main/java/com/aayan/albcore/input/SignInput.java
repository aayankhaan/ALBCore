package com.aayan.albcore.input;

import com.aayan.albcore.ALBCore;
import com.aayan.albcore.util.TextUtil;
import org.bukkit.entity.Player;
import java.util.function.Consumer;

public final class SignInput {

    private String[] defaultLines = new String[]{"", "^^^^^^^^^^^^^^^", "Enter your text", ""};
    private Consumer<String[]> handler;

    public SignInput lines(String... lines) {
        for (int i = 0; i < Math.min(lines.length, 4); i++) {
            this.defaultLines[i] = lines[i];
        }
        return this;
    }

    public SignInput onComplete(Consumer<String[]> handler) {
        this.handler = handler;
        return this;
    }

    public void open(Player player) {
        if (!ALBCore.api().protocolLib().isEnabled()) {
            TextUtil.send(player, "<red>Error: You must install ProtocolLib to use this feature.");
            return;
        }

        ProtocolSignManager.open(player);
    }
}
