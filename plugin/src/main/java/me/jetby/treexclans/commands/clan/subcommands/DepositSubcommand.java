package me.jetby.treexclans.commands.clan.subcommands;

import me.jetby.treex.text.Colorize;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.addons.commands.CommandService;
;
import me.jetby.treexclans.api.service.clan.member.rank.RankPerms;
import me.jetby.treexclans.api.command.Subcommand;
import me.jetby.treexclans.configurations.Messages;
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
                plugin.getMessages().sendMessage(player, null, "commands.deposit");
                return true;
            }
            if (plugin.getEconomy() == null) {
                player.sendMessage(Colorize.text(plugin.getMessages().getConfig().getString("null-economy", "null-economy")));
                return true;
            }
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                plugin.getMessages().sendMessage(player, null, "your-not-in-clan");
                return true;
            }

            var clanImpl = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());
            if (!clanImpl.getMember(player.getUniqueId()).getRank().perms().contains(RankPerms.DEPOSIT)) {
                plugin.getMessages().sendMessage(player, clanImpl, "your-rank-is-not-allowed-to-do-that");
                return true;
            }
            double balance = Double.parseDouble(args[0]);
            if (plugin.getEconomy().has(player, balance)) {
                if (clanImpl.getLevel().maxBalance() < balance || clanImpl.getBalance() > balance) {
                    plugin.getMessages().sendMessage(player, clanImpl, "clan-balance-limit",
                            new Messages.ReplaceString("{sum}", String.valueOf(balance)),
                            new Messages.ReplaceString("{max-balance}", String.valueOf(clanImpl.getLevel().maxBalance()))
                    );
                    return true;
                }
                plugin.getEconomy().withdrawPlayer(player, balance);
                plugin.getClanManager().economy().addBalance(balance, clanImpl);
                plugin.getMessages().sendMessage(player, clanImpl, "clan-balance-deposit", new Messages.ReplaceString("{sum}", String.valueOf(balance)));
            } else {
                plugin.getMessages().sendMessage(player, clanImpl, "clan-deposit-no-money", new Messages.ReplaceString("{sum}", String.valueOf(balance)));
            }

        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }

    @Override
    public CommandService.CommandType type() {
        return CommandService.CommandType.CLAN;
    }
}
