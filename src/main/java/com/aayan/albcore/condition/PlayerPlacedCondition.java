package com.aayan.albcore.condition;

import com.aayan.albcore.data.PlacedBlockTracker;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Condition that checks whether the target block was placed by a player.
 * <p>
 * Usage in enchant YAML:
 * <pre>
 * conditions:
 *   - "!player_placed"   # only fires if block was NOT player-placed
 *   - "player_placed"    # only fires if block WAS player-placed
 * </pre>
 * <p>
 * Must call {@link #withBlock(Block)} before evaluating.
 */
public final class PlayerPlacedCondition implements Condition {

    private final boolean inverted; // true = !player_placed (block must NOT be player placed)
    private Block block;

    public PlayerPlacedCondition(boolean inverted) {
        this.inverted = inverted;
    }

    /**
     * Set the block to check before evaluating.
     */
    public PlayerPlacedCondition withBlock(Block block) {
        this.block = block;
        return this;
    }

    @Override
    public boolean evaluate(Player player) {
        if (block == null) return true; // no block context — pass through
        PlacedBlockTracker tracker = PlacedBlockTracker.getInstance();
        if (tracker == null) return true; // tracker not initialized — pass through

        boolean placed = tracker.isPlayerPlaced(block);
        return inverted ? !placed : placed;
    }

    /**
     * @return condition that passes when block IS player-placed
     */
    public static PlayerPlacedCondition isPlaced() {
        return new PlayerPlacedCondition(false);
    }

    /**
     * @return condition that passes when block is NOT player-placed
     */
    public static PlayerPlacedCondition isNotPlaced() {
        return new PlayerPlacedCondition(true);
    }
}
