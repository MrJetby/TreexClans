package me.jetby.treexclans.gui.core;

import com.jodexindustries.jguiwrapper.gui.advanced.GuiItemController;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import me.jetby.treexclans.functions.glow.Equipment;
import me.jetby.treexclans.gui.Button;
import me.jetby.treexclans.gui.Menu;
import me.jetby.treexclans.gui.Gui;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ChooseColor extends Gui {

    public ChooseColor(TreexClans plugin, Menu menu, Player player, Clan clan) {
        super(plugin, menu, player, clan);

        size(menu.size());
        type(menu.inventoryType());

    }

    @Override
    public void onClick(Player player, Button button, InventoryClickEvent event, GuiItemController controller) {

        event.setCancelled(true);
        if (!controller.slots().contains(button.slot())) return;

        if (button.type().startsWith("color-")) {
            Member member = getClan().getMember(player.getUniqueId());

            Color color = Equipment.getColorByName(button.type().replace("color-", ""));
            getPlugin().getClanManager().setColor(getClan(), member, color);
            if (getPlugin().getGlow().hasObserver(player)) {
                getPlugin().getGlow().removeObserver(player);
                getPlugin().getGlow().addObserver(player, getClan().getMembers());
            }
        }

    }

}

