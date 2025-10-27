package me.jetby.xClans.gui;

import lombok.experimental.UtilityClass;
import me.jetby.xClans.TreexClans;
import me.jetby.xClans.gui.types.Default;
import me.jetby.xClans.gui.types.Members;
import me.jetby.xClans.gui.types.Quests;
import me.jetby.xClans.clan.Clan;
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
