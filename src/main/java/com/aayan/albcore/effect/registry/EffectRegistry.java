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

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Central registry for all registered effects.
 * Effects are loaded from ALBCore/effects/*.yml and can also be registered via code.
 * Any plugin can then apply/remove effects on items using the API.
 *
 * Every effect is defined as a set of levels. Each level has its own
 * chance, cooldown, actions (or code callback), and placeholder values.
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

    public void reload() {
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

            // yml can override id but default to filename
            id = cfg.getString("id", id).toLowerCase();

            // Parse trigger
            String triggerStr = cfg.getString("trigger", "ON_ATTACK");
            AbilityTrigger trigger;
            try {
                trigger = AbilityTrigger.valueOf(triggerStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                LOG.warning("[ALBCore | Effects] Invalid trigger '" + triggerStr + "' in " + file.getName());
                return;
            }

            String displayName = cfg.getString("display-name", "<white>" + id + " {level}");
            List<String> description = cfg.getStringList("description");
            if (description.isEmpty()) {
                description = cfg.getStringList("lore"); // alias
            }

            // Parse top-level conditions (apply to every level)
            ConditionSet conditions = parseConditions(cfg.getConfigurationSection("conditions"));

            // Parse applicable items
            Set<String> applicableItems = new HashSet<>();
            for (String item : cfg.getStringList("applicable-items")) {
                applicableItems.add(item.toUpperCase());
            }

            // Parse levels
            Map<Integer, LevelData> levels = new LinkedHashMap<>();
            ConfigurationSection levelsSection = cfg.getConfigurationSection("levels");
            if (levelsSection == null) {
                LOG.warning("[ALBCore | Effects] Effect '" + id + "' in " + file.getName()
                        + " has no 'levels:' section. Skipping.");
                return;
            }

            for (String levelKey : levelsSection.getKeys(false)) {
                int lvlNum;
                try {
                    lvlNum = Integer.parseInt(levelKey);
                } catch (NumberFormatException e) {
                    LOG.warning("[ALBCore | Effects] Non-numeric level key '" + levelKey
                            + "' in " + file.getName() + " — skipping.");
                    continue;
                }

                ConfigurationSection lvlSec = levelsSection.getConfigurationSection(levelKey);
                if (lvlSec == null) continue;

                double chance = lvlSec.getDouble("chance", 100.0);
                long cooldown = lvlSec.getLong("cooldown", 0);

                // Actions
                List<EffectAction> actions = new ArrayList<>();
                List<Map<?, ?>> actionsList = lvlSec.getMapList("effects");
                if (actionsList.isEmpty()) {
                    actionsList = lvlSec.getMapList("actions"); // alias
                }
                for (Map<?, ?> actionMap : actionsList) {
                    Object typeObj = actionMap.get("type");
                    if (typeObj == null) continue;
                    Map<String, Object> params = new LinkedHashMap<>();
                    for (Map.Entry<?, ?> entry : actionMap.entrySet()) {
                        String key = String.valueOf(entry.getKey());
                        if (!key.equals("type")) {
                            params.put(key, entry.getValue());
                        }
                    }
                    actions.add(new EffectAction(String.valueOf(typeObj), params));
                }

                // Placeholders — supports both "placeholder" (singular) and "placeholders"
                Map<String, Object> placeholders = new LinkedHashMap<>();
                ConfigurationSection phSec = lvlSec.getConfigurationSection("placeholder");
                if (phSec == null) phSec = lvlSec.getConfigurationSection("placeholders");
                if (phSec != null) {
                    for (String phKey : phSec.getKeys(false)) {
                        placeholders.put(phKey, phSec.get(phKey));
                    }
                }

                levels.put(lvlNum, new LevelData(
                        lvlNum, chance, cooldown, actions, placeholders,
                        null, null, null // yml effects have no code callbacks
                ));
            }

            if (levels.isEmpty()) {
                LOG.warning("[ALBCore | Effects] Effect '" + id + "' has no valid levels. Skipping.");
                return;
            }

            EffectDefinition def = new EffectDefinition(
                    id, trigger, displayName, description,
                    conditions, applicableItems, levels,
                    null, null, null
            );

            if (registerEffect(def)) {
                ymlSourced.add(def.getId());
            }

        } catch (Exception e) {
            LOG.warning("[ALBCore | Effects] Failed to load effect: " + file.getName() + " — " + e.getMessage());
            e.printStackTrace();
        }
    }


    private ConditionSet parseConditions(ConfigurationSection condSection) {
        ConditionSet conditions = ConditionSet.empty();
        if (condSection == null) return conditions;


        List<String> mobTypes = condSection.getStringList("mob-type");
        if (!mobTypes.isEmpty()) {
            conditions.add(Condition.mobType().is(mobTypes));
        }

        String world = condSection.getString("world");
        if (world != null) {
            conditions.add(Condition.playerWorld().equals(world));
        }

        return conditions;
    }

    // ── Register ─────────────────────────────────────

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

    public EffectBuilder register(AbilityTrigger trigger) {
        return new EffectBuilder(this, trigger);
    }

    public EffectApplier applyTo(ItemStack item) {
        return new EffectApplier(this, item);
    }

    public ItemStack apply(ItemStack item, String effectId, int level) {
        return applyTo(item).addEffect(effectId, level).apply();
    }

    public ItemStack apply(ItemStack item, String effectId, int level, String displayName, List<String> lore) {
        EffectDefinition def = getEffect(effectId.toLowerCase());
        if (def == null) return applyTo(item).addEffect(effectId, level).apply();

        int clampedLevel = Math.max(1, Math.min(level, def.getMaxLevel()));

        String resolvedName = (displayName != null)
                ? substitutePlaceholders(def, displayName, clampedLevel)
                : def.formatDisplayName(clampedLevel);

        List<String> resolvedLore = (lore != null)
                ? lore.stream().map(l -> substitutePlaceholders(def, l, clampedLevel)).toList()
                : def.formatDescription(clampedLevel);

        return applyTo(item)
                .addEffectWithLore(effectId, level, resolvedName, resolvedLore)
                .apply();
    }

    public ItemStack applyWithLore(ItemStack item, String effectId, int level) {
        return applyTo(item).addEffectWithLore(effectId, level).apply();
    }

    public ItemStack remove(ItemStack item, String effectId) {
        return applyTo(item).removeEffect(effectId).apply();
    }

    public Map<String, Integer> getItemEffects(ItemStack item) {
        return EffectApplier.loadEffects(item);
    }

    public boolean itemHasEffect(ItemStack item, String effectId) {
        return getItemEffects(item).containsKey(effectId.toLowerCase());
    }

    public int getItemEffectLevel(ItemStack item, String effectId) {
        return getItemEffects(item).getOrDefault(effectId.toLowerCase(), 0);
    }

    // ── Trigger firing ───────────────────────────────

    public void fireAttackEffects(Player player, Entity target, ItemStack held, EntityDamageByEntityEvent event) {
        Map<String, Integer> itemEffects = EffectApplier.loadEffects(held);
        if (itemEffects.isEmpty()) return;

        for (Map.Entry<String, Integer> entry : itemEffects.entrySet()) {
            EffectDefinition def = effects.get(entry.getKey());
            if (def == null || def.getTrigger() != AbilityTrigger.ON_ATTACK) continue;

            int level = entry.getValue();
            LevelData lvlData = def.getLevel(level);
            if (lvlData == null) continue;

            if (!lvlData.rollChance()) continue;
            if (!checkAndSetCooldown(player, def, lvlData)) continue;

            injectMobTarget(def.getConditions(), target);
            if (!def.getConditions().evaluate(player)) continue;

            EffectDefinition.AttackCallback cb = def.resolveAttackCallback(level);
            if (cb != null) {
                cb.execute(player, target, level);
            } else {
                for (EffectAction action : lvlData.getActions()) {
                    EffectActionExecutor.executeAttack(action, player, target, level, event);
                }
            }
        }
    }

    public void fireDefendEffects(Player player, Entity attacker, ItemStack held, EntityDamageEvent event) {
        Map<String, Integer> itemEffects = EffectApplier.loadEffects(held);
        if (itemEffects.isEmpty()) return;

        for (Map.Entry<String, Integer> entry : itemEffects.entrySet()) {
            EffectDefinition def = effects.get(entry.getKey());
            if (def == null || def.getTrigger() != AbilityTrigger.ON_DEFEND) continue;

            int level = entry.getValue();
            LevelData lvlData = def.getLevel(level);
            if (lvlData == null) continue;

            if (!lvlData.rollChance()) continue;
            if (!checkAndSetCooldown(player, def, lvlData)) continue;

            injectMobTarget(def.getConditions(), attacker);
            if (!def.getConditions().evaluate(player)) continue;

            EffectDefinition.DefendCallback cb = def.resolveDefendCallback(level);
            if (cb != null) {
                cb.execute(player, attacker, level);
            } else {
                for (EffectAction action : lvlData.getActions()) {
                    EffectActionExecutor.executeDefend(action, player, attacker, level, event);
                }
            }
        }
    }

    public void fireGenericEffects(Player player, ItemStack held, AbilityTrigger triggerType) {
        Map<String, Integer> itemEffects = EffectApplier.loadEffects(held);
        if (itemEffects.isEmpty()) return;

        for (Map.Entry<String, Integer> entry : itemEffects.entrySet()) {
            EffectDefinition def = effects.get(entry.getKey());
            if (def == null || def.getTrigger() != triggerType) continue;

            int level = entry.getValue();
            LevelData lvlData = def.getLevel(level);
            if (lvlData == null) continue;

            if (!lvlData.rollChance()) continue;
            if (!checkAndSetCooldown(player, def, lvlData)) continue;

            if (!def.getConditions().evaluate(player)) continue;

            EffectDefinition.GenericCallback cb = def.resolveGenericCallback(level);
            if (cb != null) {
                cb.execute(player, level);
            } else {
                for (EffectAction action : lvlData.getActions()) {
                    EffectActionExecutor.executeGeneric(action, player, level);
                }
            }
        }
    }

    // ── Internal ─────────────────────────────────────

    private boolean checkAndSetCooldown(Player player, EffectDefinition def, LevelData lvlData) {
        long cd = lvlData.getCooldownMs();
        if (cd <= 0) return true;

        String cooldownKey = "effect:" + def.getId();
        if (ALBCore.api().cooldowns().isOnCooldown(player.getUniqueId(), cooldownKey)) return false;
        ALBCore.api().cooldowns().set(player.getUniqueId(), cooldownKey, cd);
        return true;
    }

    private void injectMobTarget(ConditionSet conditions, Entity target) {
        if (conditions == null || conditions.isEmpty() || target == null) return;
        try {
            var field = ConditionSet.class.getDeclaredField("conditions");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            var list = (List<Condition>) field.get(conditions);
            for (var condition : list) {
                if (condition instanceof MobTypeCondition mtc) {
                    mtc.withTarget(target);
                }
            }
        } catch (Exception ignored) {}
    }

    private String substitutePlaceholders(EffectDefinition def, String template, int level) {
        if (template == null) return "";
        String out = template.replace("{level}", toRoman(level));
        LevelData data = def.getLevel(level);
        if (data != null) {
            for (Map.Entry<String, Object> entry : data.getPlaceholders().entrySet()) {
                out = out.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
            }
        }
        return out;
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