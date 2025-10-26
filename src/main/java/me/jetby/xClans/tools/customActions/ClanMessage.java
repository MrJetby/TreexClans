package me.jetby.xClans.tools.customActions;

import me.jetby.treex.actions.Action;
import me.jetby.treex.actions.ActionContext;
import me.jetby.xClans.TreexClans;
import me.jetby.xClans.records.Clan;
import org.jetbrains.annotations.NotNull;

public class ClanMessage implements Action {
    private final TreexClans plugin = TreexClans.getInstance();
    @Override
    public void execute(@NotNull ActionContext ctx) {
        String message = ctx.get("message", String.class);
        Clan clan = ctx.get("clan", Clan.class);
        if (clan==null) return;
        plugin.getClanManager().sendMessage(clan, message);
    }
}
