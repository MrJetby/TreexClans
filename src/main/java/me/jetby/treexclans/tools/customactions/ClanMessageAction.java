package me.jetby.treexclans.tools.customactions;

import me.jetby.treex.actions.Action;
import me.jetby.treex.actions.ActionContext;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import org.jetbrains.annotations.NotNull;

public class ClanMessageAction implements Action {
    private final TreexClans plugin = TreexClans.getInstance();
    @Override
    public void execute(@NotNull ActionContext ctx) {
        String message = ctx.get("message", String.class);
        Clan clan = ctx.get("clan", Clan.class);
        if (clan==null) return;
        plugin.getClanManager().sendMessage(clan, message);
    }
}
