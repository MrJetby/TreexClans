package me.jetby.treexclans.commands.clan.subcommands;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.addons.commands.CommandService;
import me.jetby.treexclans.api.command.Subcommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ChatSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                plugin.getLang().sendMessage(player, null, "your-not-in-clan");
                return true;
            }

            if (args.length == 0) {
                var memberImpl = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId()).getMember(player.getUniqueId());
                if (!memberImpl.isChat()) {
                    plugin.getLang().sendMessage(player, null, "clan-chat-on");
                    memberImpl.setChat(true);
                } else {
                    plugin.getLang().sendMessage(player, null, "clan-chat-off");
                    memberImpl.setChat(false);
                }
                return true;
            } else {
                StringBuilder message = new StringBuilder();
                for (String str : args) message.append(str).append(" ");
                var clanImpl = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());

                plugin.getClanManager().chat().sendChat(clanImpl, player, message.toString());
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
