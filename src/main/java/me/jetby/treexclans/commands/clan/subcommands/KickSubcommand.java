package me.jetby.treexclans.commands.clan.subcommands;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.rank.RankPerms;
import me.jetby.treexclans.commands.Subcommand;
import me.jetby.treexclans.configurations.Lang;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
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
            if (args.length==0) {
                plugin.getLang().sendMessage(player, null, "commands.kick");
                return true;
            }
            if (!plugin.getClanManager().isInClan(player.getUniqueId())) {
                plugin.getLang().sendMessage(player, null, "your-not-in-clan");
                return true;
            }
            Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());

            if (!clan.getMember(player.getUniqueId()).getRank().perms().contains(RankPerms.KICK)) {
                plugin.getLang().sendMessage(player, clan, "your-rank-is-not-allowed-to-do-that");
                return true;
            }

            String targetName = args[0];
            UUID uuid;
            Player target = Bukkit.getPlayer(targetName);
            if (target==null) {
                String string = "OfflinePlayer:" + targetName;
                uuid = UUID.nameUUIDFromBytes(string.getBytes(StandardCharsets.UTF_8));
            } else {
                uuid = target.getUniqueId();
            }
            Member member = clan.getMember(uuid);

            if (member==null) {
                plugin.getLang().sendMessage(player, clan, "player-not-found");
                return true;
            }
            if (target!=null && clan.getMember(player.getUniqueId()).equals(member)) {
                plugin.getLang().sendMessage(player, clan, "clan-you-cant-kick-yourself");
                return true;
            }
            if (clan.getLeader().equals(member)) {
                plugin.getLang().sendMessage(player, clan, "you-cant-do-that-with-leader");
                return true;
            }

            clan.removeMember(member);
            plugin.getLang().sendMessage(player, clan, "clan-player-kick", new Lang.ReplaceString("{target}", targetName));
            if (target!=null && target.isOnline()) {
                plugin.getLang().sendMessage(target, clan, "clan-you-was-kicked", new Lang.ReplaceString("{player}", player.getName()));
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (!plugin.getClanManager().isInClan(player.getUniqueId())) {
                return List.of();
            }
            Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());
            if (args.length>0) {
                return clan.getMembers().stream().map(member -> {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(member.getUuid());
                    return offlinePlayer.getName();
                }).toList();
            }
        }
        return List.of();
    }
}
