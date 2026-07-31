package com.g4vrk.afkZone;

import com.g4vrk.afkZone.listener.EntryListener;
import com.g4vrk.afkZone.task.RandomRewardTask;
import com.g4vrk.afkZone.util.ProbabilityCollection;
import com.g4vrk.fastTextFormatter.TextFormatter;
import com.g4vrk.functionalActions.defaults.DefaultActions;
import com.g4vrk.functionalActions.list.ExecutableActionList;
import com.g4vrk.functionalActions.parser.ActionParser;
import com.g4vrk.functionalActions.parser.impl.SimpleActionParser;
import com.g4vrk.functionalActions.registry.ActionRegistry;
import com.g4vrk.functionalActions.registry.impl.SimpleActionRegistry;
import com.g4vrk.functionalConfiguration.Config;
import com.g4vrk.functionalConfiguration.loader.YamlConfigLoader;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class AFKZonePlugin extends JavaPlugin {

    private static final String MAIN_CONFIG_FILE = "main-config.yml";

    private final TextFormatter textFormatter = TextFormatter.textFormatter();

    private final Map<UUID, RandomRewardTask> taskMap = new Object2ObjectOpenHashMap<>();

    private long taskPeriod;
    private ProbabilityCollection<BiConsumer<Player, Function<String, String>>> rewards;

    @Override
    public void onEnable() {
        final File pluginDir = getDataFolder();

        //noinspection ResultOfMethodCallIgnored
        pluginDir.mkdirs();

        final ActionRegistry<Player> actionRegistry = new SimpleActionRegistry<>(true);
        new DefaultActions.Player().registerDefaults(actionRegistry, textFormatter::format, ";");

        final ActionParser<Player> actionParser = new SimpleActionParser<>(actionRegistry);

        final Config mainConfig;

        try {
            saveResource(MAIN_CONFIG_FILE, false);
            mainConfig = new YamlConfigLoader().from(pluginDir.toPath().resolve(MAIN_CONFIG_FILE));
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }

        final String zoneRegionId = mainConfig.node("zone", "region-id").getString("afk_zone-region");

        final List<String> zoneEnterActions;
        final List<String> zoneLeaveActions;
        try {
            zoneEnterActions = mainConfig.node("zone", "actions", "on-enter").getList(String.class, Collections.emptyList());
            zoneLeaveActions = mainConfig.node("zone", "actions", "on-leave").getList(String.class, Collections.emptyList());
        } catch (final SerializationException ex) {
            throw new RuntimeException(ex);
        }

        final EntryListener listener = new EntryListener(
                zoneRegionId::equalsIgnoreCase,
                actionParser.parseAll(zoneEnterActions),
                actionParser.parseAll(zoneLeaveActions),
                this::startTask,
                this::terminateTask
        );

        getServer().getPluginManager().registerEvents(listener, this);

        rewards = new ProbabilityCollection<>();

        mainConfig.node("zone", "rewards", "actions").childrenMap().forEach((o, node) -> {
            final ExecutableActionList<? super Player> actions;
            try {
                actions = actionParser.parseAll(node.getList(String.class, Collections.emptyList()));
            } catch (final SerializationException ex) {
                throw new RuntimeException(ex);
            }

            rewards.add(actions::run, Double.parseDouble(String.valueOf(o)));
        });

        taskPeriod = mainConfig.node("zone", "rewards", "task-period-seconds").getLong(60) * 20L;
    }

    private void startTask(
            final @NotNull Player player
    ) {
        final RandomRewardTask task = taskMap.computeIfAbsent(player.getUniqueId(), uuid -> new RandomRewardTask(this, taskPeriod, player, () -> rewards.get()));

        task.start();
    }

    private void terminateTask(
            final @NotNull Player player
    ) {
        final RandomRewardTask removed = taskMap.remove(player.getUniqueId());

        if (removed != null) {
            removed.terminate();
        }
    }

}
