package me.jetby.xClans.commands.clan.args;

import me.jetby.treex.text.Colorize;
import me.jetby.xClans.TreexClans;
import me.jetby.xClans.commands.Subcommand;
import me.jetby.xClans.configurations.Lang;
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



        if (sender instanceof Player player) {
            if (args.length==0) {
                plugin.getLang().sendMessage(player, null, "commands.withdraw");
                return true;
            }
            if (plugin.getEconomy()==null) {
                player.sendMessage(Colorize.text(plugin.getLang().getConfig().getString("null-economy", "null-economy")));
                return true;
            }
            if (!plugin.getClanManager().isInClan(player.getUniqueId())) {
                plugin.getLang().sendMessage(player, null, "your-not-in-clan");
                return true;
            }

            Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());
            if (!clan.getMember(player.getUniqueId()).getRank().rankPermissions().deposit()) {
                plugin.getLang().sendMessage(player, clan, "your-rank-is-not-allowed-to-do-that");
                return true;
            }
            double balance = Double.parseDouble(args[0]);
            if (plugin.getClanManager().getBalance(clan)>=balance) {
                plugin.getEconomy().depositPlayer(player, balance);
                plugin.getClanManager().takeBalance(balance, clan);
                plugin.getLang().sendMessage(player, clan, "clan-balance-withdraw", new Lang.ReplaceString("{sum}", String.valueOf(balance)));
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
