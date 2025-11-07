package me.jetby.treexclans.tools.customactions;

import me.jetby.treex.actions.Action;
import me.jetby.treex.actions.ActionContext;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.ClanImpl;
import org.jetbrains.annotations.NotNull;

public class ClanMessageAction implements Action {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public void execute(@NotNull ActionContext ctx) {
        String message = ctx.get("message", String.class);
        ClanImpl clanImpl = ctx.get("clan", ClanImpl.class);
        if (clanImpl == null) return;
        plugin.getClanManager().chat().sendMessage(clanImpl, message);
    }
}
