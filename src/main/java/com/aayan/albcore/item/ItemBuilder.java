package com.aayan.albcore.item;

import com.aayan.albcore.ALBCore;
import com.aayan.albcore.ability.trigger.AbilityTrigger;
import com.aayan.albcore.condition.Condition;
import com.aayan.albcore.condition.ConditionSet;
import com.aayan.albcore.util.TextUtil;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;
    private final List<String> loreLines = new ArrayList<>();
    private String rarityKey = null;
    private long onHoldCooldownMs = 0L;
    private String onHoldCooldownMsg = null;
    private Consumer<Player> onHoldAction = null;
    private ConditionSet conditions = ConditionSet.empty();
    private long onSneakCooldownMs = 0L;
    private String onSneakCooldownMsg = null;
    private Consumer<Player> onSneakAction = null;
    private long onClickCooldownMs = 0L;
    private String onClickCooldownMsg = null;
    private Consumer<Player> onClickAction = null;
    private long onBreakCooldownMs = 0L;
    private String onBreakCooldownMsg = null;
    private BiConsumer<Player, org.bukkit.block.Block> onBreakAction = null;
    private long onFishCooldownMs = 0L;
    private String onFishCooldownMsg = null;
    private Consumer<Player> onFishAction = null;

    public ItemBuilder onFish(long cooldownMs, String cooldownMsg,
                              Consumer<Player> action) {
        this.onFishCooldownMs  = cooldownMs;
        this.onFishCooldownMsg = cooldownMsg;
        this.onFishAction      = action;
        return this;
    }

    public ItemBuilder onFish(Consumer<Player> action) {
        this.onFishAction = action;
        return this;
    }
    public ItemBuilder onBreak(long cooldownMs, String cooldownMsg,
                               BiConsumer<Player, org.bukkit.block.Block> action) {
        this.onBreakCooldownMs  = cooldownMs;
        this.onBreakCooldownMsg = cooldownMsg;
        this.onBreakAction      = action;
        return this;
    }

    public ItemBuilder onBreak(BiConsumer<Player, Block> action) {
        this.onBreakAction = action;
        return this;
    }
    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    // Name

    public ItemBuilder name(String miniMessage) {
        if (meta == null) return this;
        meta.displayName(TextUtil.parse("<i:false>" + miniMessage));
        return this;
    }

    // Lore

    public ItemBuilder lore(String... lines) {
        loreLines.addAll(Arrays.asList(lines));
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        loreLines.addAll(lines);
        return this;
    }

    public ItemBuilder loreLine(String line) {
        loreLines.add(line);
        return this;
    }

    // Amount

    public ItemBuilder amount(int amount) {
        item.setAmount(Math.max(1, Math.min(64, amount)));
        return this;
    }

    // Model data

    public ItemBuilder modelData(int data) {
        if (meta == null) return this;
        meta.setCustomModelData(data);
        return this;
    }

    // Unbreakable

    public ItemBuilder unbreakable(boolean value) {
        if (meta == null) return this;
        meta.setUnbreakable(value);
        if (value) meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        return this;
    }

    // Rarity

    public ItemBuilder rarity(String rarityKey) {
        this.rarityKey = rarityKey;
        return this;
    }

    // Flags

    public ItemBuilder flag(ItemFlag... flags) {
        if (meta == null) return this;
        meta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder hideAllFlags() {
        if (meta == null) return this;
        meta.addItemFlags(ItemFlag.values());
        return this;
    }

    public ItemBuilder hideAllAttributes() {
        if (meta == null) return this;
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        return this;
    }

    // Glow

    public ItemBuilder glow() {
        if (meta == null) return this;
        meta.setEnchantmentGlintOverride(true);
        return this;
    }

    // Enchants

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (meta == null) return this;
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder sharpness(int level) {
        return enchant(Enchantment.SHARPNESS, level);
    }

    public ItemBuilder protection(int level) {
        return enchant(Enchantment.PROTECTION, level);
    }

    public ItemBuilder unbreaking(int level) {
        return enchant(Enchantment.UNBREAKING, level);
    }

    public ItemBuilder fortune(int level) {
        return enchant(Enchantment.FORTUNE, level);
    }

    public ItemBuilder looting(int level) {
        return enchant(Enchantment.LOOTING, level);
    }

    public ItemBuilder efficiency(int level) {
        return enchant(Enchantment.EFFICIENCY, level);
    }

    public ItemBuilder mending() {
        return enchant(Enchantment.MENDING, 1);
    }

    public ItemBuilder silkTouch() {
        return enchant(Enchantment.SILK_TOUCH, 1);
    }

    // Armor color — leather only

    public ItemBuilder armorColor(int r, int g, int b) {
        if (!(meta instanceof LeatherArmorMeta leatherMeta)) {
            warn("armorColor() called on non-leather item — skipping");
            return this;
        }
        leatherMeta.setColor(Color.fromRGB(r, g, b));
        return this;
    }

    // Skull owner

    public ItemBuilder skullOwner(Player player) {
        if (!(meta instanceof SkullMeta skullMeta)) {
            warn("skullOwner() called on non-skull item — skipping");
            return this;
        }
        skullMeta.setOwningPlayer(player);
        return this;
    }

    /**
     * Set a custom texture on a player head using a Base64 string.
     * Uses Paper's PlayerProfile API.
     *
     * @param base64 The texture Base64 string.
     */
    public ItemBuilder skullBase64(String base64) {
        if (!(meta instanceof SkullMeta skullMeta)) {
            warn("skullBase64() called on non-skull item — skipping");
            return this;
        }
        if (base64 == null || base64.isEmpty()) return this;

        // Use a stable UUID based on the Base64 string so identical heads stack
        java.util.UUID uuid = java.util.UUID.nameUUIDFromBytes(base64.getBytes());
        com.destroystokyo.paper.profile.PlayerProfile profile = org.bukkit.Bukkit.createProfile(uuid);
        profile.setProperty(new com.destroystokyo.paper.profile.ProfileProperty("textures", base64));

        skullMeta.setPlayerProfile(profile);
        return this;
    }

    // Custom NBT

    public ItemBuilder customNBT(String key, String value) {
        if (meta == null) return this;
        meta.getPersistentDataContainer().set(
                new NamespacedKey(ALBCore.getInstance(), key),
                PersistentDataType.STRING, value);
        return this;
    }

    public ItemBuilder customNBT(String key, int value) {
        if (meta == null) return this;
        meta.getPersistentDataContainer().set(
                new NamespacedKey(ALBCore.getInstance(), key),
                PersistentDataType.INTEGER, value);
        return this;
    }

    public ItemBuilder customNBT(String key, double value) {
        if (meta == null) return this;
        meta.getPersistentDataContainer().set(
                new NamespacedKey(ALBCore.getInstance(), key),
                PersistentDataType.DOUBLE, value);
        return this;
    }

    // Trim

    public ItemBuilder trim(String materialName, String patternName) {
        if (!(meta instanceof ArmorMeta armorMeta)) return this;
        try {
            TrimMaterial material = org.bukkit.Registry.TRIM_MATERIAL
                    .get(NamespacedKey.minecraft(materialName.toLowerCase()));
            TrimPattern pattern = org.bukkit.Registry.TRIM_PATTERN
                    .get(NamespacedKey.minecraft(patternName.toLowerCase()));
            if (material != null && pattern != null) {
                armorMeta.setTrim(new ArmorTrim(material, pattern));
                armorMeta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM);
            } else {
                warn("Invalid trim: " + materialName + " / " + patternName);
            }
        } catch (Exception e) {
            warn("Failed to apply trim: " + e.getMessage());
        }
        return this;
    }

    // Triggers
    private long onBlockDamageCooldownMs = 0L;
    private String onBlockDamageCooldownMsg = null;
    private BiConsumer<Player, Block> onBlockDamageAction = null;

    public ItemBuilder onBlockDamage(long cooldownMs, String cooldownMsg,
                                     BiConsumer<Player, Block> action) {
        this.onBlockDamageCooldownMs  = cooldownMs;
        this.onBlockDamageCooldownMsg = cooldownMsg;
        this.onBlockDamageAction      = action;
        return this;
    }

    public ItemBuilder onBlockDamage(BiConsumer<Player, Block> action) {
        this.onBlockDamageAction = action;
        return this;
    }

    public ItemBuilder onHold(long cooldownMs, String cooldownMsg, Consumer<Player> action) {
        this.onHoldCooldownMs  = cooldownMs;
        this.onHoldCooldownMsg = cooldownMsg;
        this.onHoldAction      = action;
        return this;
    }

    public ItemBuilder onHold(Consumer<Player> action) {
        this.onHoldAction = action;
        return this;
    }

    public ItemBuilder onSneak(long cooldownMs, String cooldownMsg, Consumer<Player> action) {
        this.onSneakCooldownMs  = cooldownMs;
        this.onSneakCooldownMsg = cooldownMsg;
        this.onSneakAction      = action;
        return this;
    }

    public ItemBuilder onSneak(Consumer<Player> action) {
        this.onSneakAction = action;
        return this;
    }

    public ItemBuilder onClick(long cooldownMs, String cooldownMsg, Consumer<Player> action) {
        this.onClickCooldownMs  = cooldownMs;
        this.onClickCooldownMsg = cooldownMsg;
        this.onClickAction      = action;
        return this;
    }

    public ItemBuilder onClick(Consumer<Player> action) {
        this.onClickAction = action;
        return this;
    }

    // Conditions

    public ItemBuilder condition(Condition... conditions) {
        this.conditions.add(conditions);
        return this;
    }

    // Build

    public ItemStack build() {
        if (meta == null) return item;

        if (!loreLines.isEmpty()) {
            String rarityDisplay = rarityKey == null
                    ? "<gray>None"
                    : ALBCore.api().rarities().getDisplay(rarityKey);

            meta.lore(loreLines.stream()
                    .map(line -> {
                        String resolved = line.contains("%rarity%")
                                ? line.replace("%rarity%", rarityDisplay)
                                : line;
                        return TextUtil.parse("<i:false>" + resolved);
                    })
                    .collect(Collectors.toList()));
        }

        item.setItemMeta(meta);
        return item;
    }

    // Build and register

    public ItemStack buildAndRegister(String id) {
        ItemStack built = build();
        ALBCore.api().registry().register(id, built);

        if (onHoldAction != null) {
            ALBCore.api().trigger(AbilityTrigger.ON_HOLD)
                    .forItem(id)
                    .withCooldown(onHoldCooldownMs)
                    .withCooldownMessage(onHoldCooldownMsg)
                    .conditions(conditions)
                    .run(onHoldAction)
                    .register();
        }
        if (onSneakAction != null) {
            ALBCore.api().trigger(AbilityTrigger.ON_SNEAK)
                    .forItem(id)
                    .withCooldown(onSneakCooldownMs)
                    .withCooldownMessage(onSneakCooldownMsg)
                    .conditions(conditions)
                    .run(onSneakAction)
                    .register();
        }
        if (onClickAction != null) {
            ALBCore.api().trigger(AbilityTrigger.ON_CLICK)
                    .forItem(id)
                    .withCooldown(onClickCooldownMs)
                    .withCooldownMessage(onClickCooldownMsg)
                    .conditions(conditions)
                    .run(onClickAction)
                    .register();
        }
        if (onBreakAction != null) {
            ALBCore.api().trigger(AbilityTrigger.ON_BREAK)
                    .forItem(id)
                    .withCooldown(onBreakCooldownMs)
                    .withCooldownMessage(onBreakCooldownMsg)
                    .conditions(conditions)
                    .runOnBreak(onBreakAction)
                    .register();
        }
        if (onFishAction != null) {
            ALBCore.api().trigger(AbilityTrigger.ON_FISH)
                    .forItem(id)
                    .withCooldown(onFishCooldownMs)
                    .withCooldownMessage(onFishCooldownMsg)
                    .conditions(conditions)
                    .runOnFish(onFishAction)
                    .register();
        }
        if (onBlockDamageAction != null) {
            ALBCore.api().trigger(AbilityTrigger.ON_BLOCK_DAMAGE)
                    .forItem(id)
                    .withCooldown(onBlockDamageCooldownMs)
                    .withCooldownMessage(onBlockDamageCooldownMsg)
                    .conditions(conditions)
                    .runOnBlockDamage(onBlockDamageAction)
                    .register();
        }
        return built;
    }

    // Internal

    private void warn(String msg) {
        java.util.logging.Logger.getLogger("ALBCore")
                .warning("[ALBCore | ItemBuilder] " + msg);
    }
}