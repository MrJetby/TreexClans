package me.jetby.treexclans.tools.customactions;

import me.jetby.treex.actions.Action;
import me.jetby.treex.actions.ActionContext;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.jetby.treexclans.TreexClans.LOGGER;

public class ClanExpGiveAction implements Action {
    private final TreexClans plugin = TreexClans.getInstance();
    @Override
    public void execute(@NotNull ActionContext ctx) {
        Player player = ctx.getPlayer();
        String message = ctx.get("message", String.class);
        Clan clan = ctx.get("clan", Clan.class);

        if (message!=null && clan!=null) {
            if (player!=null) {
                try {
                    int amount = Integer.parseInt(message);
                    Member member = clan.getMember(player.getUniqueId());
                    clan.addExp(amount, member, plugin.getCfg().getLevels());
                } catch (NumberFormatException e) {
                    LOGGER.warn(e.getMessage());
                }
            } else {
                try {
                    int amount = Integer.parseInt(message);
                    clan.addExp(amount, plugin.getCfg().getLevels());
                } catch (NumberFormatException e) {
                    LOGGER.warn(e.getMessage());
                }
            }
        }
    }
}
