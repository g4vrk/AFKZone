package com.g4vrk.afkZone.listener;

import com.g4vrk.functionalActions.list.ExecutableActionList;
import net.raidstone.wgevents.events.RegionEnteredEvent;
import net.raidstone.wgevents.events.RegionLeftEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class EntryListener implements Listener {

    private final Predicate<String> regionIdFilter;

    private final ExecutableActionList<? super Player> zoneEnterActionList;
    private final ExecutableActionList<? super Player> zoneLeaveActionList;

    private final Consumer<Player> enterConsumer;
    private final Consumer<Player> leaveConsumer;

    public EntryListener(
            @NotNull Predicate<String> regionIdFilter,
            @NotNull ExecutableActionList<? super Player> zoneEnterActionList,
            @NotNull ExecutableActionList<? super Player> zoneLeaveActionList,
            @NotNull Consumer<Player> enterConsumer,
            @NotNull Consumer<Player> leaveConsumer
    ) {
        this.regionIdFilter = regionIdFilter;
        this.zoneEnterActionList = zoneEnterActionList;
        this.zoneLeaveActionList = zoneLeaveActionList;
        this.enterConsumer = enterConsumer;
        this.leaveConsumer = leaveConsumer;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRegionEntered(RegionEnteredEvent event) {
        if (!regionIdFilter.test(event.getRegionName())) return;

        final Player player = event.getPlayer();

        if (player == null) return;

        enterConsumer.accept(player);
        zoneEnterActionList.run(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRegionLeft(RegionLeftEvent event) {
        if (!regionIdFilter.test(event.getRegionName())) return;

        final Player player = event.getPlayer();

        if (player == null) return;

        leaveConsumer.accept(player);
        zoneLeaveActionList.run(player);
    }

}
