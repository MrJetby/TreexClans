package me.jetby.treexclans.api.addons.example;

import me.jetby.treexclans.api.addons.commands.CommandService;
import me.jetby.treexclans.api.command.Subcommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TestCommand implements Subcommand {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        return false;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }

    @Override
    public CommandService.CommandType type() {
        return CommandService.CommandType.ADMIN;
    }
}
