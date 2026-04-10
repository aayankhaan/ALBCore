package com.aayan.albcore.condition;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ConditionSet {

    private final List<Condition> conditions = new ArrayList<>();

    public ConditionSet add(Condition... conditions) {
        this.conditions.addAll(Arrays.asList(conditions));
        return this;
    }

    public boolean evaluate(Player player) {
        for (Condition condition : conditions) {
            if (!condition.evaluate(player)) return false;
        }
        return true;
    }

    public boolean isEmpty() {
        return conditions.isEmpty();
    }

    public static ConditionSet of(Condition... conditions) {
        return new ConditionSet().add(conditions);
    }

    public static ConditionSet empty() {
        return new ConditionSet();
    }
}