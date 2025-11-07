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
public class OnClanCreate extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    @NotNull
    private final Clan clanImpl;
    @Nullable
    private final Player player;

    public OnClanCreate(@NotNull Clan clanImpl, @Nullable Player player) {
        this.clanImpl = clanImpl;
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
