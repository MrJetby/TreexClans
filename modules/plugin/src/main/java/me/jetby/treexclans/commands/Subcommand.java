package me.jetby.treexclans.commands;

import me.jetby.treexclans.api.CustomCommandApi;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Subcommand {
    boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args);

    @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args);

    CustomCommandApi.CommandType type();
}
