package me.jetby.treexclans.tools.customactions;

import me.jetby.treex.actions.Action;
import me.jetby.treex.actions.ActionContext;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.jetby.treexclans.TreexClans.LOGGER;

public class CoinAddAction implements Action {
    private final TreexClans plugin = TreexClans.getInstance();
    @Override
    public void execute(@NotNull ActionContext ctx) {
        Player player = ctx.getPlayer();
        String message = ctx.get("message", String.class);
        Clan clan = ctx.get("clan", Clan.class);

        if (player!=null && message!=null && clan!=null) {
            try {
                int amount = Integer.parseInt(message);
                Member member = clan.getMember(player.getUniqueId());
                member.addCoin(amount);
            } catch (NumberFormatException e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }
}
