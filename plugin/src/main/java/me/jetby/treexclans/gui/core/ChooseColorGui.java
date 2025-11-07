package me.jetby.treexclans.gui.core;

import com.jodexindustries.jguiwrapper.gui.advanced.GuiItemController;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.service.clan.Clan;
import me.jetby.treexclans.clan.MemberImpl;
import me.jetby.treexclans.functions.glow.Equipment;
import me.jetby.treexclans.gui.Button;
import me.jetby.treexclans.gui.Gui;
import me.jetby.treexclans.gui.Menu;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.Nullable;

public class ChooseColorGui extends Gui {

    private final MemberImpl target;

    public ChooseColorGui(TreexClans plugin, Menu menu, Player player, Clan clanImpl, @Nullable MemberImpl target) {
        super(plugin, menu, player, clanImpl);
        this.target = target;
        registerButtons();

    }

    @Override
    public void onClick(Player player, Button button, GuiItemController controller) {

        if (!controller.slots().contains(button.slot())) return;

        if (button.type().startsWith("color-")) {
            var memberImpl = getClanImpl().getMember(player.getUniqueId());
            Color color = Equipment.getColorByName(button.type().replace("color-", ""));

            if (target != null) {
                getPlugin().getClanManager().colors().setColor(memberImpl, target, color);
                if (getPlugin().getGlow().hasObserver(getPlayer())) {
                    getPlugin().getGlow().removeObserver(getPlayer());
                    getPlugin().getGlow().addObserver(getPlayer(), getClanImpl().getMembers());
                }
                return;
            }

            getPlugin().getClanManager().colors().setColor(getClanImpl(), memberImpl, color);
            if (getPlugin().getGlow().hasObserver(player)) {
                getPlugin().getGlow().removeObserver(player);
                getPlugin().getGlow().addObserver(player, getClanImpl().getMembers());
            }
        }

    }

}

