package me.jetby.xClans.commands.clan.args;

import me.jetby.xClans.ClanManager;
import me.jetby.xClans.TreexClans;
import me.jetby.xClans.commands.Subcommand;
import me.jetby.xClans.records.Member;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Create implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();
    private final ClanManager clanManager = plugin.getClanManager();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player player) {

            if (clanManager.isInClan(player.getUniqueId())) {
                sender.sendMessage("§cYou are already in a clan.");
                return true;
            } else {
                if (args.length < 1) {
                    sender.sendMessage("§cUsage: /clan create <clanName>");
                    return true;
                }
                String clanName = args[0];
                if (clanManager.clanExists(clanName)) {
                    sender.sendMessage("§cA clan with that name already exists.");
                    return true;
                }
                Member leader = new Member(player.getUniqueId(), plugin.getCfg().getLeaderRank(), System.currentTimeMillis(), System.currentTimeMillis() ,false, null);
                if (clanManager.createClan(clanName, leader)) {
                    sender.sendMessage("§aClan " + clanName + " created successfully!");
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
