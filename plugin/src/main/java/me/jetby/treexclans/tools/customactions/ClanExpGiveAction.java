package me.jetby.treexclans.tools.customactions;

import me.jetby.treex.actions.Action;
import me.jetby.treex.actions.ActionContext;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.ClanImpl;
import me.jetby.treexclans.clan.MemberImpl;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.jetby.treexclans.TreexClans.LOGGER;

public class ClanExpGiveAction implements Action {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public void execute(@NotNull ActionContext ctx) {
        Player player = ctx.getPlayer();
        String message = ctx.get("message", String.class);
        ClanImpl clanImpl = ctx.get("clan", ClanImpl.class);

        if (message != null && clanImpl != null) {
            if (player != null) {
                try {
                    int amount = Integer.parseInt(message);
                    var memberImpl = clanImpl.getMember(player.getUniqueId());
                    clanImpl.addExp(amount, memberImpl, plugin.getCfg().getLevels());
                } catch (NumberFormatException e) {
                    LOGGER.warn(e.getMessage());
                }
            } else {
                try {
                    int amount = Integer.parseInt(message);
                    clanImpl.addExp(amount, plugin.getCfg().getLevels());
                } catch (NumberFormatException e) {
                    LOGGER.warn(e.getMessage());
                }
            }
        }
    }
}
