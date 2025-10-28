package me.jetby.treexclans.gui;

import lombok.experimental.UtilityClass;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.gui.types.*;
import me.jetby.treexclans.clan.Clan;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class GuiFactory {
    private final Map<Clan, Chest> chests = new HashMap<>();
    public TGui create(TreexClans plugin, @Nullable Menu menu, Player player, Clan clan) {
        if (menu==null) return new Default(plugin, null, player, clan);
        return switch (menu.type()) {
            case QUESTS -> new Quests(plugin, menu, player, clan);
            case MEMBERS -> new Members(plugin, menu, player, clan);
            case CHEST -> new Chest(plugin, menu, player, clan);
            case CHOOSE_COLOR -> new ChooseColor(plugin, menu, player, clan);
            default -> new Default(plugin, menu, player, clan);
        };
    }

    private TGui getChest(TreexClans plugin, @Nullable Menu menu, Player player, Clan clan) {
        if (chests.containsKey(clan)) {
            return chests.get(clan);
        }
        Chest chest = new Chest(plugin, menu, player, clan);
        chests.put(clan, chest);
        return chest;
    }
}
