package com.aayan.albcore.effect.registry;

import com.aayan.albcore.ALBCore;
import com.aayan.albcore.ability.trigger.AbilityTrigger;
import com.aayan.albcore.condition.Condition;
import com.aayan.albcore.condition.ConditionSet;
import com.aayan.albcore.condition.MobTypeCondition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Central registry for all registered effects.
 * Effects are loaded from ALBCore/effects/*.yml and can also be registered via code.
 * Any plugin can then apply/remove effects on items using the API.
 */
public final class EffectRegistry {

    private static final Logger LOG = Logger.getLogger("ALBCore");
    private final ALBCore plugin;

    // effect id -> definition
    private final Map<String, EffectDefinition> effects = new ConcurrentHashMap<>();

    // tracks which effect ids came from yml files (so reload only clears those)
    private final Set<String> ymlSourced = ConcurrentHashMap.newKeySet();

    public EffectRegistry(ALBCore plugin) {
        this.plugin = plugin;
    }

    // ── Load from config ─────────────────────────────

    /**
     * Load all effect yml files from plugins/ALBCore/effects/.
     * On first load this is called once from ALBCoreAPI.
     * For a runtime reload (e.g. /albcore reload) use {@link #reload()} instead —
     * it preserves code-registered effects.
     */
    public void loadAll() {
        effects.clear();
        ymlSourced.clear();

        File effectsDir = new File(plugin.getDataFolder(), "effects");
        if (!effectsDir.exists()) {
            effectsDir.mkdirs();
            return;
        }

        File[] files = effectsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            loadFromFile(file);
        }

        LOG.info("[ALBCore] Loaded " + effects.size() + " effects from config.");
    }

    /**
     * Reload yml-sourced effects only. Code-registered effects from other
     * plugins are preserved. Safe to call from /albcore reload at runtime.
     */
    public void reload() {
        // remove only yml-sourced entries
        for (String id : ymlSourced) {
            effects.remove(id);
        }
        ymlSourced.clear();

        File effectsDir = new File(plugin.getDataFolder(), "effects");
        if (!effectsDir.exists()) {
            effectsDir.mkdirs();
            LOG.info("[ALBCore] Effects folder created. No effects to reload.");
            return;
        }

        File[] files = effectsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            LOG.info("[ALBCore] Effects folder is empty. Cleared yml-sourced effects.");
            return;
        }

        for (File file : files) {
            loadFromFile(file);
        }

        LOG.info("[ALBCore] Reloaded " + ymlSourced.size()
                + " yml effects (" + effects.size() + " total including code-registered).");
    }

    private void loadFromFile(File file) {
        String id = file.getName().replace(".yml", "").toLowerCase();

        try {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

            // Parse trigger
            String triggerStr = cfg.getString("trigger", "ON_ATTACK");
            AbilityTrigger trigger;
            try {
                trigger = AbilityTrigger.valueOf(triggerStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                LOG.warning("[ALBCore | Effects] Invalid trigger '" + triggerStr + "' in " + file.getName());
                return;
            }

            int maxLevel = cfg.getInt("max-level", 1);
            long cooldown = cfg.getLong("cooldown", 0);
            double chance = cfg.getDouble("chance", 100.0);
            String displayName = cfg.getString("display-name", "<white>" + id + " {level}");
            List<String> description = cfg.getStringList("description");
            if (description.isEmpty()) {
                description = cfg.getStringList("lore"); // alias
            }

            // Parse conditions
            ConditionSet conditions = ConditionSet.empty();
            ConfigurationSection condSection = cfg.getConfigurationSection("conditions");
            if (condSection != null) {
                // Mob type condition
                List<String> mobTypes = condSection.getStringList("mob-type");
                if (!mobTypes.isEmpty()) {
                    conditions.add(Condition.mobType().is(mobTypes));
                }

                // World condition
                String world = condSection.getString("world");
                if (world != null) {
                    conditions.add(Condition.playerWorld().equals(world));
                }
            }

            // Parse actions
            List<EffectAction> actions = new ArrayList<>();
            List<Map<?, ?>> actionsList = cfg.getMapList("actions");
            for (Map<?, ?> actionMap : actionsList) {
                String type = String.valueOf(actionMap.get("type"));
                Map<String, Object> params = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : actionMap.entrySet()) {
                    String key = String.valueOf(entry.getKey());
                    if (!key.equals("type")) {
                        params.put(key, entry.getValue());
                    }
                }
                actions.add(new EffectAction(type, params));
            }

            // Parse applicable items
            Set<String> applicableItems = new HashSet<>();
            List<String> itemList = cfg.getStringList("applicable-items");
            for (String item : itemList) {
                applicableItems.add(item.toUpperCase());
            }

            EffectDefinition def = new EffectDefinition(
                    id, trigger, maxLevel, cooldown, chance,
                    displayName, description, conditions, actions,
                    applicableItems,
                    null, null, null // no code callbacks for config-driven
            );

            if (registerEffect(def)) {
                ymlSourced.add(def.getId());
            }

        } catch (Exception e) {
            LOG.warning("[ALBCore | Effects] Failed to load effect: " + file.getName() + " — " + e.getMessage());
        }
    }

    // ── Register ─────────────────────────────────────

    /**
     * Register an effect definition. Rejects duplicates.
     *
     * @return true if the effect was registered, false if a duplicate was rejected.
     */
    public boolean registerEffect(EffectDefinition def) {
        if (effects.containsKey(def.getId())) {
            LOG.severe("[ALBCore | Effects] Duplicate effect ID '" + def.getId()
                    + "'! Already registered. Change the ID to avoid conflicts.");
            return false;
        }
        effects.put(def.getId(), def);
        LOG.info("[ALBCore | Effects] Registered effect: " + def.getId()
                + " (trigger=" + def.getTrigger() + ", maxLevel=" + def.getMaxLevel() + ")");
        return true;
    }

    // ── Query ────────────────────────────────────────

    public EffectDefinition getEffect(String id) {
        return effects.get(id.toLowerCase());
    }

    public boolean hasEffect(String id) {
        return effects.containsKey(id.toLowerCase());
    }

    public Collection<EffectDefinition> getAllEffects() {
        return Collections.unmodifiableCollection(effects.values());
    }

    public Map<String, EffectDefinition> getEffectMap() {
        return Collections.unmodifiableMap(effects);
    }

    // ── Fluent API ───────────────────────────────────

    /**
     * Start building a new effect via code.
     */
    public EffectBuilder register(AbilityTrigger trigger) {
        return new EffectBuilder(this, trigger);
    }

    /**
     * Create an applier to add/remove effects on an item.
     */
    public EffectApplier applyTo(ItemStack item) {
        return new EffectApplier(this, item);
    }

    /**
     * Shortcut: apply a single effect to an item. No lore added.
     */
    public ItemStack apply(ItemStack item, String effectId, int level) {
        return applyTo(item).addEffect(effectId, level).apply();
    }

    /**
     * Shortcut: apply effect with custom displayName and lore on the item.
     * Both displayName and lore are optional — pass null to use defaults from the effect definition.
     */
    public ItemStack apply(ItemStack item, String effectId, int level, String displayName, java.util.List<String> lore) {
        EffectDefinition def = getEffect(effectId.toLowerCase());
        if (def == null) return applyTo(item).addEffect(effectId, level).apply();

        int clampedLevel = Math.max(1, Math.min(level, def.getMaxLevel()));

        // Resolve displayName: custom > default from definition
        String resolvedName = (displayName != null)
                ? displayName.replace("{level}", toRoman(clampedLevel))
                : def.formatDisplayName(clampedLevel);

        // Resolve lore: custom > default from definition
        java.util.List<String> resolvedLore = (lore != null)
                ? lore.stream().map(l -> l.replace("{level}", String.valueOf(clampedLevel))).toList()
                : def.formatDescription(clampedLevel);

        return applyTo(item)
                .addEffectWithLore(effectId, level, resolvedName, resolvedLore)
                .apply();
    }

    /**
     * Shortcut: apply effect with default displayName and lore from effect definition.
     */
    public ItemStack applyWithLore(ItemStack item, String effectId, int level) {
        return applyTo(item).addEffectWithLore(effectId, level).apply();
    }

    /**
     * Shortcut: remove a single effect from an item.
     */
    public ItemStack remove(ItemStack item, String effectId) {
        return applyTo(item).removeEffect(effectId).apply();
    }

    /**
     * Get all effects currently on an item.
     */
    public Map<String, Integer> getItemEffects(ItemStack item) {
        return EffectApplier.loadEffects(item);
    }

    /**
     * Check if an item has a specific effect.
     */
    public boolean itemHasEffect(ItemStack item, String effectId) {
        return getItemEffects(item).containsKey(effectId.toLowerCase());
    }

    /**
     * Get the level of a specific effect on an item, or 0 if not present.
     */
    public int getItemEffectLevel(ItemStack item, String effectId) {
        return getItemEffects(item).getOrDefault(effectId.toLowerCase(), 0);
    }

    // ── Trigger firing ───────────────────────────────

    /**
     * Called by OnAttackTrigger — fires all matching effects on the held item.
     */
    public void fireAttackEffects(Player player, Entity target, ItemStack held) {
        Map<String, Integer> itemEffects = EffectApplier.loadEffects(held);
        if (itemEffects.isEmpty()) return;

        for (Map.Entry<String, Integer> entry : itemEffects.entrySet()) {
            EffectDefinition def = effects.get(entry.getKey());
            if (def == null || def.getTrigger() != AbilityTrigger.ON_ATTACK) continue;

            int level = entry.getValue();

            // Chance check
            if (!def.rollChance()) continue;

            // Cooldown check
            String cooldownKey = "effect:" + def.getId();
            if (def.getCooldownMs() > 0) {
                if (ALBCore.api().cooldowns().isOnCooldown(player.getUniqueId(), cooldownKey)) continue;
                ALBCore.api().cooldowns().set(player.getUniqueId(), cooldownKey, def.getCooldownMs());
            }

            // Inject mob target into conditions
            injectMobTarget(def.getConditions(), target);

            // Condition check
            if (!def.getConditions().evaluate(player)) continue;

            // Execute
            if (def.isCodeDriven() && def.getAttackCallback() != null) {
                def.getAttackCallback().execute(player, target, level);
            } else {
                for (EffectAction action : def.getActions()) {
                    EffectActionExecutor.executeAttack(action, player, target, level);
                }
            }
        }
    }

    /**
     * Called by OnDefendTrigger — fires all matching effects on the held item.
     */
    public void fireDefendEffects(Player player, Entity attacker, ItemStack held) {
        Map<String, Integer> itemEffects = EffectApplier.loadEffects(held);
        if (itemEffects.isEmpty()) return;

        for (Map.Entry<String, Integer> entry : itemEffects.entrySet()) {
            EffectDefinition def = effects.get(entry.getKey());
            if (def == null || def.getTrigger() != AbilityTrigger.ON_DEFEND) continue;

            int level = entry.getValue();
            if (!def.rollChance()) continue;

            String cooldownKey = "effect:" + def.getId();
            if (def.getCooldownMs() > 0) {
                if (ALBCore.api().cooldowns().isOnCooldown(player.getUniqueId(), cooldownKey)) continue;
                ALBCore.api().cooldowns().set(player.getUniqueId(), cooldownKey, def.getCooldownMs());
            }

            injectMobTarget(def.getConditions(), attacker);
            if (!def.getConditions().evaluate(player)) continue;

            if (def.isCodeDriven() && def.getDefendCallback() != null) {
                def.getDefendCallback().execute(player, attacker, level);
            } else {
                for (EffectAction action : def.getActions()) {
                    EffectActionExecutor.executeDefend(action, player, attacker, level);
                }
            }
        }
    }

    /**
     * Called by generic triggers (ON_HOLD, ON_CLICK, ON_SNEAK, etc).
     */
    public void fireGenericEffects(Player player, ItemStack held, AbilityTrigger triggerType) {
        Map<String, Integer> itemEffects = EffectApplier.loadEffects(held);
        if (itemEffects.isEmpty()) return;

        for (Map.Entry<String, Integer> entry : itemEffects.entrySet()) {
            EffectDefinition def = effects.get(entry.getKey());
            if (def == null || def.getTrigger() != triggerType) continue;

            int level = entry.getValue();
            if (!def.rollChance()) continue;

            String cooldownKey = "effect:" + def.getId();
            if (def.getCooldownMs() > 0) {
                if (ALBCore.api().cooldowns().isOnCooldown(player.getUniqueId(), cooldownKey)) continue;
                ALBCore.api().cooldowns().set(player.getUniqueId(), cooldownKey, def.getCooldownMs());
            }

            if (!def.getConditions().evaluate(player)) continue;

            if (def.isCodeDriven() && def.getGenericCallback() != null) {
                def.getGenericCallback().execute(player, level);
            } else {
                for (EffectAction action : def.getActions()) {
                    EffectActionExecutor.executeGeneric(action, player, level);
                }
            }
        }
    }

    // ── Internal ─────────────────────────────────────

    private void injectMobTarget(ConditionSet conditions, Entity target) {
        if (conditions == null || conditions.isEmpty() || target == null) return;
        try {
            var field = ConditionSet.class.getDeclaredField("conditions");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            var list = (List<com.aayan.albcore.condition.Condition>) field.get(conditions);
            for (var condition : list) {
                if (condition instanceof MobTypeCondition mtc) {
                    mtc.withTarget(target);
                }
            }
        } catch (Exception ignored) {}
    }

    private String toRoman(int level) {
        return switch (level) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III";
            case 4 -> "IV"; case 5 -> "V"; case 6 -> "VI";
            case 7 -> "VII"; case 8 -> "VIII"; case 9 -> "IX";
            case 10 -> "X"; default -> String.valueOf(level);
        };
    }
}