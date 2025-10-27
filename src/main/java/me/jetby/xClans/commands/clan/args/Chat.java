package me.jetby.xClans.commands.clan.args;

import me.jetby.xClans.TreexClans;
import me.jetby.xClans.commands.Subcommand;
import me.jetby.xClans.clan.Clan;
import me.jetby.xClans.clan.Member;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Chat implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (!plugin.getClanManager().isInClan(player.getUniqueId())) {
                plugin.getLang().sendMessage(player, null, "your-not-in-clan");
                return true;
            }

            if (args.length==0) {
                Member member = plugin.getClanManager().getClanByMember(player.getUniqueId()).getMember(player.getUniqueId());
                if (!member.isChat()) {
                    plugin.getLang().sendMessage(player, null, "clan-chat-on");
                    member.setChat(true);
                } else {
                    plugin.getLang().sendMessage(player, null, "clan-chat-off");
                    member.setChat(false);
                }
                return true;
            } else {
                StringBuilder message = new StringBuilder();
                for (String str : args) message.append(str).append(" ");
                Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());

                plugin.getClanManager().sendChat(clan, player, message.toString());
            }


        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }
}
