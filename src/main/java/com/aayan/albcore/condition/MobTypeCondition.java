package com.aayan.albcore.condition;

import com.aayan.albcore.hook.MythicMobsHook;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;

public final class MobTypeCondition implements Condition {

    private final Set<String> mobTypes;
    private final boolean whitelist;
    private Entity target;

    private MobTypeCondition(Set<String> mobTypes, boolean whitelist) {
        this.mobTypes = mobTypes;
        this.whitelist = whitelist;
    }

    public MobTypeCondition withTarget(Entity entity) {
        this.target = entity;
        return this;
    }

    @Override
    public boolean evaluate(Player player) {
        // If no target set, can't evaluate — pass through
        if (target == null) return true;
        return evaluate(target);
    }

    public boolean evaluate(Entity entity) {
        if (entity == null) return !whitelist;

        String vanillaType = entity.getType().name();

        String mythicType = null;
        if (MythicMobsHook.isEnabled()) {
            mythicType = MythicMobsHook.getMobType(entity).orElse(null);
        }

        boolean matches = false;
        for (String type : mobTypes) {
            if (type.equalsIgnoreCase(vanillaType)) {
                matches = true;
                break;
            }
            if (mythicType != null && type.equalsIgnoreCase(mythicType)) {
                matches = true;
                break;
            }
        }

        return whitelist ? matches : !matches;
    }

    public static final class Builder {

        public MobTypeCondition is(String... types) {
            return new MobTypeCondition(
                    new HashSet<>(Arrays.asList(types)), true);
        }

        public MobTypeCondition is(Collection<String> types) {
            return new MobTypeCondition(new HashSet<>(types), true);
        }

        public MobTypeCondition isNot(String... types) {
            return new MobTypeCondition(
                    new HashSet<>(Arrays.asList(types)), false);
        }

        public MobTypeCondition isNot(Collection<String> types) {
            return new MobTypeCondition(new HashSet<>(types), false);
        }
    }
}