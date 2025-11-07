package me.jetby.treexclans.gui.core;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.service.clan.Clan;
import me.jetby.treexclans.api.gui.Gui;
import me.jetby.treexclans.api.gui.Menu;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class DefaultGui extends Gui {
    public DefaultGui(JavaPlugin plugin, @Nullable Menu menu, Player player, Clan clanImpl) {
        super(plugin, menu, player, clanImpl);
        registerButtons();
    }
}
