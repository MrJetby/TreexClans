package me.jetby.treexclans.commands.admin.subcommands;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.addons.commands.CommandService;
import me.jetby.treexclans.api.command.Subcommand;
import me.jetby.treexclans.api.gui.Gui;
import me.jetby.treexclans.api.gui.Menu;
import me.jetby.treexclans.gui.core.ChestGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StorageSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {

        if (sender instanceof Player player) {

            Menu menu = plugin.getGuiLoader().getMenus().values().stream()
                    .filter(m -> m.type().equalsIgnoreCase("chest"))
                    .findFirst()
                    .orElse(null);
            if (menu != null) {

                if (args.length > 0) {
                    String clanName = args[0].toLowerCase();
                    var clanImpl = plugin.getClanManager().lookup().getClan(clanName);
                    if (clanImpl == null) return true;

                    Gui gui = new ChestGui(plugin, menu, player, clanImpl);
                    gui.open(player);

                    return true;
                } else {
                    sender.sendMessage("/xclan storage <clan>");
                }

            }
        }


        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        List<String> completions = new ArrayList<>(plugin.getCfg().getClans().keySet());

        return completions.stream()
                .filter(cmd -> cmd.startsWith(args[1].toLowerCase()))
                .toList();

    }

    @Override
    public CommandService.CommandType type() {
        return CommandService.CommandType.ADMIN;
    }
}
