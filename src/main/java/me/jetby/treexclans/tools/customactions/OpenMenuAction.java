package me.jetby.treexclans.tools.customactions;

import me.jetby.treex.actions.Action;
import me.jetby.treex.actions.ActionContext;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.gui.Gui;
import me.jetby.treexclans.gui.GuiFactory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OpenMenuAction implements Action {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public void execute(@NotNull ActionContext ctx) {
        Player player = ctx.getPlayer();
        String message = ctx.get("message", String.class);
        Clan clan = ctx.get("clan", Clan.class);
        var menu = plugin.getGuiLoader().getMenus().get(message);
        if (menu!=null && player!=null && clan!=null) {
            Gui gui = GuiFactory.create(plugin, menu, player, clan, null, null, null);
            Bukkit.getScheduler().runTaskLater(plugin, () -> gui.open(player), 1L);
        }
    }
}
