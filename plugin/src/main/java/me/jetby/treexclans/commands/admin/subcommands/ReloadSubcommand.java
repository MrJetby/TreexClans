package me.jetby.treexclans.commands.admin.subcommands;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.addon.AddonManagerImpl;
import me.jetby.treexclans.api.addons.commands.CommandService;
import me.jetby.treexclans.api.command.Subcommand;
import me.jetby.treexclans.api.service.clan.Clan;
import me.jetby.treexclans.commands.clan.ClanCommand;
import me.jetby.treexclans.configurations.Config;
import me.jetby.treexclans.configurations.QuestsLoader;
import me.jetby.treexclans.gui.GuiLoader;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ReloadSubcommand implements Subcommand {
    private final TreexClans plugin;

    public ReloadSubcommand(TreexClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {

        try {
            long start = System.currentTimeMillis();

            plugin.getStorage().save();
            Map<String, Clan> clans = plugin.getCfg().getClans();

            Config cfg = new Config();
            cfg.load();
            cfg.setClans(clans);
            plugin.setCfg(cfg);

            GuiLoader guiLoader = new GuiLoader(plugin, plugin.getDataFolder());
            guiLoader.load();

            plugin.setGuiLoader(guiLoader);
            if (plugin.getClanCommand() != null) {
                ClanCommand cmd = new ClanCommand(plugin);
                plugin.getClanCommand().setExecutor(cmd);
                plugin.getClanCommand().setTabCompleter(cmd);
            }

            QuestsLoader questsLoader = new QuestsLoader();
            questsLoader.load();
            plugin.setQuestsLoader(questsLoader);
            plugin.getAddonManager().disableAddons();
            ((AddonManagerImpl) plugin.getAddonManager()).loadAddons();

            plugin.getStorage().load();

            sender.sendMessage("Reloaded by " + (System.currentTimeMillis() - start) + " ms");
        } catch (Exception e) {
            sender.sendMessage(e.getMessage());
        }
        return true;

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
