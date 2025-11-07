package me.jetby.treexclans.commands.clan.subcommands;

import me.jetby.treex.text.Colorize;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.CustomCommandApi;
import me.jetby.treexclans.api.service.clan.member.rank.RankPerms;
import me.jetby.treexclans.commands.Subcommand;
import me.jetby.treexclans.configurations.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DepositSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {


        if (sender instanceof Player player) {
            if (args.length == 0) {
                plugin.getLang().sendMessage(player, null, "commands.deposit");
                return true;
            }
            if (plugin.getEconomy() == null) {
                player.sendMessage(Colorize.text(plugin.getLang().getConfig().getString("null-economy", "null-economy")));
                return true;
            }
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                plugin.getLang().sendMessage(player, null, "your-not-in-clan");
                return true;
            }

            var clanImpl = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());
            if (!clanImpl.getMember(player.getUniqueId()).getRank().perms().contains(RankPerms.DEPOSIT)) {
                plugin.getLang().sendMessage(player, clanImpl, "your-rank-is-not-allowed-to-do-that");
                return true;
            }
            double balance = Double.parseDouble(args[0]);
            if (plugin.getEconomy().has(player, balance)) {
                if (clanImpl.getLevel().maxBalance() < balance || clanImpl.getBalance() > balance) {
                    plugin.getLang().sendMessage(player, clanImpl, "clan-balance-limit",
                            new Lang.ReplaceString("{sum}", String.valueOf(balance)),
                            new Lang.ReplaceString("{max-balance}", String.valueOf(clanImpl.getLevel().maxBalance()))
                    );
                    return true;
                }
                plugin.getEconomy().withdrawPlayer(player, balance);
                plugin.getClanManager().economy().addBalance(balance, clanImpl);
                plugin.getLang().sendMessage(player, clanImpl, "clan-balance-deposit", new Lang.ReplaceString("{sum}", String.valueOf(balance)));
            } else {
                plugin.getLang().sendMessage(player, clanImpl, "clan-deposit-no-money", new Lang.ReplaceString("{sum}", String.valueOf(balance)));
            }

        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }

    @Override
    public CustomCommandApi.CommandType type() {
        return CustomCommandApi.CommandType.CLAN;
    }
}
