package me.jetby.treexclans.commands.clan.subcommands;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.CustomCommandApi;
import me.jetby.treexclans.commands.Subcommand;
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
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                plugin.getLang().sendMessage(player, null, "your-not-in-clan");
                return true;
            }
            var clanImpl = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());
            if (clanImpl.getLeader().equals(clanImpl.getMember(player.getUniqueId()))) {
                player.sendMessage(plugin.getLang().getMessage("you-cant-leave-leader"));
                return true;
            }

            clanImpl.removeMember(clanImpl.getMember(player.getUniqueId()));
            plugin.getLang().sendMessage(player, clanImpl, "clan-leave",
                    new Lang.ReplaceString("{player}", player.getName()),
                    new Lang.ReplaceString("{clan}", clanImpl.getId())
            );
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
