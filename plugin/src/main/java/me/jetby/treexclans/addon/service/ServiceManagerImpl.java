package me.jetby.treexclans.addon.service;

import lombok.Getter;
import me.jetby.treexclans.addon.configuration.ServiceConfigurationImpl;
import me.jetby.treexclans.api.TreexClansAPI;
import me.jetby.treexclans.api.addons.AddonManager;
import me.jetby.treexclans.api.addons.annotations.ClanAddon;
import me.jetby.treexclans.api.addons.commands.CommandService;
import me.jetby.treexclans.api.addons.configuration.ServiceConfiguration;
import me.jetby.treexclans.api.addons.service.ServiceManager;
import me.jetby.treexclans.api.service.ClanManager;
import me.jetby.treexclans.api.service.leaderboard.LeaderboardService;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

@Getter
public class ServiceManagerImpl implements ServiceManager {

    private final JavaPlugin plugin;
    private final File dataFolder;

    private final Economy economy;
    private final ClanManager clanManager;
    private final LeaderboardService leaderboardService;
    private final CommandService commandService;

    private final AddonManager addonManager;
    private final ServiceConfiguration serviceConfiguration;
    private final ClanAddon addon;

    public ServiceManagerImpl(AddonManager addonManager, File dataFolder, JavaPlugin plugin, ClanAddon addon) {
        this.plugin = plugin;
        this.dataFolder = new File(dataFolder, addon.id());
        if (!dataFolder.exists()) dataFolder.mkdirs();

        var treex = (TreexClansAPI) plugin;
        this.economy = treex.getEconomy();
        this.clanManager = treex.getClanManager();
        this.leaderboardService = treex.getLeaderboardService();
        this.commandService = treex.getCommandService();

        this.addonManager = addonManager;
        this.serviceConfiguration = new ServiceConfigurationImpl(this);
        this.addon = addon;
    }
}
