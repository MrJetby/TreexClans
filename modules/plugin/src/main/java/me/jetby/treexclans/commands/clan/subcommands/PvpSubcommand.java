package me.jetby.treexclans.commands.clan.subcommands;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.addons.commands.CommandService;;
import me.jetby.treexclans.api.service.clan.member.rank.RankPerms;
import me.jetby.treexclans.api.command.Subcommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PvpSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                plugin.getLang().sendMessage(player, null, "your-not-in-clan");
                return true;
            }
            var clanImpl = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());

            if (!clanImpl.getMember(player.getUniqueId()).getRank().perms().contains(RankPerms.PVP)) {
                plugin.getLang().sendMessage(player, clanImpl, "your-rank-is-not-allowed-to-do-that");
                return true;
            }

            if (clanImpl.isPvp()) {
                plugin.getLang().sendMessage(player, clanImpl, "clan-pvp-off");
                clanImpl.setPvp(false);
            } else {
                plugin.getLang().sendMessage(player, clanImpl, "clan-pvp-on");
                clanImpl.setPvp(true);
            }
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
