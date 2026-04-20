package com.aayan.albcore.api;

import com.aayan.albcore.ALBCore;
import com.aayan.albcore.ability.trigger.*;
import com.aayan.albcore.config.CoreConfig;
import com.aayan.albcore.data.DatabaseManager;
import com.aayan.albcore.data.PlayerStatManager;
import com.aayan.albcore.effect.ConsoleCommandEffect;
import com.aayan.albcore.effect.DamageBoostEffect;
import com.aayan.albcore.effect.HealEffect;
import com.aayan.albcore.effect.AddHeartsEffect;
import com.aayan.albcore.effect.LightningEffect;
import com.aayan.albcore.effect.MessageEffect;
import com.aayan.albcore.effect.PotionEffect;
import com.aayan.albcore.effect.SoundEffect;
import com.aayan.albcore.effect.TimberEffect;
import com.aayan.albcore.hook.PlaceholderRegistry;
import com.aayan.albcore.hook.ProtocolLibHook;
import com.aayan.albcore.hook.ShopGUIPlusHook;
import com.aayan.albcore.hook.VaultHook;
import com.aayan.albcore.item.CustomItemRegistry;
import com.aayan.albcore.item.ItemBuilder;
import com.aayan.albcore.item.RarityManager;
import com.aayan.albcore.manager.CooldownManager;
import com.aayan.albcore.manager.MultiplierManager;
import com.aayan.albcore.reward.RewardExecutor;
import com.aayan.albcore.input.AnvilInput;
import com.aayan.albcore.input.SignInput;
import com.aayan.albcore.util.DebugLogger;
import com.aayan.albcore.util.ParticleBuilder;
import com.aayan.albcore.util.SerializationUtil;
import com.aayan.albcore.util.TaskScheduler;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;

public final class ALBCoreAPI {

    private final ALBCore plugin;

    // managers
    private final CooldownManager cooldowns = new CooldownManager();
    private final CustomItemRegistry registry;
    private final RarityManager rarities;
    private final CoreConfig config;
    private final DebugLogger debug;
    private final TaskScheduler scheduler;
    private final MultiplierManager multipliers = new MultiplierManager();

    // effects
    private final MessageEffect       message    = new MessageEffect();
    private final SoundEffect         sound      = new SoundEffect();
    private final ConsoleCommandEffect consoleCmd = new ConsoleCommandEffect();
    private final RewardExecutor      rewards    = new RewardExecutor();
    private final PotionEffect        potion     = new PotionEffect();
    private final TimberEffect        timber     = new TimberEffect();
    private final LightningEffect     lightning  = new LightningEffect();
    private final HealEffect          heal       = new HealEffect();
    private final AddHeartsEffect     addHearts  = new AddHeartsEffect();
    private final DamageBoostEffect   damageBoost = new DamageBoostEffect();

    // triggers
    private OnHoldTrigger onHoldTrigger;
    private OnSneakTrigger onSneakTrigger;
    private OnClickTrigger onClickTrigger;
    private OnBreakTrigger onBreakTrigger;
    private OnFishTrigger onFishTrigger;
    public void setOnFishTrigger(OnFishTrigger t) { this.onFishTrigger = t; }
    public OnFishTrigger onFish()                 { return onFishTrigger; }
    private OnBlockDamageTrigger onBlockDamageTrigger;
    public void setOnBlockDamageTrigger(OnBlockDamageTrigger t) { this.onBlockDamageTrigger = t; }
    public OnBlockDamageTrigger onBlockDamage() { return onBlockDamageTrigger; }

    private OnAttackTrigger onAttackTrigger;
    public void setOnAttackTrigger(OnAttackTrigger t) { this.onAttackTrigger = t; }
    public OnAttackTrigger onAttack() { return onAttackTrigger; }

    private OnDefendTrigger onDefendTrigger;
    public void setOnDefendTrigger(OnDefendTrigger t) { this.onDefendTrigger = t; }
    public OnDefendTrigger onDefend() { return onDefendTrigger; }

    private OnWearTrigger onWearTrigger;
    public void setOnWearTrigger(OnWearTrigger t) { this.onWearTrigger = t; }
    public OnWearTrigger onWear() { return onWearTrigger; }

    // hooks
    private DatabaseManager database;
    private PlayerStatManager stats;
    private VaultHook vault;
    private ShopGUIPlusHook shop;
    private ProtocolLibHook protocolLib;
    private final PlaceholderRegistry placeholders = new PlaceholderRegistry();

    public PlaceholderRegistry placeholders() { return placeholders; }
    public ALBCoreAPI(ALBCore plugin) {
        this.plugin = plugin;

        this.config = new CoreConfig(plugin);
        this.debug = new DebugLogger(plugin);          // FIXED
        this.scheduler = new TaskScheduler(plugin);
        this.registry = new CustomItemRegistry(plugin); // FIXED
        this.rarities = new RarityManager(plugin);

        this.rarities.load();
    }

    public void setDatabase(DatabaseManager database) {
        this.database = database;
        this.stats = new PlayerStatManager(database);
    }

    public void setVault(VaultHook vault) { this.vault = vault; }

    public void setShop(ShopGUIPlusHook shop) { this.shop = shop; }

    public void setProtocolLib(ProtocolLibHook protocolLib) { this.protocolLib = protocolLib; }

    public void setOnHoldTrigger(OnHoldTrigger t) { this.onHoldTrigger = t; }
    public void setOnSneakTrigger(OnSneakTrigger t) { this.onSneakTrigger = t; }
    public void setOnClickTrigger(OnClickTrigger t) { this.onClickTrigger = t; }
    public void setOnBreakTrigger(OnBreakTrigger t) { this.onBreakTrigger = t; }

    public CooldownManager cooldowns() { return cooldowns; }
    public CustomItemRegistry registry() { return registry; }
    public RarityManager rarities() { return rarities; }
    public CoreConfig config() { return config; }
    public DebugLogger debug() { return debug; }
    public TaskScheduler scheduler() { return scheduler; }
    public MultiplierManager multipliers() { return multipliers; }

    public MessageEffect        message()    { return message; }
    public SoundEffect          sound()      { return sound; }
    public ConsoleCommandEffect consoleCmd() { return consoleCmd; }
    public RewardExecutor       rewards()    { return rewards; }
    public PotionEffect         potion()     { return potion; }
    public TimberEffect         timber()     { return timber; }
    public LightningEffect      lightning()  { return lightning; }
    public HealEffect           heal()       { return heal; }
    public AddHeartsEffect      addHearts()  { return addHearts; }
    public DamageBoostEffect    damageBoost() { return damageBoost; }

    public DatabaseManager db() { return database; }
    public PlayerStatManager stats() { return stats; }
    public VaultHook vault() { return vault; }
    public ShopGUIPlusHook shop() { return shop; }
    public ProtocolLibHook protocolLib() { return protocolLib; }

    public OnHoldTrigger onHold() { return onHoldTrigger; }
    public OnSneakTrigger onSneak() { return onSneakTrigger; }
    public OnClickTrigger onClick() { return onClickTrigger; }
    public OnBreakTrigger onBreak() { return onBreakTrigger; }

    public ItemBuilder item(Material material) {
        return new ItemBuilder(material);
    }

    public TriggerBuilder trigger(AbilityTrigger trigger) {
        return TriggerBuilder.of(trigger);
    }

    public ParticleBuilder particles(Particle particle) {
        return new ParticleBuilder(particle);
    }

    public String toBase64(ItemStack item) {
        return SerializationUtil.toBase64(item);
    }

    public ItemStack fromBase64(String base64) {
        return SerializationUtil.fromBase64(base64);
    }

    public AnvilInput anvilInput() {
        return new AnvilInput();
    }

    public SignInput signInput() {
        return new SignInput();
    }
}