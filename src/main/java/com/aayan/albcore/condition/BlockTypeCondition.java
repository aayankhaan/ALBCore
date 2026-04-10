package com.aayan.albcore.condition;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class BlockTypeCondition implements Condition {

    private final Set<Material> materials;
    private final boolean whitelist;
    private org.bukkit.block.Block lastBlock;

    private BlockTypeCondition(Set<Material> materials, boolean whitelist) {
        this.materials = materials;
        this.whitelist = whitelist;
    }

    // set the block before evaluating
    public BlockTypeCondition withBlock(org.bukkit.block.Block block) {
        this.lastBlock = block;
        return this;
    }

    @Override
    public boolean evaluate(Player player) {
        if (lastBlock == null) return false;
        boolean contains = materials.contains(lastBlock.getType());
        return whitelist ? contains : !contains;
    }

    public static final class Builder {
        public BlockTypeCondition is(Material... materials) {
            return new BlockTypeCondition(
                    new HashSet<>(Arrays.asList(materials)), true);
        }
        public BlockTypeCondition isNot(Material... materials) {
            return new BlockTypeCondition(
                    new HashSet<>(Arrays.asList(materials)), false);
        }
    }
}