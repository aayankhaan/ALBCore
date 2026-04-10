package com.aayan.albcore.hook;

import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.HologramManager;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Display;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class FancyHologramsHook {

    private static final Logger LOG = Logger.getLogger("ALBCore");
    private static boolean enabled = false;
    private static HologramManager manager = null;

    // Track holograms created by this plugin
    private static final Map<String, Hologram> managed =
            new ConcurrentHashMap<>();

    public static void init() {
        if (Bukkit.getPluginManager().getPlugin("FancyHolograms") != null) {
            manager = FancyHologramsPlugin.get().getHologramManager();
            enabled = true;
            LOG.info("[ALBCore] FancyHolograms hooked!");
        } else {
            LOG.info("[ALBCore] FancyHolograms not found — hologram features disabled.");
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    // ── Create ────────────────────────────────────────────

    public static void create(String id, Location location, List<String> lines) {
        if (!enabled) return;

        delete(id);

        TextHologramData data = new TextHologramData(id, location);
        data.setText(lines);
        data.setBillboard(Display.Billboard.CENTER);

        Hologram hologram = manager.create(data);
        manager.addHologram(hologram);

        hologram.createHologram();
        managed.put(id, hologram);
    }

    // ── Update ────────────────────────────────────────────

    public static void update(String id, List<String> lines) {
        if (!enabled) return;

        Hologram hologram = managed.get(id);
        if (hologram == null) return;

        if (!(hologram.getData() instanceof TextHologramData data)) return;

        data.setText(lines);
        hologram.forceUpdate();
    }

    // ── Delete ────────────────────────────────────────────

    public static void delete(String id) {
        if (!enabled) return;

        Hologram hologram = managed.remove(id);
        if (hologram == null) return;

        hologram.deleteHologram();
        manager.removeHologram(hologram);
    }

    public static void deleteAll() {
        if (!enabled) return;

        for (String id : List.copyOf(managed.keySet())) {
            delete(id);
        }
    }

    // ── Exists ────────────────────────────────────────────

    public static boolean exists(String id) {
        return managed.containsKey(id);
    }
}