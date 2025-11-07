package me.jetby.treexclans.tools.customactions;

import me.jetby.treex.actions.Action;
import me.jetby.treex.actions.ActionContext;
import me.jetby.treexclans.TreexClans;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.jetby.treexclans.TreexClans.LOGGER;

public class MoneyGiveAction implements Action {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public void execute(@NotNull ActionContext ctx) {
        Player player = ctx.getPlayer();
        String message = ctx.get("message", String.class);

        if (player != null && message != null && plugin.getEconomy() != null) {
            try {
                int amount = Integer.parseInt(message);
                plugin.getEconomy().depositPlayer(player, amount);
            } catch (NumberFormatException e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }
}
