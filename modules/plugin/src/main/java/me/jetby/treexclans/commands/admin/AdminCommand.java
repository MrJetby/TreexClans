package me.jetby.treexclans.commands.admin;

import me.jetby.treexclans.api.CustomCommandApi;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("clan.admin")) return true;
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /" + command.getName() + " <subcommand> [args]");
            return true;
        }
        try {
            var apiArg = CustomCommandApi.getSubcommands().get(args[0]);
            if (apiArg != null && apiArg.type() == CustomCommandApi.CommandType.ADMIN) {
                apiArg.onCommand(sender, Arrays.copyOfRange(args, 1, args.length));
                return true;
            }

            var arg = AdminCommandArgs.valueOf(args[0].toUpperCase());
            arg.getSubcommand().onCommand(sender, Arrays.copyOfRange(args, 1, args.length));
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cUnknown subcommand. Use /" + command.getName() + " for help.");
        }
        return true;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("clan.admin")) return null;
        List<String> completions = new ArrayList<>(Arrays.stream(AdminCommandArgs.values())
                .map(Enum::name)
                .map(String::toLowerCase)
                .toList());

        if (args.length == 1) {
            for (var cmd : AdminCommandArgs.values()) completions.add(cmd.name().toLowerCase());
            return completions;
        }
        try {
            var arg = AdminCommandArgs.valueOf(args[0].toUpperCase());
            return arg.getSubcommand().onTabCompleter(sender, command, s, args);
        } catch (IllegalArgumentException e) {
            return completions;
        }
    }
}
