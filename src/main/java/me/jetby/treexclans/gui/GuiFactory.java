package me.jetby.treexclans.gui;

import lombok.experimental.UtilityClass;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.gui.types.Default;
import me.jetby.treexclans.gui.types.Members;
import me.jetby.treexclans.gui.types.Quests;
import me.jetby.treexclans.clan.Clan;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

@UtilityClass
public class GuiFactory {
    public TGui create(TreexClans plugin, @Nullable Menu menu, Player player, Clan clan) {
        if (menu==null) return new Default(plugin, null, player, clan);
        return switch (menu.type()) {
            case QUESTS -> new Quests(plugin, menu, player, clan);
            case MEMBERS -> new Members(plugin, menu, player, clan);
            default -> new Default(plugin, menu, player, clan);
        };
    }
}
