package me.jetby.treexclans.commands.clan.subcommands;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.commands.Subcommand;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GlowSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (!plugin.getClanManager().isInClan(player.getUniqueId())) {
                plugin.getLang().sendMessage(player, null, "your-not-in-clan");
                return true;
            }

//            if (!plugin.isPacketInit()) {
//                player.sendMessage(Colorize.text(plugin.getLang().getConfig().getString("restart-needed", "restart-needed")));
//                return true;
//            }
            Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());
            if (plugin.getGlow().hasObserver(player)) {
                plugin.getGlow().removeObserver(player);
                sender.sendMessage("§cClan glow disabled.");
            } else {
                Set<Member> members = new HashSet<>(clan.getMembers());
                if (clan.getMember(player.getUniqueId())!=clan.getLeader()) {
                    members.add(clan.getLeader());
                }
                members.remove(clan.getMember(player.getUniqueId()));
                plugin.getGlow().addObserver(player, members);
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
