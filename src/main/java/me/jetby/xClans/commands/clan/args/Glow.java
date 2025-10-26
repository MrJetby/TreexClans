package me.jetby.xClans.commands.clan.args;

import me.jetby.treex.text.Colorize;
import me.jetby.xClans.TreexClans;
import me.jetby.xClans.commands.Subcommand;
import me.jetby.xClans.records.Clan;
import me.jetby.xClans.records.Member;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Glow implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (!plugin.getClanManager().isInClan(player.getUniqueId())) {
                plugin.getLang().sendMessage(player, null, "your-not-in-clan");
                return true;
            }

            if (!plugin.isPacketInit()) {
                player.sendMessage(Colorize.text(plugin.getLang().getConfig().getString("restart-need", "restart-needed")));
                return true;
            }
            Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());
            if (plugin.getClanGlow().hasObserver(player)) {
                plugin.getClanGlow().removeObserver(player);
                sender.sendMessage("§cClan glow disabled.");
            } else {
                Set<Member> members = new HashSet<>(clan.getMembers());
                if (clan.getMember(player.getUniqueId())!=clan.getLeader()) {
                    members.add(clan.getLeader());
                }
                members.remove(clan.getMember(player.getUniqueId()));
                plugin.getClanGlow().addObserver(player, members);
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
