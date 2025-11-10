package me.jetby.treexclans.tools.customactions;

import me.jetby.treex.actions.Action;
import me.jetby.treex.actions.ActionContext;
import me.jetby.treex.text.Md5Button;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ButtonAction implements Action {
    @Override
    public void execute(@NotNull ActionContext ctx) {
        Player player = ctx.getPlayer();
        String context = ctx.get("context", String.class);
        if (player!=null) {
            Md5Button.send(player, context);
        }
    }
}
