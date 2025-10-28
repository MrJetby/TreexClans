package me.jetby.treexclans.gui;

import com.jodexindustries.jguiwrapper.api.text.SerializerType;
import com.jodexindustries.jguiwrapper.gui.advanced.PaginatedAdvancedGui;
import me.jetby.treexclans.clan.Clan;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;

public abstract class TGui extends PaginatedAdvancedGui implements Listener {

    // TODO: Так как много кода повторяется придумай что то например регистр плейсхолдер отдельно и так далее
    //  можно поставить null если не надо

    public TGui(JavaPlugin plugin, @Nullable Menu menu, Player player, Clan clan) {
        super(menu != null ? menu.title() : "Menu");
        size(menu != null ? menu.size() : 54);
        type(menu != null ? menu.inventoryType() : InventoryType.CHEST);
        defaultSerializer = SerializerType.LEGACY_AMPERSAND;
        onOpen(event -> {
            if (!player.hasPermission(menu.permission())) {
                event.setCancelled(true);
            }
        });
    }
    public abstract GuiType guiType();
}
