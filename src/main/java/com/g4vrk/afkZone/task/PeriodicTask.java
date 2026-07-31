package com.g4vrk.afkZone.task;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public abstract class PeriodicTask extends BukkitRunnable {

    private final Plugin plugin;
    private final long period;

    public PeriodicTask(
            @NotNull Plugin plugin,
            long period
    ) {
        this.plugin = plugin;
        this.period = Math.max(1, period);
    }

    public void start() {
        try {
            super.runTaskTimer(plugin, period, period);
        } catch (final Exception ignored) {
        }
    }

    public void terminate() {
        super.cancel();
    }

    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    public long getPeriod() {
        return period;
    }

    @Override
    public abstract void run();
}
