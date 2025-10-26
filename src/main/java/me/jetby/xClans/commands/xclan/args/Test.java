package me.jetby.xClans.commands.xclan.args;

import me.jetby.xClans.TreexClans;
import me.jetby.xClans.commands.Subcommand;
import me.jetby.xClans.gui.GuiFactory;
import me.jetby.xClans.records.Clan;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

        if (sender instanceof Player player) {
            Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());
//            GuiFactory.create(plugin, , player, clan).open(player);
        }

        return true;

    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }
}
