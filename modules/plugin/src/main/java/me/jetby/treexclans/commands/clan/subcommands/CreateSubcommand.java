package me.jetby.treexclans.commands.clan.subcommands;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.CustomCommandApi;
import me.jetby.treexclans.api.service.ClanManager;
import me.jetby.treexclans.commands.Subcommand;
import me.jetby.treexclans.configurations.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CreateSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();
    private final ClanManager clanManagerImpl = plugin.getClanManager();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player player) {

            if (clanManagerImpl.lookup().isInClan(player.getUniqueId())) {
                plugin.getLang().sendMessage(player, null, "commands.create");
                return true;
            } else {
                if (args.length < 1) {
                    sender.sendMessage("§cUsage: /clan create <clanName>");
                    return true;
                }
                String clanName = args[0].toLowerCase();
                if (clanManagerImpl.lifecycle().clanExists(clanName)) {
                    plugin.getLang().sendMessage(player, null, "clan-is-already-exists");
                    return true;
                }
                if (!clanManagerImpl.validation().isAllowedName(player, clanName)) {
                    return true;
                }

                if (clanManagerImpl.lifecycle().createClan(clanName, player)) {
                    var clanImpl = plugin.getClanManager().lookup().getClan(clanName);
                    plugin.getLang().sendMessage(player, clanImpl, "clan-create", new Lang.ReplaceString("{clan}", clanName));
                }
            }

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
