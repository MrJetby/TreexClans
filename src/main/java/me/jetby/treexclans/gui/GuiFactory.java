package me.jetby.treexclans.gui;

import lombok.experimental.UtilityClass;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import me.jetby.treexclans.clan.rank.Rank;
import me.jetby.treexclans.functions.tops.TopType;
import me.jetby.treexclans.gui.core.*;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class GuiFactory {
    private final Map<String, Gui> customGuis = new HashMap<>();

    public Gui create(TreexClans plugin,
                      Menu menu,
                      Player player,
                      Clan clan,
                      @Nullable Rank rank,
                      @Nullable Member target,
                      @Nullable TopType topType) {
        return switch (GuiType.valueOf(menu.type())) {
            case MEMBERS -> new MembersGui(plugin, menu, player, clan);
            case CHOOSE_COLOR -> new ChooseColorGui(plugin, menu, player, clan, target);
            case CHEST -> new ChestGui(plugin, menu, player, clan);
            case QUESTS -> new QuestsGui(plugin, menu, player, clan);
            case RANKS -> new RanksGui(plugin, menu, player, clan);
            case RANK_PERMISSIONS -> new RankPermissionsGui(plugin, menu, player, clan, rank);
            case CHOOSE_PLAYER_COLOR -> new ChoosePlayerColorGui(plugin, menu, player, clan, target);
            case MENU -> new DefaultGui(plugin, menu, player, clan);
            case TOP_CLANS -> new TopClansGui(plugin, menu, player, clan, topType);
            default -> getCustomGuiOrDefault(plugin, menu, player, clan, menu.type());
        };
    }

    private Gui getCustomGuiOrDefault(TreexClans plugin, Menu menu, Player player, Clan clan, String type) {
        var gui = customGuis.get(type);
        if (gui!=null) return gui;

        return new DefaultGui(plugin, menu, player, clan);
    }

    public void registerCustomGui(String type, Gui gui) {
        customGuis.put(type.toUpperCase(), gui);
    }
    public void unregisterCustomGui(String type) {
        customGuis.remove(type.toUpperCase());
    }
}
