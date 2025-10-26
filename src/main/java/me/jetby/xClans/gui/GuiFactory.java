package me.jetby.xClans.gui;

import lombok.experimental.UtilityClass;
import me.jetby.xClans.gui.types.Quests;
import me.jetby.xClans.records.Clan;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@UtilityClass
public class GuiFactory {
    public TGui create(GuiType type, JavaPlugin plugin, Menu menu, Player player, Clan clan) {
        return switch (type) {
            case QUESTS -> new Quests(plugin, menu, player, clan);
            default -> null;
        };
    }
}
