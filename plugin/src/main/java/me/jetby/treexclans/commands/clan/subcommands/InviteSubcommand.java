package me.jetby.treexclans.commands.clan.subcommands;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.addons.commands.CommandService;;
import me.jetby.treexclans.api.service.clan.member.rank.RankPerms;
import me.jetby.treexclans.api.command.Subcommand;
import me.jetby.treexclans.configurations.Messages;
import me.jetby.treexclans.tools.Cooldown;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InviteSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {

        if (sender instanceof Player player) {
            if (args.length == 0) {
                plugin.getMessages().sendMessage(player, null, "commands.invite");
                return true;
            }
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                plugin.getMessages().sendMessage(player, null, "your-not-in-clan");
                return true;
            }
            var clan = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());
            if (!clan.getMember(player.getUniqueId()).getRank().perms().contains(RankPerms.INVITE)) {
                plugin.getMessages().sendMessage(player, clan, "your-rank-is-not-allowed-to-do-that");
                return true;
            }
            if (clan.getMembers().size() >= clan.getLevel().maxMembers()) {
                plugin.getMessages().sendMessage(player, clan, "clan-invite-limit");
                return true;
            }

            Player target = player.getServer().getPlayer(args[0]);
            if (target == null) {
                plugin.getMessages().sendMessage(player, clan, "player-not-found");
                return true;
            }

            if (plugin.getClanManager().lookup().isInClan(target.getUniqueId())) {
                plugin.getMessages().sendMessage(player, clan, "clan-player-already-in-clan");
                return true;
            }
            if (Cooldown.isOnCooldown("denied_" + target.getUniqueId() + "_" + clan.getId())) {
                plugin.getMessages().sendMessage(player, clan, "clan-invite-denied", new Messages.ReplaceString("{target}", target.getName()));
                return true;
            }

            if (Cooldown.isOnCooldown("invite_" + target.getUniqueId() + "_" + clan.getId())) {
                plugin.getMessages().sendMessage(player, clan, "clan-already-invited");
            } else {
                Cooldown.setCooldown("invite_" + target.getUniqueId() + "_" + clan.getId(), 60);
                plugin.getMessages().sendMessage(player, clan, "clan-invite", new Messages.ReplaceString("{target}", target.getName()));

                plugin.getMessages().sendMessage(target, null, "clan-join-request",
                        new Messages.ReplaceString("{clan}", clan.getId()),
                        new Messages.ReplaceString("{player}", player.getName())
                );

            }
            return true;
        }

        return true;
    }


    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length > 0) {
            return new ArrayList<>((Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList())));
        }
        return List.of();
    }

    @Override
    public CommandService.CommandType type() {
        return CommandService.CommandType.CLAN;
    }
}
