package com.aayan.albcore;

import com.aayan.albcore.ability.trigger.*;
import com.aayan.albcore.api.ALBCoreAPI;
import com.aayan.albcore.command.ALBCoreCommand;
import com.aayan.albcore.data.DatabaseManager;
import com.aayan.albcore.data.PlacedBlockTracker;
import com.aayan.albcore.gui.GuiManager;
import com.aayan.albcore.input.InputListener;
import com.aayan.albcore.hook.ALBCorePlaceholders;
import com.aayan.albcore.hook.FancyHologramsHook;
import com.aayan.albcore.hook.MythicMobsHook;
import com.aayan.albcore.hook.ProtectionHook;
import com.aayan.albcore.hook.ProtocolLibHook;
import com.aayan.albcore.hook.ShopGUIPlusHook;
import com.aayan.albcore.hook.VaultHook;
import com.aayan.albcore.input.ProtocolSignManager;
import com.aayan.albcore.util.PlaceholderUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ALBCore extends JavaPlugin {

    private static ALBCore instance;
    private ALBCoreAPI api;
    private OnHoldTrigger onHoldTrigger;
    private OnSneakTrigger onSneakTrigger;
    private OnClickTrigger onClickTrigger;
    private OnFishTrigger onFishTrigger;
    private OnBreakTrigger onBreakTrigger;
    private OnBlockDamageTrigger onBlockDamageTrigger;
    private OnAttackTrigger onAttackTrigger;
    private OnDefendTrigger onDefendTrigger;
    private OnWearTrigger onWearTrigger;
    private DatabaseManager database;
    private VaultHook vault;
    private ShopGUIPlusHook shop;
    private ProtocolLibHook protocolLib;

    @Override
    public void onEnable() {
        instance = this;

        // Save default effect configs
        saveDefaultEffects();

        api = new ALBCoreAPI(this);

        // init util first before anything uses it
        PlaceholderUtil.init();

        database = new DatabaseManager(this);
        vault    = new VaultHook();
        vault.init();
        shop     = new ShopGUIPlusHook();
        shop.init();
        protocolLib = new ProtocolLibHook();
        protocolLib.init();

        api.setDatabase(database);
        api.setVault(vault);
        api.setShop(shop);
        api.setProtocolLib(protocolLib);

        // Load persistent cooldowns
        api.cooldowns().loadFromDatabase();

//        if (protocolLib.isEnabled()) {
//            ProtocolSignManager.init();
//        }

        onHoldTrigger  = new OnHoldTrigger(this);
        onSneakTrigger = new OnSneakTrigger();
        onClickTrigger = new OnClickTrigger();
        onBreakTrigger = new OnBreakTrigger();
        onFishTrigger  = new OnFishTrigger();
        onBlockDamageTrigger = new OnBlockDamageTrigger();
        onAttackTrigger      = new OnAttackTrigger();
        onDefendTrigger      = new OnDefendTrigger();
        onWearTrigger        = new OnWearTrigger(this);
        api.setOnBlockDamageTrigger(onBlockDamageTrigger);
        api.setOnHoldTrigger(onHoldTrigger);
        api.setOnSneakTrigger(onSneakTrigger);
        api.setOnClickTrigger(onClickTrigger);
        api.setOnBreakTrigger(onBreakTrigger);
        api.setOnFishTrigger(onFishTrigger);
        api.setOnAttackTrigger(onAttackTrigger);
        api.setOnDefendTrigger(onDefendTrigger);
        api.setOnWearTrigger(onWearTrigger);

        getServer().getPluginManager().registerEvents(onSneakTrigger, this);
        getServer().getPluginManager().registerEvents(onClickTrigger, this);
        getServer().getPluginManager().registerEvents(onBreakTrigger, this);
        getServer().getPluginManager().registerEvents(onFishTrigger, this);
        getServer().getPluginManager().registerEvents(onBlockDamageTrigger, this);
        getServer().getPluginManager().registerEvents(onAttackTrigger, this);
        getServer().getPluginManager().registerEvents(onDefendTrigger, this);
        getServer().getPluginManager().registerEvents(onWearTrigger, this);
        getServer().getPluginManager().registerEvents(new PlacedBlockTracker(this), this);
        getServer().getPluginManager().registerEvents(new GuiManager(), this);
        getServer().getPluginManager().registerEvents(new InputListener(), this);


        if (PlaceholderUtil.isEnabled()) {
            new ALBCorePlaceholders().register();
        }

        if (Bukkit.getPluginManager().getPlugin("FancyHolograms") != null) {
            FancyHologramsHook.init();
        } else {
            getLogger().info("FancyHolograms not found — hologram features disabled.");
        }
        ProtectionHook.init();
        MythicMobsHook.init();

        getCommand("albcore").setExecutor(new ALBCoreCommand(this));
        getLogger().info("ALBCore v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (api != null) api.cooldowns().clearAll();
        if (database != null) {
            database.pruneCooldowns();
            database.close();
        }
        instance = null;
        getLogger().info("ALBCore disabled.");
    }

    private void saveDefaultEffects() {
        java.io.File effectsDir = new java.io.File(getDataFolder(), "effects");
        if (!effectsDir.exists()) effectsDir.mkdirs();

        java.io.File sample = new java.io.File(effectsDir, "undead_slayer.yml");
        if (!sample.exists()) {
            saveResource("effects/undead_slayer.yml", false);
        }
    }

    public FancyHologramsHook holograms() {
        return null; // static class — use FancyHologramsHook.create() directly
    }

    public static ALBCore getInstance() {
        return instance;
    }

    public static ALBCoreAPI api() {
        if (instance == null || instance.api == null) {
            java.util.logging.Logger.getLogger("ALBCore")
                    .warning("[ALBCore] API accessed before plugin loaded - returning null.");
            return null;
        }
        return instance.api;
    }
}