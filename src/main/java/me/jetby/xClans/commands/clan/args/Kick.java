package me.jetby.xClans.commands.clan.args;

import me.jetby.xClans.TreexClans;
import me.jetby.xClans.commands.Subcommand;
import me.jetby.xClans.records.Clan;
import me.jetby.xClans.records.Member;
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

public class Kick implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (!plugin.getClanManager().isInClan(player.getUniqueId())) {
                sender.sendMessage("§cYou are not in a clan.");
                return true;
            }
            Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());

            if (!clan.getMember(player.getUniqueId()).getRank().rankPermissions().kick()) {
                player.sendMessage("You are not allowed to do that! Please ask your clan leader to give you permission");
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
                player.sendMessage("§cPlayer not found.");
                return true;
            }

            clan.removeMember(member);
            player.sendMessage(targetName+" was successfully kicked out the clan");
            if (target!=null && target.isOnline()) {
                target.sendMessage("You was kicked out your clan by "+player.getName());
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
