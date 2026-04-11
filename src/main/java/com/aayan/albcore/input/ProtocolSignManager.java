
// Yeh bhen ka lawda work nhi kr raha mkc ProtocolLib ki

package com.aayan.albcore.input;

import com.aayan.albcore.ALBCore;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public class ProtocolSignManager {

    private static final ProtocolManager manager = ProtocolLibrary.getProtocolManager();

    public static void open(Player player) {
        Location loc = player.getLocation().clone();
        loc.setY(player.getWorld().getMaxHeight() - 1);

        Bukkit.getScheduler().runTask(ALBCore.getInstance(), () -> {
            BlockData signData = Material.OAK_SIGN.createBlockData(bd ->
                    ((org.bukkit.block.data.type.Sign) bd).setRotation(org.bukkit.block.BlockFace.SOUTH)
            );
            player.sendBlockChange(loc, signData);

            try {
                org.bukkit.block.Sign virtualSign =
                        (org.bukkit.block.Sign) Material.OAK_SIGN.createBlockData().createBlockState();
                virtualSign.getSide(org.bukkit.block.sign.Side.FRONT).setLine(0, "hi");
                player.sendBlockUpdate(loc, virtualSign);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Open the editor
            Bukkit.getScheduler().runTaskLater(ALBCore.getInstance(), () -> {
                BlockPosition pos = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                PacketContainer openSign = manager.createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR);
                openSign.getBlockPositionModifier().write(0, pos);
                openSign.getBooleans().write(0, true);
                try {
                    manager.sendServerPacket(player, openSign);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 2L);
        });
    }
}