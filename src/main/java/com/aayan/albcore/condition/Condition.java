package com.aayan.albcore.condition;

import org.bukkit.entity.Player;

public interface Condition {

    boolean evaluate(Player player);

    static PlayerHealthCondition.Builder playerHealth() {
        return new PlayerHealthCondition.Builder();
    }

    static PlayerWorldCondition.Builder playerWorld() {
        return new PlayerWorldCondition.Builder();
    }

    static BlockTypeCondition.Builder blockType() { return new BlockTypeCondition.Builder();}

    static MobTypeCondition.Builder mobType() { return new MobTypeCondition.Builder(); }

    static PlayerPlacedCondition playerPlaced() { return PlayerPlacedCondition.isPlaced(); }
    static PlayerPlacedCondition notPlayerPlaced() { return PlayerPlacedCondition.isNotPlaced(); }
}