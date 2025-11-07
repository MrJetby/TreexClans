package me.jetby.treexclans.commands.clan.subcommands;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.CustomCommandApi;
import me.jetby.treexclans.api.service.clan.member.rank.Rank;
import me.jetby.treexclans.api.service.clan.member.rank.RankPerms;
import me.jetby.treexclans.commands.Subcommand;
import me.jetby.treexclans.configurations.Lang;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SetRankSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (args.length < 2) {
                plugin.getLang().sendMessage(player, null, "commands.setrank");
                return true;
            }
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                plugin.getLang().sendMessage(player, null, "your-not-in-clan");
                return true;
            }

            var clanImpl = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());

            if (!clanImpl.getMember(player.getUniqueId()).getRank().perms().contains(RankPerms.SETRANK)) {
                plugin.getLang().sendMessage(player, clanImpl, "your-rank-is-not-allowed-to-do-that");
                return true;
            }

            String rankName = args[0].toLowerCase();
            Rank rank = clanImpl.getRanks().get(rankName);
            if (rank != null) {
                if (plugin.getCfg().getLeaderRank().equals(rank)) {
                    return true;
                }
                String targetName = args[1];
                UUID uuid;
                Player target = Bukkit.getPlayer(targetName);
                if (target == null) {
                    String string = "OfflinePlayer:" + targetName;
                    uuid = UUID.nameUUIDFromBytes(string.getBytes(StandardCharsets.UTF_8));
                } else {
                    uuid = target.getUniqueId();
                }
                var targetMemberImpl = clanImpl.getMember(uuid);

                if (target != null && clanImpl.getMember(player.getUniqueId()).equals(targetMemberImpl)) {
                    plugin.getLang().sendMessage(player, clanImpl, "clan-you-cant-setrank-yourself");
                    return true;
                }

                if (clanImpl.getLeader().equals(targetMemberImpl)) {
                    plugin.getLang().sendMessage(player, clanImpl, "you-cant-do-that-with-leader");
                    return true;
                }

                plugin.getLang().sendMessage(player, clanImpl, "clan-setrank",
                        new Lang.ReplaceString("{target}", targetName),
                        new Lang.ReplaceString("{player}", player.getName()),
                        new Lang.ReplaceString("{rank_prefix}", rank.name())
                );
                targetMemberImpl.setRank(rank);
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                return null;
            }
            var clanImpl = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());
            if (args.length == 1) {
                List<String> playerNames = new ArrayList<>();
                for (var memberImpl : clanImpl.getMembers()) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberImpl.getUuid());
                    playerNames.add(offlinePlayer.getName());
                }
                return playerNames;
            } else if (args.length == 2) {
                return clanImpl.getRanks().values().stream()
                        .filter(s1 -> !plugin.getCfg().getLeaderRank().equals(s1))
                        .map(rank -> rank.id())
                        .map(String::toLowerCase)
                        .toList();
            }
        }
        return null;
    }

    @Override
    public CustomCommandApi.CommandType type() {
        return CustomCommandApi.CommandType.CLAN;
    }
}
