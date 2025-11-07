package me.jetby.treexclans.commands.clan.subcommands;

import me.jetby.treexclans.api.addons.commands.CommandService;;
import me.jetby.treexclans.api.command.Subcommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ListSubcommand implements Subcommand {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {

        return true;
    }

    @Override
    public java.util.@Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return java.util.List.of();
    }

    @Override
    public CommandService.CommandType type() {
        return CommandService.CommandType.CLAN;
    }
}
