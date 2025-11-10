package me.jetby.treexclans.commands.clan.subcommands;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.addons.commands.CommandService;
import me.jetby.treexclans.api.command.Subcommand;
import me.jetby.treexclans.tools.Cooldown;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DenySubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {


        if (sender instanceof Player player) {
            if (args.length == 0) {
                plugin.getMessages().sendMessage(player, null, "commands.deny");
                return true;
            }

            var clanId = args[0];
            if (!plugin.getClanManager().lifecycle().clanExists(clanId)) {
                plugin.getMessages().sendMessage(player, null, "clan-does-not-exist");
                return true;
            }

            if (!Cooldown.isOnCooldown("invite_" + player.getUniqueId() + "_" + clanId)) {
                plugin.getMessages().sendMessage(player, null, "no-invite");
                return true;
            }

            Cooldown.setCooldown("denied_" + player.getUniqueId() + "_" + clanId, 120);
            Cooldown.removeCooldown("invite_" + player.getUniqueId() + "_" + clanId);

            plugin.getMessages().sendMessage(player, null, "clan-deny");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }

    @Override
    public CommandService.CommandType type() {
        return CommandService.CommandType.CLAN;
    }
}
