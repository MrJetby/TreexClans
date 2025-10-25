package me.jetby.xClans.commands.clan.args;

import me.jetby.xClans.TreexClans;
import me.jetby.xClans.commands.Subcommand;
import me.jetby.xClans.records.Clan;
import me.jetby.xClans.tools.Cooldown;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Disband implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {

        if (sender instanceof Player player) {
            if (!plugin.getClanManager().isInClan(player.getUniqueId())) {
                player.sendMessage(plugin.getLang().getMessage("your-not-in-clan"));
                return true;
            }
            if (Cooldown.isOnCooldown("delete_" + player.getUniqueId())) {
                Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());
                if (clan.getLeader().getUuid().equals(player.getUniqueId())) {
                    plugin.getClanManager().deleteClan(clan);
                    player.sendMessage("Your clan was successfully disbanded");
                }
                return true;
            } else {
                Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());
                if (clan.getLeader().getUuid().equals(player.getUniqueId())) {
                    Cooldown.setCooldown("delete_"+player.getUniqueId(), 10);
                    player.sendMessage("§c§lPlease confirm that you want to disband your clan by writing the command again in 10 seconds.");
                }
                return true;
            }

        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }
}
