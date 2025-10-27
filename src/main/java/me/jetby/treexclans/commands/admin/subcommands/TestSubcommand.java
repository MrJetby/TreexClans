package me.jetby.treexclans.commands.admin.subcommands;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Member;
import me.jetby.treexclans.commands.Subcommand;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.functions.glow.Equipment;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TestSubcommand implements Subcommand {
    private final TreexClans plugin;

    public TestSubcommand(TreexClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {

        if (sender instanceof Player player) {

            if (plugin.getClanManager().isInClan(player.getUniqueId())) {
                Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());
                Member member = clan.getMember(player.getUniqueId());

                Color color = Equipment.getColorByName(args[0]);
                plugin.getClanManager().setColor(clan, member, color);
                if (plugin.getGlow().hasObserver(player)) {
                    plugin.getGlow().removeObserver(player);
                    plugin.getGlow().addObserver(player, clan.getMembers());
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
