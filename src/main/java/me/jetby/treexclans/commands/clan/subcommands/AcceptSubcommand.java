package me.jetby.treexclans.commands.clan.subcommands;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.commands.Subcommand;
import me.jetby.treexclans.configurations.Lang;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import me.jetby.treexclans.tools.Cooldown;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class AcceptSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {



        if (sender instanceof Player player) {
            if (args.length==0) {
                plugin.getLang().sendMessage(player, null, "commands.accept");
                return true;
            }
            if (plugin.getClanManager().isInClan(player.getUniqueId())) {
                    plugin.getLang().sendMessage(player, null, "your-already-in-clan");
                return true;
            }
            if (!plugin.getClanManager().clanExists(args[0])) {
                plugin.getLang().sendMessage(player, null, "clan-does-not-exist");

                return true;
            }
            if (!Cooldown.isOnCooldown("invite_"+player.getUniqueId()+"_"+args[0])) {
                sender.sendMessage("§cYou have no pending clan invites.");
                return true;
            } else {
                Cooldown.removeCooldown("invite_"+player.getUniqueId()+"_"+args[0]);
                Clan clan = plugin.getClanManager().getClan(args[0]);
                Member member = new Member(
                        player.getUniqueId(),
                        plugin.getCfg().getDefaultRank(),
                        System.currentTimeMillis(),
                        System.currentTimeMillis() ,
                        false, false,
                        0, 0, new HashMap<>(),
                        0,0,0,0,0
                );
                plugin.getLang().sendMessage(player, clan, "clan-join", List.of(
                        new Lang.ReplaceString("{player}", player.getName()),
                        new Lang.ReplaceString("{clan}", clan.getId())
                ));
                clan.addMember(member);
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
