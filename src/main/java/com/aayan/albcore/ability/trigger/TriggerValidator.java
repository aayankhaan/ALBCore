package com.aayan.albcore.ability.trigger;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class TriggerValidator {

    private static final Map<AbilityTrigger, Set<TriggerContext>> VALID
            = new EnumMap<>(AbilityTrigger.class);

    static {
        Set<TriggerContext> selfOnly = EnumSet.of(TriggerContext.SELF);
        Set<TriggerContext> withTarget = EnumSet.of(
                TriggerContext.SELF,
                TriggerContext.TARGET,
                TriggerContext.SELF_AND_TARGET);
        Set<TriggerContext> withAttacker = EnumSet.of(
                TriggerContext.SELF,
                TriggerContext.ATTACKER,
                TriggerContext.SELF_AND_ATTACKER);

        VALID.put(AbilityTrigger.ON_HOLD,
                selfOnly);
        VALID.put(AbilityTrigger.ON_SNEAK, selfOnly);
        VALID.put(AbilityTrigger.ON_CLICK, selfOnly);
        VALID.put(AbilityTrigger.ON_BREAK, selfOnly);
        VALID.put(AbilityTrigger.ON_FISH, selfOnly);
        VALID.put(AbilityTrigger.ON_BLOCK_DAMAGE, selfOnly);
        VALID.put(AbilityTrigger.ON_ATTACK, withTarget);
        VALID.put(AbilityTrigger.ON_DEFEND, withAttacker);
        VALID.put(AbilityTrigger.ON_WEAR, selfOnly);
    }

    private TriggerValidator() {}

    public static boolean isValid(AbilityTrigger trigger, TriggerContext context) {
        Set<TriggerContext> allowed = VALID.get(trigger);
        if (allowed == null) return false;
        return allowed.contains(context);
    }

    public static TriggerContext validateOrFallback(AbilityTrigger trigger,
                                                    TriggerContext context,
                                                    String abilityKey) {
        if (isValid(trigger, context)) return context;

        java.util.logging.Logger.getLogger("ALBCore").warning(
                "[ALBCore | TriggerValidator] Ability \"" + abilityKey
                        + "\" uses context " + context
                        + " with trigger " + trigger
                        + " — this combination is not allowed. Falling back to SELF.");

        return TriggerContext.SELF;
    }

    public static Set<TriggerContext> getAllowed(AbilityTrigger trigger) {
        return VALID.getOrDefault(trigger, EnumSet.of(TriggerContext.SELF));
    }
}