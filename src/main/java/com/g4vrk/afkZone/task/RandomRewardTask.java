package com.g4vrk.afkZone.task;

import com.g4vrk.afkZone.util.ProbabilityCollection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RandomRewardTask extends PeriodicTask {

    private final ProbabilityCollection<Consumer<Player>> rewards;
    private final Supplier<Set<Player>> rewardReceiversSupplier;

    public RandomRewardTask(
            @NotNull Plugin plugin,
            long period,
            @NotNull Set<Consumer<Player>> rewards,
            @NotNull Supplier<Set<Player>> rewardReceiversSupplier
    ) {
        super(plugin, period);
        this.rewardReceiversSupplier = rewardReceiversSupplier;
        this.rewards = new ProbabilityCollection<>();

        final double chanceForAll = 100D / rewards.size();
        for (final Consumer<Player> reward : rewards) {
            this.rewards.add(reward, chanceForAll);
        }
    }

    @Override
    public void run() {
        for (final Player player : rewardReceiversSupplier.get()) {
            rewards.get().accept(player);
        }
    }
}
