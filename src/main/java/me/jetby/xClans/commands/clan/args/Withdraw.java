package me.jetby.xClans.commands.clan.args;

import me.jetby.xClans.TreexClans;
import me.jetby.xClans.commands.Subcommand;
import me.jetby.xClans.records.Clan;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Withdraw implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length==0) {
            sender.sendMessage("§c/clan withdraw [amount]");
            return true;
        }
        if (sender instanceof Player player) {
            if (!plugin.getClanManager().isInClan(player.getUniqueId())) {
                player.sendMessage(plugin.getLang().getMessage("your-not-in-clan"));
                return true;
            }

            Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());
            if (!clan.getMember(player.getUniqueId()).getRank().rankPermissions().deposit()) {
                player.sendMessage(plugin.getLang().getMessage("your-rank-is-not-allowed-to-do-that"));
                return true;
            }
            double balance = Double.parseDouble(args[0]);
            if (plugin.getClanManager().getBalance(clan)>=balance) {
                plugin.getEconomy().depositPlayer(player, balance);
                plugin.getClanManager().takeBalance(balance, clan);
                player.sendMessage(plugin.getLang().getMessage("clan-balance-withdraw").replace("{sum}", String.valueOf(balance)));
            } else {
                player.sendMessage("Your clan balance haven't enough money");
            }

        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }
}
