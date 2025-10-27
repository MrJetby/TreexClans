package me.jetby.treexclans.commands.admin.subcommands;

import me.jetby.treexclans.commands.Subcommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// TODO: coin give <player> <amount>
// TODO: coin set <player> <amount>
// TODO: coin take <player> <amount>
// TODO: coin gvieall <clan> <amount>
// TODO: coin gvieall <amount>
public class CoinSubcommand implements Subcommand {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        return false;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }
}
