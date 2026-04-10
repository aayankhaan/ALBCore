package com.aayan.albcore.util;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class TaskScheduler {

    private final JavaPlugin plugin;

    public TaskScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    //  Delayed 

    public BukkitTask delayed(Runnable task, long delayTicks) {
        return plugin.getServer().getScheduler()
                .runTaskLater(plugin, task, delayTicks);
    }

    public BukkitTask delayedAsync(Runnable task, long delayTicks) {
        return plugin.getServer().getScheduler()
                .runTaskLaterAsynchronously(plugin, task, delayTicks);
    }

    //  Repeating 

    public BukkitTask repeating(Runnable task, long delayTicks, long periodTicks) {
        return plugin.getServer().getScheduler()
                .runTaskTimer(plugin, task, delayTicks, periodTicks);
    }

    public BukkitTask repeatingAsync(Runnable task, long delayTicks, long periodTicks) {
        return plugin.getServer().getScheduler()
                .runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
    }

    //  Immediate 

    public BukkitTask run(Runnable task) {
        return plugin.getServer().getScheduler()
                .runTask(plugin, task);
    }

    public BukkitTask runAsync(Runnable task) {
        return plugin.getServer().getScheduler()
                .runTaskAsynchronously(plugin, task);
    }

    //  Cancel 

    public void cancel(BukkitTask task) {
        if (task != null && !task.isCancelled()) task.cancel();
    }

    public void cancel(int taskId) {
        plugin.getServer().getScheduler().cancelTask(taskId);
    }

    public void cancelAll() {
        plugin.getServer().getScheduler().cancelTasks(plugin);
    }

    //  Run back on main thread (from async) 

    public BukkitTask sync(Runnable task) {
        return plugin.getServer().getScheduler()
                .runTask(plugin, task);
    }
}