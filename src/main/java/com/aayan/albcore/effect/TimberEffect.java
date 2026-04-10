package com.aayan.albcore.effect;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public final class TimberEffect {

    private static final Set<Material> LOGS = Set.of(
            Material.OAK_LOG,         Material.BIRCH_LOG,       Material.SPRUCE_LOG,
            Material.JUNGLE_LOG,      Material.ACACIA_LOG,      Material.DARK_OAK_LOG,
            Material.MANGROVE_LOG,    Material.CHERRY_LOG,      Material.BAMBOO_BLOCK,
            Material.OAK_WOOD,        Material.BIRCH_WOOD,      Material.SPRUCE_WOOD,
            Material.JUNGLE_WOOD,     Material.ACACIA_WOOD,     Material.DARK_OAK_WOOD,
            Material.MANGROVE_WOOD,   Material.CHERRY_WOOD,
            Material.STRIPPED_OAK_LOG,      Material.STRIPPED_BIRCH_LOG,
            Material.STRIPPED_SPRUCE_LOG,   Material.STRIPPED_JUNGLE_LOG,
            Material.STRIPPED_ACACIA_LOG,   Material.STRIPPED_DARK_OAK_LOG,
            Material.STRIPPED_MANGROVE_LOG, Material.STRIPPED_CHERRY_LOG
    );

    /**
     * Chop a whole connected tree starting from the broken block.
     * Uses BFS to collect all adjacent log blocks up to maxLogs.
     * Each log is broken naturally so it drops items as normal.
     *
     * @param player    the player who broke the first log
     * @param start     the block that was originally broken
     * @param maxLogs   maximum number of logs to chop (prevents lag on huge trees)
     */
    public void chop(Player player, Block start, int maxLogs) {
        if (!LOGS.contains(start.getType())) return;

        Set<Block> toBreak = new LinkedHashSet<>();
        Queue<Block> queue = new LinkedList<>();
        queue.add(start);

        while (!queue.isEmpty() && toBreak.size() < maxLogs) {
            Block current = queue.poll();
            if (!LOGS.contains(current.getType())) continue;
            if (!toBreak.add(current)) continue; // already visited

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = 0; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        Block neighbour = current.getRelative(dx, dy, dz);
                        if (LOGS.contains(neighbour.getType())
                                && !toBreak.contains(neighbour)) {
                            queue.add(neighbour);
                        }
                    }
                }
            }
        }

        for (Block log : toBreak) {
            log.breakNaturally(player.getInventory().getItemInMainHand());
        }
    }
}
