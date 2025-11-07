package me.jetby.treexclans.commands.clan.subcommands;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.CustomCommandApi;
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
import java.util.List;
import java.util.UUID;

public class KickSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {


        if (sender instanceof Player player) {
            if (args.length == 0) {
                plugin.getLang().sendMessage(player, null, "commands.kick");
                return true;
            }
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                plugin.getLang().sendMessage(player, null, "your-not-in-clan");
                return true;
            }
            var clanImpl = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());

            if (!clanImpl.getMember(player.getUniqueId()).getRank().perms().contains(RankPerms.KICK)) {
                plugin.getLang().sendMessage(player, clanImpl, "your-rank-is-not-allowed-to-do-that");
                return true;
            }

            String targetName = args[0];
            UUID uuid;
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                String string = "OfflinePlayer:" + targetName;
                uuid = UUID.nameUUIDFromBytes(string.getBytes(StandardCharsets.UTF_8));
            } else {
                uuid = target.getUniqueId();
            }
            var memberImpl = clanImpl.getMember(uuid);

            if (memberImpl == null) {
                plugin.getLang().sendMessage(player, clanImpl, "player-not-found");
                return true;
            }
            if (target != null && clanImpl.getMember(player.getUniqueId()).equals(memberImpl)) {
                plugin.getLang().sendMessage(player, clanImpl, "clan-you-cant-kick-yourself");
                return true;
            }
            if (clanImpl.getLeader().equals(memberImpl)) {
                plugin.getLang().sendMessage(player, clanImpl, "you-cant-do-that-with-leader");
                return true;
            }

            clanImpl.removeMember(memberImpl);
            plugin.getLang().sendMessage(player, clanImpl, "clan-player-kick", new Lang.ReplaceString("{target}", targetName));
            if (target != null && target.isOnline()) {
                plugin.getLang().sendMessage(target, clanImpl, "clan-you-was-kicked", new Lang.ReplaceString("{player}", player.getName()));
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                return List.of();
            }
            var clanImpl = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());
            if (args.length > 0) {
                return clanImpl.getMembers().stream()
                        .map(member -> {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(member.getUuid());
                    return offlinePlayer.getName();
                }).toList();
            }
        }
        return List.of();
    }

    @Override
    public CustomCommandApi.CommandType type() {
        return CustomCommandApi.CommandType.CLAN;
    }
}
