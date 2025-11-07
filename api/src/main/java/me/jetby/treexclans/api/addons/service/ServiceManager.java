package me.jetby.treexclans.api.addons.service;

import me.jetby.treexclans.api.addons.AddonManager;
import me.jetby.treexclans.api.addons.commands.CommandService;
import me.jetby.treexclans.api.addons.configuration.ServiceConfiguration;
import me.jetby.treexclans.api.service.ClanManager;
import me.jetby.treexclans.api.service.leaderboard.LeaderboardService;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public interface ServiceManager {

    JavaPlugin getPlugin();
    File getDataFolder();

    Economy getEconomy();
    ClanManager getClanManager();
    LeaderboardService getLeaderboardService();
    CommandService getCommandService();

    AddonManager getAddonManager();
    ServiceConfiguration getServiceConfiguration();

}
