package com.aayan.albcore.data;

import com.aayan.albcore.ALBCore;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which blocks were placed by players.
 * Uses an in-memory cache backed by chunk PDC for persistence across restarts.
 * <p>
 * Other systems can check {@link #isPlayerPlaced(Block)} to determine
 * if a block was placed by a player.
 */
public final class PlacedBlockTracker implements Listener {

    private static PlacedBlockTracker instance;

    private final NamespacedKey chunkKey;

    // in-memory cache: "world:x:y:z" -> true
    private final Set<String> cache = ConcurrentHashMap.newKeySet();

    public PlacedBlockTracker(ALBCore plugin) {
        instance = this;
        this.chunkKey = new NamespacedKey(plugin, "placed_blocks");
    }

    public static PlacedBlockTracker getInstance() {
        return instance;
    }

    // ── Events ────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        markPlaced(e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        unmark(e.getBlock());
    }

    // ── Public API ────────────────────────────────────────

    /**
     * Check if a block was placed by a player.
     */
    public boolean isPlayerPlaced(Block block) {
        if (block == null) return false;
        String key = toKey(block);

        // check memory cache first
        if (cache.contains(key)) return true;

        // fall back to chunk PDC (handles server restarts)
        if (isInChunkPDC(block)) {
            cache.add(key); // warm the cache
            return true;
        }

        return false;
    }

    /**
     * Manually mark a block as player-placed.
     */
    public void markPlaced(Block block) {
        if (block == null) return;
        String key = toKey(block);
        cache.add(key);
        addToChunkPDC(block);
    }

    /**
     * Remove the player-placed flag from a block.
     */
    public void unmark(Block block) {
        if (block == null) return;
        String key = toKey(block);
        cache.remove(key);
        removeFromChunkPDC(block);
    }

    // ── Internal: Chunk PDC storage ───────────────────────
    // We store placed block positions as a semicolon-separated string
    // of "x,y,z" coords in the chunk's PDC. This persists across restarts.

    private void addToChunkPDC(Block block) {
        try {
            Chunk chunk = block.getChunk();
            PersistentDataContainer pdc = chunk.getPersistentDataContainer();
            String existing = pdc.getOrDefault(chunkKey, PersistentDataType.STRING, "");

            String entry = blockToLocal(block);
            if (existing.contains(entry)) return; // already tracked

            String updated = existing.isEmpty() ? entry : existing + ";" + entry;
            pdc.set(chunkKey, PersistentDataType.STRING, updated);
        } catch (Exception e) {
            // silently fail — cache still works
        }
    }

    private void removeFromChunkPDC(Block block) {
        try {
            Chunk chunk = block.getChunk();
            PersistentDataContainer pdc = chunk.getPersistentDataContainer();
            String existing = pdc.getOrDefault(chunkKey, PersistentDataType.STRING, "");
            if (existing.isEmpty()) return;

            String entry = blockToLocal(block);
            // remove entry from the semicolon-separated list
            String updated = existing.replace(entry, "")
                    .replace(";;", ";")
                    .replaceAll("^;|;$", "");

            if (updated.isEmpty()) {
                pdc.remove(chunkKey);
            } else {
                pdc.set(chunkKey, PersistentDataType.STRING, updated);
            }
        } catch (Exception e) {
            // silently fail
        }
    }

    private boolean isInChunkPDC(Block block) {
        try {
            Chunk chunk = block.getChunk();
            PersistentDataContainer pdc = chunk.getPersistentDataContainer();
            String existing = pdc.getOrDefault(chunkKey, PersistentDataType.STRING, "");
            if (existing.isEmpty()) return false;
            return existing.contains(blockToLocal(block));
        } catch (Exception e) {
            return false;
        }
    }

    // ── Helpers ────────────────────────────────────────────

    private String toKey(Block block) {
        Location loc = block.getLocation();
        return loc.getWorld().getName() + ":" + loc.getBlockX()
                + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    // position relative within chunk for PDC storage (saves space)
    private String blockToLocal(Block block) {
        return block.getX() + "," + block.getY() + "," + block.getZ();
    }
}
