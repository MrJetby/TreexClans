package me.jetby.treexclans.commands.clan.subcommands;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import me.jetby.treexclans.clan.rank.Rank;
import me.jetby.treexclans.clan.rank.RankPerms;
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
            if (!plugin.getClanManager().isInClan(player.getUniqueId())) {
                plugin.getLang().sendMessage(player, null, "your-not-in-clan");
                return true;
            }

            Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());

            if (!clan.getMember(player.getUniqueId()).getRank().perms().contains(RankPerms.SETRANK)) {
                plugin.getLang().sendMessage(player, clan, "your-rank-is-not-allowed-to-do-that");
                return true;
            }

            String rankName = args[1];
            Rank rank = clan.getRanks().get(rankName);
            if (rank != null) {
                String targetName = args[0];
                UUID uuid;
                Player target = Bukkit.getPlayer(targetName);
                if (target == null) {
                    String string = "OfflinePlayer:" + targetName;
                    uuid = UUID.nameUUIDFromBytes(string.getBytes(StandardCharsets.UTF_8));
                } else {
                    uuid = target.getUniqueId();
                }
                Member targetMember = clan.getMember(uuid);

                plugin.getLang().sendMessage(player, clan, "clan-setrank",
                        new Lang.ReplaceString("{target}", targetName),
                        new Lang.ReplaceString("{player}", player.getName()),
                        new Lang.ReplaceString("{rank_prefix}", rank.name())
                );
                targetMember.setRank(rank);
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (!plugin.getClanManager().isInClan(player.getUniqueId())) {
                return null;
            }
            Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());
            if (args.length == 1) {
                List<String> playerNames = new ArrayList<>();
                for (Member member : clan.getMembers()) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(member.getUuid());
                    playerNames.add(offlinePlayer.getName());
                }
                return playerNames;
            } else if (args.length == 2) {
                return new ArrayList<>(clan.getRanks().keySet());
            }
        }
        return null;
    }
}
