package com.aayan.albcore.effect.registry;

import com.aayan.albcore.ability.trigger.AbilityTrigger;
import com.aayan.albcore.condition.ConditionSet;

import java.util.List;
import java.util.Set;

/**
 * Immutable definition of a registered effect.
 * Created either from yml config or via the fluent builder API.
 */
public final class EffectDefinition {

    private final String id;
    private final AbilityTrigger trigger;
    private final int maxLevel;
    private final long cooldownMs;
    private final double chance; // 0-100, 100 = always
    private final String displayName;   // supports {level}
    private final List<String> description; // supports {level}
    private final ConditionSet conditions;
    private final List<EffectAction> actions;
    private final Set<String> applicableItems; // empty = any item

    // Code-driven callbacks (null if config-driven)
    private final AttackCallback attackCallback;
    private final DefendCallback defendCallback;
    private final GenericCallback genericCallback;

    public EffectDefinition(String id, AbilityTrigger trigger, int maxLevel,
                            long cooldownMs, double chance,
                            String displayName, List<String> description,
                            ConditionSet conditions, List<EffectAction> actions,
                            Set<String> applicableItems,
                            AttackCallback attackCallback,
                            DefendCallback defendCallback,
                            GenericCallback genericCallback) {
        this.id = id;
        this.trigger = trigger;
        this.maxLevel = maxLevel;
        this.cooldownMs = cooldownMs;
        this.chance = chance;
        this.displayName = displayName;
        this.description = description;
        this.conditions = conditions;
        this.actions = actions;
        this.applicableItems = applicableItems;
        this.attackCallback = attackCallback;
        this.defendCallback = defendCallback;
        this.genericCallback = genericCallback;
    }

    // Getters
    public String getId() { return id; }
    public AbilityTrigger getTrigger() { return trigger; }
    public int getMaxLevel() { return maxLevel; }
    public long getCooldownMs() { return cooldownMs; }
    public double getChance() { return chance; }
    public String getDisplayName() { return displayName; }
    public List<String> getDescription() { return description; }
    public ConditionSet getConditions() { return conditions; }
    public List<EffectAction> getActions() { return actions; }
    public Set<String> getApplicableItems() { return applicableItems; }
    public AttackCallback getAttackCallback() { return attackCallback; }
    public DefendCallback getDefendCallback() { return defendCallback; }
    public GenericCallback getGenericCallback() { return genericCallback; }

    public boolean isCodeDriven() {
        return attackCallback != null || defendCallback != null || genericCallback != null;
    }

    /**
     * Format the display name with level.
     */
    public String formatDisplayName(int level) {
        return displayName.replace("{level}", toRoman(level));
    }

    /**
     * Format description lines with level.
     */
    public List<String> formatDescription(int level) {
        return description.stream()
                .map(line -> line.replace("{level}", String.valueOf(level)))
                .toList();
    }

    /**
     * Check if this effect can be applied to the given material.
     */
    public boolean canApplyTo(String materialName) {
        if (applicableItems == null || applicableItems.isEmpty()) return true;
        return applicableItems.contains(materialName.toUpperCase());
    }

    /**
     * Roll chance check.
     */
    public boolean rollChance() {
        if (chance >= 100.0) return true;
        return Math.random() * 100.0 < chance;
    }

    private String toRoman(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(level);
        };
    }

    // ── Callback interfaces ──────────────────────────

    @FunctionalInterface
    public interface AttackCallback {
        void execute(org.bukkit.entity.Player player, org.bukkit.entity.Entity target, int level);
    }

    @FunctionalInterface
    public interface DefendCallback {
        void execute(org.bukkit.entity.Player player, org.bukkit.entity.Entity attacker, int level);
    }

    @FunctionalInterface
    public interface GenericCallback {
        void execute(org.bukkit.entity.Player player, int level);
    }
}