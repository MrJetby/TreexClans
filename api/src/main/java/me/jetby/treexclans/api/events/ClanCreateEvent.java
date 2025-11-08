package me.jetby.treexclans.api.events;

import lombok.Getter;
import me.jetby.treexclans.api.service.clan.Clan;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class ClanCreateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    @NotNull
    private final Clan clan;
    @Nullable
    private final Player player;

    public ClanCreateEvent(@NotNull Clan clan, @Nullable Player player) {
        this.clan = clan;
        this.player = player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
