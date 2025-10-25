package me.jetby.xClans.commands.clan.args;

import me.jetby.xClans.TreexClans;
import me.jetby.xClans.commands.Subcommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;

public class Glow implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (!plugin.getClanManager().isInClan(player.getUniqueId())) {
                sender.sendMessage("§cYou are not in a clan.");
                return true;
            }
            if (plugin.getClanGlow().hasObserver(player)) {
                plugin.getClanGlow().removeObserver(player);
                sender.sendMessage("§cClan glow disabled.");
            } else {
                plugin.getClanGlow().addObserver(player, new HashSet<>(plugin.getClanManager().getClanByMember(player.getUniqueId()).getMembers()));
                sender.sendMessage("§aClan glow enabled.");
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }
}
