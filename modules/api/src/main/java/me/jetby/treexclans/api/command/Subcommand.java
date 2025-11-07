package me.jetby.treexclans.api.command;

import me.jetby.treexclans.api.addons.commands.CommandService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Subcommand {
    boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args);

    @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args);

    CommandService.CommandType type();
}
