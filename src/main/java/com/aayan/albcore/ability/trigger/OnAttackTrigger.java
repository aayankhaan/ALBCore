package com.aayan.albcore.ability.trigger;

import com.aayan.albcore.ALBCore;
import com.aayan.albcore.condition.ConditionSet;
import com.aayan.albcore.condition.MobTypeCondition;
import com.aayan.albcore.util.TextUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public final class OnAttackTrigger implements Listener {

    private record AttackEntry(String itemId, long cooldownMs,
                               String cooldownKey, String cooldownMsg,
                               ConditionSet conditions,
                               BiConsumer<Player, Entity> action) {}

    private final List<AttackEntry> entries = new ArrayList<>();

    public void addCallback(String itemId, long cooldownMs, String cooldownKey,
                            String cooldownMsg, ConditionSet conditions,
                            BiConsumer<Player, Entity> action) {
        entries.add(new AttackEntry(itemId, cooldownMs, cooldownKey,
                cooldownMsg, conditions, action));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player)) return;

        ItemStack held = player.getInventory().getItemInMainHand();
        Entity victim = e.getEntity();

        String registryId = ALBCore.api().registry().getId(held).orElse(null);

        ALBCore.api().effects().fireAttackEffects(player, victim, held);

        for (AttackEntry entry : entries) {
            if (entry.itemId() != null && !entry.itemId().equals(registryId)) continue;

            injectMobTarget(entry.conditions(), victim);

            if (!entry.conditions().evaluate(player)) continue;

            if (entry.cooldownMs() > 0 && ALBCore.api().cooldowns()
                    .isOnCooldown(player.getUniqueId(), entry.cooldownKey())) {
                if (entry.cooldownMsg() != null) {
                    String remaining = ALBCore.api().cooldowns()
                            .getFormatted(player.getUniqueId(), entry.cooldownKey());
                    TextUtil.sendActionbar(player,
                            entry.cooldownMsg().replace("{remaining}", remaining));
                }
                continue;
            }

            if (entry.cooldownMs() > 0)
                ALBCore.api().cooldowns().set(player.getUniqueId(),
                        entry.cooldownKey(), entry.cooldownMs());

            entry.action().accept(player, victim);
        }
    }

    public void clear() { entries.clear(); }

    private void injectMobTarget(ConditionSet conditions, Entity target) {
        if (conditions == null || conditions.isEmpty()) return;
        try {
            var field = ConditionSet.class.getDeclaredField("conditions");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            var list = (java.util.List<com.aayan.albcore.condition.Condition>) field.get(conditions);
            for (var condition : list) {
                if (condition instanceof MobTypeCondition mtc) {
                    mtc.withTarget(target);
                }
            }
        } catch (Exception ignored) {}
    }
}