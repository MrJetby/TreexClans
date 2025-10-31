package me.jetby.treexclans.commands.clan.subcommands;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.commands.Subcommand;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.configurations.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LeaveSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (!plugin.getClanManager().isInClan(player.getUniqueId())) {
                plugin.getLang().sendMessage(player, null, "your-not-in-clan");
                return true;
            }
            Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());
            if (clan.getLeader().equals(clan.getMember(player.getUniqueId()))) {
                player.sendMessage(plugin.getLang().getMessage("you-cant-leave-leader"));
                return true;
            }

            plugin.getLang().sendMessage(player, clan, "clan-leave",
                    new Lang.ReplaceString("{player}", player.getName()),
                    new Lang.ReplaceString("{clan}", clan.getId())
            );
            clan.removeMember(clan.getMember(player.getUniqueId()));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }
}
