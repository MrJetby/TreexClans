package me.jetby.xClans.gui;

import com.jodexindustries.jguiwrapper.api.text.SerializerType;
import com.jodexindustries.jguiwrapper.gui.advanced.PaginatedAdvancedGui;
import me.jetby.xClans.records.Clan;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.*;

public abstract class TGui extends PaginatedAdvancedGui implements Listener {
    public TGui(JavaPlugin plugin, @Nullable Menu menu, Player player, Clan clan) {
        super(menu != null ? menu.title() : "Menu");
        defaultSerializer = SerializerType.LEGACY_AMPERSAND;
    }
    public abstract GuiType guiType();
}
