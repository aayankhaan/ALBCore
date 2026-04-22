package com.aayan.albcore.effect.registry;

import com.aayan.albcore.ability.trigger.AbilityTrigger;
import com.aayan.albcore.condition.Condition;
import com.aayan.albcore.condition.ConditionSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class EffectBuilder {

    private final EffectRegistry registry;
    private final AbilityTrigger trigger;

    private String name;
    private String displayName = "<white>{name} {level}";
    private final List<String> description = new ArrayList<>();
    private ConditionSet conditions = ConditionSet.empty();
    private final Set<String> applicableItems = new HashSet<>();

    private final Map<Integer, LevelBuilder> levelBuilders = new LinkedHashMap<>();

    private EffectDefinition.AttackCallback sharedAttackCallback;
    private EffectDefinition.DefendCallback sharedDefendCallback;
    private EffectDefinition.GenericCallback sharedGenericCallback;

    public EffectBuilder(EffectRegistry registry, AbilityTrigger trigger) {
        this.registry = registry;
        this.trigger = trigger;
    }

    // ── Top-level metadata ───────────────────────────

    public EffectBuilder name(String name) {
        this.name = name.toLowerCase();
        return this;
    }

    public EffectBuilder displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public EffectBuilder description(String... lines) {
        this.description.addAll(Arrays.asList(lines));
        return this;
    }

    public EffectBuilder description(List<String> lines) {
        this.description.addAll(lines);
        return this;
    }

    public EffectBuilder conditions(ConditionSet conditions) {
        this.conditions = conditions;
        return this;
    }

    public EffectBuilder condition(Condition condition) {
        if (condition != null) this.conditions.add(condition);
        return this;
    }

    public EffectBuilder applicableTo(String... materials) {
        for (String m : materials) applicableItems.add(m.toUpperCase());
        return this;
    }

    public EffectBuilder applicableTo(Collection<String> materials) {
        for (String m : materials) applicableItems.add(m.toUpperCase());
        return this;
    }

    public EffectBuilder onAttack(EffectDefinition.AttackCallback callback) {
        this.sharedAttackCallback = callback;
        return this;
    }

    public EffectBuilder onDefend(EffectDefinition.DefendCallback callback) {
        this.sharedDefendCallback = callback;
        return this;
    }

    public EffectBuilder onGeneric(EffectDefinition.GenericCallback callback) {
        this.sharedGenericCallback = callback;
        return this;
    }

    // ── Levels ───────────────────────────────────────

    /**
     * Configure a specific level of this effect.
     */
    public EffectBuilder level(int level, Consumer<LevelBuilder> configurator) {
        if (level < 1) {
            throw new IllegalArgumentException("[ALBCore | Effects] Level must be >= 1 (got " + level + ")");
        }
        LevelBuilder lb = levelBuilders.computeIfAbsent(level, LevelBuilder::new);
        configurator.accept(lb);
        return this;
    }

    // ── Register ─────────────────────────────────────

    public EffectDefinition register() {
        if (name == null || name.isEmpty()) {
            throw new IllegalStateException("[ALBCore | Effects] Effect name is required!");
        }
        if (levelBuilders.isEmpty()) {
            throw new IllegalStateException("[ALBCore | Effects] Effect '" + name
                    + "' must define at least one level. Use .level(n, lvl -> ...)");
        }

        Map<Integer, LevelData> levels = new LinkedHashMap<>();
        for (Map.Entry<Integer, LevelBuilder> e : levelBuilders.entrySet()) {
            levels.put(e.getKey(), e.getValue().build());
        }

        EffectDefinition def = new EffectDefinition(
                name, trigger,
                displayName, description,
                conditions, applicableItems,
                levels,
                sharedAttackCallback, sharedDefendCallback, sharedGenericCallback
        );

        registry.registerEffect(def);
        return def;
    }
}