package com.g4vrk.afkZone;

import com.g4vrk.afkZone.listener.EntryListener;
import com.g4vrk.afkZone.task.RandomRewardTask;
import com.g4vrk.fastTextFormatter.TextFormatter;
import com.g4vrk.functionalActions.defaults.DefaultActions;
import com.g4vrk.functionalActions.list.ExecutableActionList;
import com.g4vrk.functionalActions.parser.ActionParser;
import com.g4vrk.functionalActions.parser.impl.SimpleActionParser;
import com.g4vrk.functionalActions.registry.ActionRegistry;
import com.g4vrk.functionalActions.registry.impl.SimpleActionRegistry;
import com.g4vrk.functionalConfiguration.Config;
import com.g4vrk.functionalConfiguration.loader.YamlConfigLoader;
import io.leangen.geantyref.TypeToken;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class AFKZonePlugin extends JavaPlugin {

    private static final String MAIN_CONFIG_FILE = "main-config.yml";

    private final TextFormatter textFormatter = TextFormatter.textFormatter();

    private final Set<Player> rewardReceivers = new ObjectOpenHashSet<>();

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
                rewardReceivers::add,
                rewardReceivers::remove
        );

        getServer().getPluginManager().registerEvents(listener, this);

        final Set<Consumer<Player>> rewards = new ObjectOpenHashSet<>();

        final List<List<String>> rewardsRaw;
        try {

            final TypeToken<List<List<String>>> listOfString = new TypeToken<>() {};

            rewardsRaw = mainConfig.node("zone", "rewards", "actions").get(listOfString);

        } catch (final SerializationException ex) {
            throw new RuntimeException(ex);
        }

        if (rewardsRaw == null) return;

        for (List<String> list : rewardsRaw) {
            final ExecutableActionList<? super Player> actions = actionParser.parseAll(list);

            rewards.add(actions::run);
        }

        final long taskPeriod = mainConfig.node("zone", "rewards", "task-period-seconds").getLong(60) * 20L;
        new RandomRewardTask(this, taskPeriod, rewards, () -> rewardReceivers).start();
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }
}
