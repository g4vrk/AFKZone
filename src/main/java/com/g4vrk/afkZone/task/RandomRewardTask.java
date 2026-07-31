package com.g4vrk.afkZone.task;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class RandomRewardTask extends PeriodicTask {

    private final Player player;
    private final Supplier<BiConsumer<Player, Function<String, String>>> rewardGenerator;

    private int remainingSeconds;

    public RandomRewardTask(
            @NotNull Plugin plugin,
            long period,
            @NotNull Player player,
            @NotNull Supplier<BiConsumer<Player, Function<String, String>>> rewardGenerator
    ) {
        super(plugin, period);
        this.player = player;
        this.rewardGenerator = rewardGenerator;
        this.resetRemainingSeconds();
    }

    public int decreaseRemainingSeconds() {
        return this.remainingSeconds--;
    }

    private void resetRemainingSeconds() {
        this.remainingSeconds = (int) (getPeriod() / 20);
    }

    @Override
    public void run() {
        rewardGenerator.get().accept(player, s -> s.replace("{player}", player.getName()));
    }
}
