package com.aayan.albcore.condition;

import org.bukkit.entity.Player;

public final class PlayerWorldCondition implements Condition {

    private final Operator operator;
    private final String worldName;

    private PlayerWorldCondition(Operator operator, String worldName) {
        this.operator  = operator;
        this.worldName = worldName;
    }

    @Override
    public boolean evaluate(Player player) {
        return operator.evaluate(player.getWorld().getName(), worldName);
    }

    public static final class Builder {

        public PlayerWorldCondition equals(String worldName) {
            return new PlayerWorldCondition(Operator.EQUALS, worldName);
        }

        public PlayerWorldCondition notEquals(String worldName) {
            return new PlayerWorldCondition(Operator.NOT_EQUALS, worldName);
        }
    }
}