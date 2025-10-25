package me.jetby.xClans.commands.clan.args;

import me.jetby.xClans.ClanManager;
import me.jetby.xClans.TreexClans;
import me.jetby.xClans.commands.Subcommand;
import me.jetby.xClans.records.Clan;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Info implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();
    private final ClanManager clanManager = plugin.getClanManager();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {

        if (sender instanceof Player player) {
            if (args.length==1) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
                if (clanManager.isInClan(offlinePlayer.getUniqueId())) {
                    Clan clan = clanManager.getClanByMember(offlinePlayer.getUniqueId());
                    OfflinePlayer leader = Bukkit.getOfflinePlayer(clan.getLeader().getUuid());
                    player.sendMessage("Clan id: " + clan.getId());
                    player.sendMessage("Clan leader: " + leader.getName());
                    player.sendMessage("Clan Prefix: " + clan.getPrefix());
                    player.sendMessage("Clan Level: " + clan.getLevel().id());
                    player.sendMessage("Clan Members: " + clan.getMembers().size());
                    player.sendMessage("Hist Rank: " + clan.getMember(offlinePlayer.getUniqueId()).getRank().name());
                    player.sendMessage("His Last online: " + clanManager.getLastOnlineFormatted(offlinePlayer.getUniqueId()));
                } else {
                    player.sendMessage("§cYou are not in a clan.");
                }
                return true;
            }
            if (clanManager.isInClan(player.getUniqueId())) {
                Clan clan = clanManager.getClanByMember(player.getUniqueId());
                OfflinePlayer leader = Bukkit.getOfflinePlayer(clan.getLeader().getUuid());
                player.sendMessage("Clan id: " + clan.getId());
                player.sendMessage("Clan leader: " + leader.getName());
                player.sendMessage("Clan Prefix: " + clan.getPrefix());
                player.sendMessage("Clan Level: " + clan.getLevel().id());
                player.sendMessage("Clan Members: " + clan.getMembers().size());
                player.sendMessage("Your Rank: " + clan.getMember(player.getUniqueId()).getRank().name());
                player.sendMessage("Last online: " + clanManager.getLastOnlineFormatted(player.getUniqueId()));
            } else {
                player.sendMessage("§cYou are not in a clan.");
            }
            return true;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }
}
