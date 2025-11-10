package me.jetby.treexclans.commands.clan.subcommands;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.addons.commands.CommandService;;
import me.jetby.treexclans.api.service.clan.member.Member;
import me.jetby.treexclans.api.command.Subcommand;
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
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                plugin.getMessages().sendMessage(player, null, "your-not-in-clan");
                return true;
            }

//            if (!plugin.isPacketInit()) {
//                player.sendMessage(Colorize.text(plugin.getLang().getConfig().getString("restart-needed", "restart-needed")));
//                return true;
//            }
            var clanImpl = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());
            if (plugin.getGlow().hasObserver(player)) {
                plugin.getGlow().removeObserver(player);
                plugin.getMessages().sendMessage(player, clanImpl, "clan-glow-off");
            } else {
                Set<Member> memberImpls = new HashSet<>(clanImpl.getMembers());
                if (clanImpl.getMember(player.getUniqueId()) != clanImpl.getLeader()) {
                    memberImpls.add(clanImpl.getLeader());
                }
                memberImpls.remove(clanImpl.getMember(player.getUniqueId()));
                plugin.getGlow().addObserver(player, memberImpls);
                plugin.getMessages().sendMessage(player, clanImpl, "clan-glow-on");
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
