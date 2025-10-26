package me.jetby.xClans.gui.types;

import me.jetby.xClans.gui.GuiType;
import me.jetby.xClans.gui.Menu;
import me.jetby.xClans.gui.TGui;
import me.jetby.xClans.records.Clan;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Quests extends TGui {

    public Quests(JavaPlugin plugin, Menu menu, Player player, Clan clan) {
        super(plugin, menu, player, clan);


    }

    @Override
    public GuiType guiType() {
        return GuiType.QUESTS;
    }

}
