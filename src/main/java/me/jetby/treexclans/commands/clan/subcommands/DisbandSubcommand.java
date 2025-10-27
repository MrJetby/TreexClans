package me.jetby.treexclans.commands.clan.subcommands;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.commands.Subcommand;
import me.jetby.treexclans.tools.Cooldown;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DisbandSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {

        if (sender instanceof Player player) {
            if (!plugin.getClanManager().isInClan(player.getUniqueId())) {
                plugin.getLang().sendMessage(player, null, "your-not-in-clan");
                return true;
            }
            Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());
            if (Cooldown.isOnCooldown("delete_" + player.getUniqueId())) {
                if (clan.getLeader().getUuid().equals(player.getUniqueId())) {
                    plugin.getLang().sendMessage(player, clan, "clan-disband");
                    plugin.getClanManager().deleteClan(clan);
                }
            } else {
                if (clan.getLeader().getUuid().equals(player.getUniqueId())) {
                    plugin.getLang().sendMessage(player, clan, "clan-disband-confirm");
                    Cooldown.setCooldown("delete_" + player.getUniqueId(), 10);
                }
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
