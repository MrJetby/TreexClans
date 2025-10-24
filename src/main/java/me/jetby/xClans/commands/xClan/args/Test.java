package me.jetby.xClans.commands.xClan.args;

import me.jetby.xClans.TreexClans;
import me.jetby.xClans.commands.Subcommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Test implements Subcommand {
    private final TreexClans plugin;

    public Test(TreexClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {

        if (args[0].equalsIgnoreCase("id")) {
            sender.sendMessage(plugin.getRank().id());
            return true;
        }
        if (args[0].equalsIgnoreCase("name")) {
            sender.sendMessage(plugin.getRank().name());
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }
}
