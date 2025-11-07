package me.jetby.treexclans.tools.customactions;

import me.jetby.treex.actions.Action;
import me.jetby.treex.actions.ActionContext;
import me.jetby.treexclans.clan.ClanImpl;
import me.jetby.treexclans.clan.MemberImpl;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.jetby.treexclans.TreexClans.LOGGER;

public class ClanExpTakeAction implements Action {
    @Override
    public void execute(@NotNull ActionContext ctx) {
        Player player = ctx.getPlayer();
        String message = ctx.get("message", String.class);
        ClanImpl clanImpl = ctx.get("clan", ClanImpl.class);

        if (player != null && message != null && clanImpl != null) {
            try {
                int amount = Integer.parseInt(message);
                var memberImpl = clanImpl.getMember(player.getUniqueId());
                clanImpl.takeExp(amount, memberImpl);
            } catch (NumberFormatException e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }
}
