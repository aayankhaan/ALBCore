package com.aayan.albcore.input;

import com.aayan.albcore.ALBCore;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ProtocolSignManager {

    private static final ProtocolManager manager = ProtocolLibrary.getProtocolManager();
    private static final Map<UUID, Consumer<String[]>> handlers = new HashMap<>();

    public static void init() {
        manager.addPacketListener(new PacketAdapter(
                ALBCore.getInstance(),
                ListenerPriority.NORMAL,
                PacketType.Play.Client.UPDATE_SIGN
        ) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                Consumer<String[]> handler = handlers.remove(player.getUniqueId());

                if (handler == null) return;

                String[] lines = event.getPacket().getStringArrays().read(0);
                handler.accept(lines);
            }
        });
    }

    public static void open(Player player, String[] defaultLines, Consumer<String[]> callback) {
        handlers.put(player.getUniqueId(), callback);

        Location loc = player.getLocation().clone().add(0, 255, 0); // out of view

        BlockPosition pos = new BlockPosition(loc.toVector());

        // Fake sign block
        PacketContainer blockChange = manager.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
        blockChange.getBlockPositionModifier().write(0, pos);
        blockChange.getBlockData().write(
                0,
                WrappedBlockData.createData(Material.OAK_SIGN)
        );

        // Open editor
        PacketContainer openSign = manager.createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR);
        openSign.getBlockPositionModifier().write(0, pos);

        try {
            manager.sendServerPacket(player, blockChange);
            manager.sendServerPacket(player, openSign);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}