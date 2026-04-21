package com.aayan.albcore.effect.registry;

import com.aayan.albcore.ability.trigger.AbilityTrigger;
import com.aayan.albcore.condition.ConditionSet;

import java.util.*;

public final class EffectBuilder {

    private final EffectRegistry registry;
    private final AbilityTrigger trigger;

    private String name;
    private String displayName = "<white>{name} {level}";
    private List<String> description = new ArrayList<>();
    private int maxLevel = 1;
    private long cooldownMs = 0;
    private double chance = 100.0;
    private ConditionSet conditions = ConditionSet.empty();
    private Set<String> applicableItems = new HashSet<>();

    private EffectDefinition.AttackCallback attackCallback;
    private EffectDefinition.DefendCallback defendCallback;
    private EffectDefinition.GenericCallback genericCallback;

    public EffectBuilder(EffectRegistry registry, AbilityTrigger trigger) {
        this.registry = registry;
        this.trigger = trigger;
    }

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

    public EffectBuilder maxLevel(int maxLevel) {
        this.maxLevel = Math.max(1, maxLevel);
        return this;
    }

    public EffectBuilder cooldown(long ms) {
        this.cooldownMs = ms;
        return this;
    }

    public EffectBuilder chance(double chance) {
        this.chance = Math.max(0, Math.min(100, chance));
        return this;
    }

    public EffectBuilder conditions(ConditionSet conditions) {
        this.conditions = conditions;
        return this;
    }

    /**
     * Restrict which item types this effect can be applied to.
     * Empty = any item.
     */
    public EffectBuilder applicableTo(String... materials) {
        for (String m : materials) applicableItems.add(m.toUpperCase());
        return this;
    }

    public EffectBuilder applicableTo(Collection<String> materials) {
        for (String m : materials) applicableItems.add(m.toUpperCase());
        return this;
    }

    // ── Callbacks ────────────────────────────────────

    public EffectBuilder onAttack(EffectDefinition.AttackCallback callback) {
        this.attackCallback = callback;
        return this;
    }

    public EffectBuilder onDefend(EffectDefinition.DefendCallback callback) {
        this.defendCallback = callback;
        return this;
    }

    public EffectBuilder onGeneric(EffectDefinition.GenericCallback callback) {
        this.genericCallback = callback;
        return this;
    }

    // ── Register ─────────────────────────────────────

    public EffectDefinition register() {
        if (name == null || name.isEmpty()) {
            throw new IllegalStateException("[ALBCore | Effects] Effect name is required!");
        }

        EffectDefinition def = new EffectDefinition(
                name, trigger, maxLevel, cooldownMs, chance,
                displayName, description, conditions,
                List.of(), // no yml actions for code-driven
                applicableItems,
                attackCallback, defendCallback, genericCallback
        );

        registry.registerEffect(def);
        return def;
    }
}