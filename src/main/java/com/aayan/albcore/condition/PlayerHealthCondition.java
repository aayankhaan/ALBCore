package com.aayan.albcore.condition;

import org.bukkit.entity.Player;

public final class PlayerHealthCondition implements Condition {

    private final Operator operator;
    private final double value;

    private PlayerHealthCondition(Operator operator, double value) {
        this.operator = operator;
        this.value    = value;
    }

    @Override
    public boolean evaluate(Player player) {
        return operator.evaluate(player.getHealth(), value);
    }

    public static final class Builder {

        public PlayerHealthCondition equals(double value) {
            return new PlayerHealthCondition(Operator.EQUALS, value);
        }

        public PlayerHealthCondition notEquals(double value) {
            return new PlayerHealthCondition(Operator.NOT_EQUALS, value);
        }

        public PlayerHealthCondition greaterThan(double value) {
            return new PlayerHealthCondition(Operator.GREATER_THAN, value);
        }

        public PlayerHealthCondition lessThan(double value) {
            return new PlayerHealthCondition(Operator.LESS_THAN, value);
        }

        public PlayerHealthCondition greaterThanOrEqual(double value) {
            return new PlayerHealthCondition(Operator.GREATER_THAN_OR_EQUAL, value);
        }

        public PlayerHealthCondition lessThanOrEqual(double value) {
            return new PlayerHealthCondition(Operator.LESS_THAN_OR_EQUAL, value);
        }
    }
}