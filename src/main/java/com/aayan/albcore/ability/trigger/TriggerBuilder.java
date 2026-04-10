package com.aayan.albcore.ability.trigger;

import com.aayan.albcore.ALBCore;
import com.aayan.albcore.condition.ConditionSet;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class TriggerBuilder {

    private final AbilityTrigger trigger;
    private String itemId;          // null = wildcard (fires for any item)
    private long cooldownMs = 0L;
    private String cooldownKey;
    private String cooldownMessage = null;
    private ConditionSet conditions = ConditionSet.empty();

    private Consumer<Player>           action            = null;
    private BiConsumer<Player, Block>  breakAction       = null;
    private Consumer<Player>           fishAction        = null;
    private BiConsumer<Player, Block>  blockDamageAction = null;
    private BiConsumer<Player, org.bukkit.entity.Entity> attackAction = null;
    private BiConsumer<Player, org.bukkit.entity.Entity> defendAction = null;
    private Consumer<Player>           wearEquipAction   = null;
    private Consumer<Player>           wearUnequipAction = null;

    private TriggerBuilder(AbilityTrigger trigger) {
        this.trigger = trigger;
    }

    public static TriggerBuilder of(AbilityTrigger trigger) {
        return new TriggerBuilder(trigger);
    }

    /** Link this trigger to a specific registered item ID. Optional — omit for wildcard. */
    public TriggerBuilder forItem(String itemId) {
        this.itemId = itemId;
        this.cooldownKey = itemId + ":" + trigger.name().toLowerCase();
        return this;
    }

    /**
     * Set an explicit cooldown key. Required when using wildcard (no forItem) with a cooldown,
     * so each registered callback has a unique key.
     */
    public TriggerBuilder withCooldownKey(String key) {
        this.cooldownKey = key;
        return this;
    }

    public TriggerBuilder withCooldown(long ms) {
        this.cooldownMs = ms;
        return this;
    }

    public TriggerBuilder withCooldownMessage(String message) {
        this.cooldownMessage = message;
        return this;
    }

    public TriggerBuilder conditions(ConditionSet conditions) {
        this.conditions = conditions;
        return this;
    }

    public TriggerBuilder run(Consumer<Player> action) {
        this.action = action;
        return this;
    }

    public TriggerBuilder runOnBreak(BiConsumer<Player, Block> action) {
        this.breakAction = action;
        return this;
    }

    public TriggerBuilder runOnFish(Consumer<Player> action) {
        this.fishAction = action;
        return this;
    }

    public TriggerBuilder runOnBlockDamage(BiConsumer<Player, Block> action) {
        this.blockDamageAction = action;
        return this;
    }

    public TriggerBuilder runOnAttack(BiConsumer<Player, org.bukkit.entity.Entity> action) {
        this.attackAction = action;
        return this;
    }

    public TriggerBuilder runOnDefend(BiConsumer<Player, org.bukkit.entity.Entity> action) {
        this.defendAction = action;
        return this;
    }

    /**
     * Set equip and unequip actions for ON_WEAR trigger.
     *
     * @param onEquip   called once when armor is first detected as worn
     * @param onUnequip called once when armor is removed or player logs out
     */
    public TriggerBuilder runOnWear(Consumer<Player> onEquip, Consumer<Player> onUnequip) {
        this.wearEquipAction = onEquip;
        this.wearUnequipAction = onUnequip;
        return this;
    }

    public void register() {
        switch (trigger) {

            case ON_HOLD -> {
                if (action == null) { warn(); return; }
                ALBCore.api().onHold().addCallback(
                        itemId, cooldownMs, cooldownKey,
                        cooldownMessage, conditions, action);
            }

            case ON_SNEAK -> {
                if (action == null) { warn(); return; }
                ALBCore.api().onSneak().addCallback(
                        itemId, cooldownMs, cooldownKey,
                        cooldownMessage, conditions, action);
            }

            case ON_CLICK -> {
                if (action == null) { warn(); return; }
                ALBCore.api().onClick().addCallback(
                        itemId, cooldownMs, cooldownKey,
                        cooldownMessage, conditions, action);
            }

            case ON_BREAK -> {
                if (breakAction == null) { warn(); return; }
                ALBCore.api().onBreak().addCallback(
                        itemId, cooldownMs, cooldownKey,
                        cooldownMessage, conditions, breakAction);
            }

            case ON_FISH -> {
                if (fishAction == null) { warn(); return; }
                ALBCore.api().onFish().addCallback(
                        itemId, cooldownMs, cooldownKey,
                        cooldownMessage, conditions, fishAction);
            }

            case ON_BLOCK_DAMAGE -> {
                if (blockDamageAction == null) { warn(); return; }
                ALBCore.api().onBlockDamage().addCallback(
                        itemId, cooldownMs, cooldownKey,
                        cooldownMessage, conditions, blockDamageAction);
            }

            case ON_ATTACK -> {
                if (attackAction == null) { warn(); return; }
                ALBCore.api().onAttack().addCallback(
                        itemId, cooldownMs, cooldownKey,
                        cooldownMessage, conditions, attackAction);
            }

            case ON_DEFEND -> {
                if (defendAction == null) { warn(); return; }
                ALBCore.api().onDefend().addCallback(
                        itemId, cooldownMs, cooldownKey,
                        cooldownMessage, conditions, defendAction);
            }

            case ON_WEAR -> {
                if (wearEquipAction == null && wearUnequipAction == null) { warn(); return; }
                ALBCore.api().onWear().addCallback(
                        itemId, conditions, wearEquipAction, wearUnequipAction);
            }

            default -> ALBCore.getInstance().getLogger()
                    .warning("[ALBCore | TriggerBuilder] Trigger "
                            + trigger + " not yet wired.");
        }
    }

    private void warn() {
        ALBCore.getInstance().getLogger()
                .warning("[ALBCore | TriggerBuilder] No action set for "
                        + trigger + " — skipping.");
    }
}
