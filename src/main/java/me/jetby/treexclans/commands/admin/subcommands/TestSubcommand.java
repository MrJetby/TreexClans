package me.jetby.treexclans.commands.admin.subcommands;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.commands.Subcommand;
import me.jetby.treexclans.functions.war.War;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TestSubcommand implements Subcommand {
    private final TreexClans plugin;

    public TestSubcommand(TreexClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {


        return true;

    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }
}
