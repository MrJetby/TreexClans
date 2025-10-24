package me.jetby.xClans.commands.xClan;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XClanCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("xclan.admin")) return true;
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /xclan <subcommand> [args]");
            return true;
        }
        try {
            var arg = XClanCommandArgs.valueOf(args[0].toUpperCase());
            arg.getSubcommand().onCommand(sender, Arrays.copyOfRange(args, 1, args.length));
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cUnknown subcommand. Use /xclan for help.");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("xclan.admin")) return null;
        if (args.length == 1) {
            List<String> result = new ArrayList<>();
            for (var cmd : XClanCommandArgs.values()) result.add(cmd.name().toLowerCase());
            return result;
        }
        try {
            var arg = XClanCommandArgs.valueOf(args[0].toUpperCase());
            return arg.getSubcommand().onTabCompleter(sender, command, s, args);
        } catch (IllegalArgumentException e) { return List.of(); }
    }
}
