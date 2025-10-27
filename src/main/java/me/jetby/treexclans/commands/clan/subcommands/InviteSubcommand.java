package me.jetby.treexclans.commands.clan.subcommands;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.commands.Subcommand;
import me.jetby.treexclans.configurations.Lang;
import me.jetby.treexclans.clan.Clan;
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
            if (args.length==0) {
                plugin.getLang().sendMessage(player, null, "commands.invite");
                return true;
            }
            if (!plugin.getClanManager().isInClan(player.getUniqueId())) {
                plugin.getLang().sendMessage(player, null, "your-not-in-clan");
                return true;
            }
            Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());
            if (!clan.getMember(player.getUniqueId()).getRank().rankPermissions().invite()) {
                plugin.getLang().sendMessage(player, clan, "your-rank-is-not-allowed-to-do-that");
                return true;
            }
            Player target = player.getServer().getPlayer(args[0]);
            if (target == null) {
                player.sendMessage("§cPlayer not found.");
                return true;
            }

            if (plugin.getClanManager().isInClan(target.getUniqueId())) {
                player.sendMessage("§cThat player is already in a clan.");
                return true;
            }
            if (Cooldown.isOnCooldown("invite_"+target.getUniqueId()+"_"+clan.getId())) {
                player.sendMessage("§cYou must wait before inviting that player again.");
                return true;
            } else {
                Cooldown.setCooldown("invite_"+target.getUniqueId()+"_"+clan.getId(), 60);
                plugin.getLang().sendMessage(player, clan, "clan-invite", new Lang.ReplaceString("{target}", target.getName()));

                plugin.getLang().sendMessage(target, null, "clan-join-request", List.of(
                        new Lang.ReplaceString("{clan}", clan.getId()),
                        new Lang.ReplaceString("{player}", player.getName())
                ));

            }
            return true;
        }

        return true;
    }


    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length>0) {
            return new ArrayList<>((Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList())));
        }
        return List.of();
    }
}
