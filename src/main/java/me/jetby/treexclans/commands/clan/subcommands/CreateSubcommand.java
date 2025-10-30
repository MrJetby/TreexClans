package me.jetby.treexclans.commands.clan.subcommands;

import me.jetby.treexclans.ClanManager;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.events.OnClanCreate;
import me.jetby.treexclans.commands.Subcommand;
import me.jetby.treexclans.configurations.Lang;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class CreateSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();
    private final ClanManager clanManager = plugin.getClanManager();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player player) {

            if (clanManager.isInClan(player.getUniqueId())) {
                plugin.getLang().sendMessage(player, null, "commands.create");
                return true;
            } else {
                if (args.length < 1) {
                    sender.sendMessage("§cUsage: /clan create <clanName>");
                    return true;
                }
                String clanName = args[0].toLowerCase();
                if (clanManager.clanExists(clanName)) {
                    plugin.getLang().sendMessage(player, null, "clan-is-already-exists");
                    return true;
                }
                Member leader = new Member(
                        player.getUniqueId(),
                        plugin.getCfg().getLeaderRank(),
                        System.currentTimeMillis(),
                        System.currentTimeMillis() ,
                        false, false,
                        0, 0, new HashMap<>(),
                        0,0,0,0,0

                );
                if (!clanManager.isAllowedName(player, clanName)) {
                    return true;
                }

                if (clanManager.createClan(clanName, leader)) {
                    Clan clan = plugin.getClanManager().getClan(clanName);
                    plugin.getLang().sendMessage(player, clan, "clan-create", new Lang.ReplaceString("{clan}", clanName));
                }
            }

        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }


}
