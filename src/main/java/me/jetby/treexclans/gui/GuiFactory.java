package me.jetby.treexclans.gui;

import lombok.experimental.UtilityClass;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.gui.core.*;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class GuiFactory {
    private final Map<String, Gui> customGuis = new HashMap<>();

    public Gui create(TreexClans plugin, Menu menu, Player player, Clan clan) {
        return switch (GuiType.valueOf(menu.type())) {
            case MEMBERS -> new Members(plugin, menu, player, clan);
            case CHOOSE_COLOR -> new ChooseColor(plugin, menu, player, clan);
            case CHEST -> new Chest(plugin, menu, player, clan);
            case QUESTS -> new Quests(plugin, menu, player, clan);
            default -> getCustomGuiOrDefault(plugin, menu, player, clan, menu.type());
        };
    }

    private Gui getCustomGuiOrDefault(TreexClans plugin, Menu menu, Player player, Clan clan, String type) {
        var gui = customGuis.get(type);
        if (gui!=null) return gui;

        return new Default(plugin, menu, player, clan);
    }

    public void registerCustomGui(String type, Gui gui) {
        customGuis.put(type.toUpperCase(), gui);
    }
    public void unregisterCustomGui(String type) {
        customGuis.remove(type.toUpperCase());
    }
}
