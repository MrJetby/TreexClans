package me.jetby.treexclans.commands.clan.subcommands;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.CustomCommandApi;
import me.jetby.treexclans.api.service.clan.member.rank.RankPerms;
import me.jetby.treexclans.commands.Subcommand;
import me.jetby.treexclans.configurations.Lang;
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
                plugin.getLang().sendMessage(player, null, "commands.invite");
                return true;
            }
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                plugin.getLang().sendMessage(player, null, "your-not-in-clan");
                return true;
            }
            var clanImpl = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());
            if (!clanImpl.getMember(player.getUniqueId()).getRank().perms().contains(RankPerms.INVITE)) {
                plugin.getLang().sendMessage(player, clanImpl, "your-rank-is-not-allowed-to-do-that");
                return true;
            }
            if (clanImpl.getMembers().size() >= clanImpl.getLevel().maxMembers()) {
                plugin.getLang().sendMessage(player, clanImpl, "clan-invite-limit");
                return true;
            }

            Player target = player.getServer().getPlayer(args[0]);
            if (target == null) {
                plugin.getLang().sendMessage(player, clanImpl, "player-not-found");
                return true;
            }

            if (plugin.getClanManager().lookup().isInClan(target.getUniqueId())) {
                plugin.getLang().sendMessage(player, clanImpl, "clan-player-already-in-clan");
                return true;
            }
            if (Cooldown.isOnCooldown("invite_" + target.getUniqueId() + "_" + clanImpl.getId())) {
                plugin.getLang().sendMessage(player, clanImpl, "no-invite");
                return true;
            } else {
                Cooldown.setCooldown("invite_" + target.getUniqueId() + "_" + clanImpl.getId(), 60);
                plugin.getLang().sendMessage(player, clanImpl, "clan-invite", new Lang.ReplaceString("{target}", target.getName()));

                plugin.getLang().sendMessage(target, null, "clan-join-request",
                        new Lang.ReplaceString("{clan}", clanImpl.getId()),
                        new Lang.ReplaceString("{player}", player.getName())
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
    public CustomCommandApi.CommandType type() {
        return CustomCommandApi.CommandType.CLAN;
    }
}
