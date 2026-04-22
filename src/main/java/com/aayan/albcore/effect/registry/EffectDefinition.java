package com.aayan.albcore.effect.registry;

import com.aayan.albcore.ability.trigger.AbilityTrigger;
import com.aayan.albcore.condition.ConditionSet;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EffectDefinition {

    private final String id;
    private final AbilityTrigger trigger;
    private final String displayName;          // supports {level} + {placeholders}
    private final List<String> description;    // supports {level} + {placeholders}
    private final ConditionSet conditions;
    private final Set<String> applicableItems; // empty = any item

    private final Map<Integer, LevelData> levels;   // level number -> data
    private final int maxLevel;

    // Shared fallback callbacks (used when a level doesn't define its own)
    private final AttackCallback attackCallback;
    private final DefendCallback defendCallback;
    private final GenericCallback genericCallback;

    public EffectDefinition(String id,
                            AbilityTrigger trigger,
                            String displayName,
                            List<String> description,
                            ConditionSet conditions,
                            Set<String> applicableItems,
                            Map<Integer, LevelData> levels,
                            AttackCallback attackCallback,
                            DefendCallback defendCallback,
                            GenericCallback genericCallback) {
        this.id = id;
        this.trigger = trigger;
        this.displayName = displayName;
        this.description = description != null ? List.copyOf(description) : List.of();
        this.conditions = conditions != null ? conditions : ConditionSet.empty();
        this.applicableItems = applicableItems;

        // sort levels for deterministic iteration
        Map<Integer, LevelData> sorted = new LinkedHashMap<>();
        if (levels != null) {
            levels.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> sorted.put(e.getKey(), e.getValue()));
        }
        this.levels = Collections.unmodifiableMap(sorted);
        this.maxLevel = this.levels.isEmpty() ? 1 : this.levels.keySet().stream().max(Integer::compareTo).orElse(1);

        this.attackCallback = attackCallback;
        this.defendCallback = defendCallback;
        this.genericCallback = genericCallback;
    }

    // ── Getters ──────────────────────────────────────

    public String getId() { return id; }
    public AbilityTrigger getTrigger() { return trigger; }
    public int getMaxLevel() { return maxLevel; }
    public String getDisplayName() { return displayName; }
    public List<String> getDescription() { return description; }
    public ConditionSet getConditions() { return conditions; }
    public Set<String> getApplicableItems() { return applicableItems; }
    public Map<Integer, LevelData> getLevels() { return levels; }

    public AttackCallback getAttackCallback() { return attackCallback; }
    public DefendCallback getDefendCallback() { return defendCallback; }
    public GenericCallback getGenericCallback() { return genericCallback; }

    /**
     * Fetch the LevelData for a given level, clamping to [1..maxLevel].
     * Returns null if this effect has no levels at all.
     */
    public LevelData getLevel(int level) {
        if (levels.isEmpty()) return null;
        int clamped = Math.max(1, Math.min(level, maxLevel));
        LevelData data = levels.get(clamped);
        if (data != null) return data;

        // fall back to the highest defined level <= clamped
        LevelData fallback = null;
        for (Map.Entry<Integer, LevelData> e : levels.entrySet()) {
            if (e.getKey() <= clamped) fallback = e.getValue();
        }
        return fallback;
    }

    /**
     * True if this effect has at least one level-specific or shared code callback.
     */
    public boolean isCodeDriven() {
        if (attackCallback != null || defendCallback != null || genericCallback != null) return true;
        for (LevelData lvl : levels.values()) {
            if (lvl.hasCodeCallback()) return true;
        }
        return false;
    }

    /**
     * Resolve the effective attack callback for a given level:
     * per-level override wins over the shared fallback.
     */
    public AttackCallback resolveAttackCallback(int level) {
        LevelData data = getLevel(level);
        if (data != null && data.getAttackCallback() != null) return data.getAttackCallback();
        return attackCallback;
    }

    public DefendCallback resolveDefendCallback(int level) {
        LevelData data = getLevel(level);
        if (data != null && data.getDefendCallback() != null) return data.getDefendCallback();
        return defendCallback;
    }

    public GenericCallback resolveGenericCallback(int level) {
        LevelData data = getLevel(level);
        if (data != null && data.getGenericCallback() != null) return data.getGenericCallback();
        return genericCallback;
    }

    // ── Formatting ──────────────────────────────────

    /**
     * Format the display name for a level: substitutes {level} (roman)
     * and all placeholders defined for that level.
     */
    public String formatDisplayName(int level) {
        return applyPlaceholders(displayName, level);
    }

    /**
     * Format description lines for a level: substitutes {level} and placeholders.
     */
    public List<String> formatDescription(int level) {
        return description.stream()
                .map(line -> applyPlaceholders(line, level))
                .toList();
    }

    private String applyPlaceholders(String template, int level) {
        if (template == null) return "";
        String out = template.replace("{level}", toRoman(level));

        LevelData data = getLevel(level);
        if (data != null) {
            for (Map.Entry<String, Object> entry : data.getPlaceholders().entrySet()) {
                out = out.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
            }
        }
        return out;
    }

    // ── Helpers ──────────────────────────────────────

    /**
     * Check if this effect can be applied to the given material.
     */
    public boolean canApplyTo(String materialName) {
        if (applicableItems == null || applicableItems.isEmpty()) return true;
        return applicableItems.contains(materialName.toUpperCase());
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