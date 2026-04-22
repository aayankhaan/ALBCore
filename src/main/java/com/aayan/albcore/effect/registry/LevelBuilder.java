package com.aayan.albcore.effect.registry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public final class LevelBuilder {

    private final int level;
    private double chance = 100.0;
    private long cooldownMs = 0;
    private final List<EffectAction> actions = new ArrayList<>();
    private final Map<String, Object> placeholders = new LinkedHashMap<>();

    private EffectDefinition.AttackCallback attackCallback;
    private EffectDefinition.DefendCallback defendCallback;
    private EffectDefinition.GenericCallback genericCallback;

    public LevelBuilder(int level) {
        this.level = level;
    }

    public int level() { return level; }

    // ── Core properties ──────────────────────────────

    public LevelBuilder chance(double chance) {
        this.chance = Math.max(0, Math.min(100, chance));
        return this;
    }

    public LevelBuilder cooldown(long milliseconds) {
        this.cooldownMs = Math.max(0, milliseconds);
        return this;
    }

    // ── Placeholders ─────────────────────────────────

    /**
     * Add a placeholder for this level. Referenced in displayName / description
     * as {name} — e.g. placeholder("damage", 10) → "{damage}" becomes "10".
     */
    public LevelBuilder placeholder(String name, Object value) {
        if (name != null && !name.isEmpty()) {
            placeholders.put(name, value);
        }
        return this;
    }

    public LevelBuilder placeholders(Map<String, Object> values) {
        if (values != null) placeholders.putAll(values);
        return this;
    }

    // ── Actions (for yml-style / config-driven effects) ──

    public LevelBuilder action(EffectAction action) {
        if (action != null) actions.add(action);
        return this;
    }

    public LevelBuilder action(String type, Map<String, Object> params) {
        actions.add(new EffectAction(type, params != null ? params : new LinkedHashMap<>()));
        return this;
    }

    // ── Callbacks (code-driven, per-level override) ──

    public LevelBuilder onAttack(EffectDefinition.AttackCallback callback) {
        this.attackCallback = callback;
        return this;
    }

    public LevelBuilder onDefend(EffectDefinition.DefendCallback callback) {
        this.defendCallback = callback;
        return this;
    }

    public LevelBuilder onGeneric(EffectDefinition.GenericCallback callback) {
        this.genericCallback = callback;
        return this;
    }

    // ── Build ────────────────────────────────────────

    LevelData build() {
        return new LevelData(
                level, chance, cooldownMs,
                actions, placeholders,
                attackCallback, defendCallback, genericCallback
        );
    }
}