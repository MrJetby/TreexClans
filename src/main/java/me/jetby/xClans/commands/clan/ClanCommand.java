package me.jetby.xClans.commands.clan;

import me.jetby.treex.text.Colorize;
import me.jetby.xClans.TreexClans;
import me.jetby.xClans.gui.GuiFactory;
import me.jetby.xClans.gui.GuiType;
import me.jetby.xClans.gui.Menu;
import me.jetby.xClans.clan.Clan;
import me.jetby.xClans.clan.Member;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClanCommand implements CommandExecutor, TabCompleter {
    private final TreexClans plugin;
    private final Map<String, List<String>> menuArgs = new HashMap<>();

    public ClanCommand(TreexClans plugin) {
        this.plugin = plugin;
        plugin.getMenuLoader().getMenus().forEach((key, item) -> menuArgs.put(key, item.openArgs()));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {

            Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());
            if (clan!=null) {
                for (Map.Entry<String, List<String>> entry : menuArgs.entrySet()) {
                    if (entry.getValue().contains(args[0])) {
                        GuiFactory.create(plugin, plugin.getMenuLoader().getMenus().get(entry.getKey()), player, clan).open(player);
                        return true;
                    }
                }
            }
            if (args.length < 1) {
                if (!plugin.getClanManager().isInClan(player.getUniqueId())) {
                    for (String str : plugin.getLang().getConfig().getStringList("commands.help-no-clan")) {
                        sender.sendMessage(Colorize.text(str));
                    }
                } else {
                    for (String str : plugin.getLang().getConfig().getStringList("commands.help")) {
                        sender.sendMessage(Colorize.text(str));
                    }
                }

                return true;
            }
        }


        try {
            var arg = ClanCommandArgs.valueOf(args[0].toUpperCase());
            arg.getSubcommand().onCommand(sender, Arrays.copyOfRange(args, 1, args.length));
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cUnknown command. Use /clan for help.");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player player)) return List.of();

            List<String> completions = Arrays.stream(ClanCommandArgs.values())
                    .map(Enum::name)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());

            for (Map.Entry<String, List<String>> entry : menuArgs.entrySet()) {
                if (entry.getValue().contains(args[0])) {
                    Menu menu = plugin.getMenuLoader().getMenus().get(entry.getKey());
                    if (menu.type()==GuiType.DEFAULT) {
                        if (player.hasPermission(menu.permission())) {
                            completions.addAll(entry.getValue());
                        }
                    }
                }
            }

            if (!plugin.getClanManager().isInClan(player.getUniqueId())) {
                return completions.stream()
                        .filter(cmd -> cmd.equals("create") || cmd.equals("accept"))
                        .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                        .toList();
            }
            Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());
            Member member = clan.getMember(player.getUniqueId());

            if (member == null || member.getRank() == null)
                return List.of();

            var perms = member.getRank().rankPermissions();

            completions.removeIf(cmd -> switch (cmd) {
                case "setbase" -> !perms.setbase();
                case "base" -> !perms.base();
                case "invite" -> !perms.invite();
                case "withdraw" -> !perms.withdraw() || plugin.getEconomy()==null;
                case "deposit", "invest" -> !perms.deposit() || plugin.getEconomy()==null;
                case "kick" -> !perms.kick();
                case "pvp" -> !perms.pvp();
                default -> false;
            });
            completions.remove("create");
            completions.remove("setrank");
            for (Map.Entry<String, List<String>> entry : menuArgs.entrySet()) {
                Menu menu = plugin.getMenuLoader().getMenus().get(entry.getKey());
                if (menu.type() != GuiType.DEFAULT && player.hasPermission(menu.permission())) {
                    completions.addAll(entry.getValue().stream()
                            .filter(str -> str.toLowerCase().startsWith(args[0].toLowerCase()))
                            .toList());
                }
            }


            return completions.stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        try {
            var arg = ClanCommandArgs.valueOf(args[0].toUpperCase());
            return arg.getSubcommand().onTabCompleter(sender, command, s, args);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }


}
