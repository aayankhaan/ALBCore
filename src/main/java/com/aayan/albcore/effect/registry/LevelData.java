package com.aayan.albcore.effect.registry;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LevelData {

    private final int level;
    private final double chance;          // 0-100
    private final long cooldownMs;
    private final List<EffectAction> actions;
    private final Map<String, Object> placeholders;

    // Optional per-level code callbacks (null if not set)
    private final EffectDefinition.AttackCallback attackCallback;
    private final EffectDefinition.DefendCallback defendCallback;
    private final EffectDefinition.GenericCallback genericCallback;

    public LevelData(int level,
                     double chance,
                     long cooldownMs,
                     List<EffectAction> actions,
                     Map<String, Object> placeholders,
                     EffectDefinition.AttackCallback attackCallback,
                     EffectDefinition.DefendCallback defendCallback,
                     EffectDefinition.GenericCallback genericCallback) {
        this.level = level;
        this.chance = Math.max(0, Math.min(100, chance));
        this.cooldownMs = Math.max(0, cooldownMs);
        this.actions = actions != null ? List.copyOf(actions) : List.of();
        this.placeholders = placeholders != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(placeholders))
                : Collections.emptyMap();
        this.attackCallback = attackCallback;
        this.defendCallback = defendCallback;
        this.genericCallback = genericCallback;
    }

    public int getLevel() { return level; }
    public double getChance() { return chance; }
    public long getCooldownMs() { return cooldownMs; }
    public List<EffectAction> getActions() { return actions; }
    public Map<String, Object> getPlaceholders() { return placeholders; }

    public EffectDefinition.AttackCallback getAttackCallback() { return attackCallback; }
    public EffectDefinition.DefendCallback getDefendCallback() { return defendCallback; }
    public EffectDefinition.GenericCallback getGenericCallback() { return genericCallback; }

    public boolean hasCodeCallback() {
        return attackCallback != null || defendCallback != null || genericCallback != null;
    }
 
    public boolean rollChance() {
        if (chance >= 100.0) return true;
        return Math.random() * 100.0 < chance;
    }
}