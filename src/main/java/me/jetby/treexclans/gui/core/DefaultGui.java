package me.jetby.treexclans.gui.core;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.gui.Gui;
import me.jetby.treexclans.gui.Menu;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class DefaultGui extends Gui {
    public DefaultGui(TreexClans plugin, @Nullable Menu menu, Player player, Clan clan) {
        super(plugin, menu, player, clan);
        registerButtons();
    }
}
