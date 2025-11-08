package me.jetby.treexclans.api.addons.service;

import me.jetby.treexclans.api.addons.AddonManager;
import me.jetby.treexclans.api.addons.commands.CommandService;
import me.jetby.treexclans.api.addons.configuration.ServiceConfiguration;
import me.jetby.treexclans.api.addons.listener.EventRegistrar;
import me.jetby.treexclans.api.gui.GuiFactory;
import me.jetby.treexclans.api.service.ClanManager;
import me.jetby.treexclans.api.service.leaderboard.LeaderboardService;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Provides access to the core TreexClans service layer.
 * <p>
 * This manager acts as the main service hub, giving addons
 * access to core components such as clans, leaderboards,
 * GUI management, commands, and configuration utilities.
 * </p>
 *
 * <p>
 * Every TreexClans addon can obtain a reference to this
 * interface through its {@code JavaAddon#getServiceManager()}
 * method to interact with the plugin ecosystem.
 * </p>
 */
public interface ServiceManager {

    /**
     * Gets the root TreexClans plugin instance.
     * <p>
     * Useful for interacting with the Bukkit API, logging,
     * or scheduling asynchronous tasks.
     * </p>
     *
     * @return The active {@link JavaPlugin} instance.
     */
    JavaPlugin getPlugin();

    /**
     * Gets the data folder of the current addon.
     * <p>
     * Each addon has its own isolated directory inside
     * the TreexClans addons folder. This folder can be
     * used to store configuration files or local data.
     * </p>
     *
     * @return The addon's dedicated data directory.
     */
    File getDataFolder();

    /**
     * Provides access to the registered Vault economy provider.
     * <p>
     * Returns {@code null} if Vault or an economy plugin
     * is not installed on the server.
     * </p>
     *
     * @return The {@link Economy} service or {@code null}.
     */
    Economy getEconomy();

    /**
     * Provides access to the clan management system.
     * <p>
     * Used to query, create, and modify clan data safely.
     * </p>
     *
     * @return The {@link ClanManager} instance.
     */
    ClanManager getClanManager();

    /**
     * Provides access to the leaderboard service.
     * <p>
     * Used to retrieve ranking information and
     * manage top clan statistics.
     * </p>
     *
     * @return The {@link LeaderboardService} instance.
     */
    LeaderboardService getLeaderboardService();

    /**
     * Provides access to the command management system.
     * <p>
     * Allows registration of commands dynamically
     * from within addons at runtime.
     * </p>
     *
     * @return The {@link CommandService} instance.
     */
    CommandService getCommandService();

    /**
     * Provides access to the GUI factory.
     * <p>
     * Used to create interactive menus and graphical
     * interfaces for players.
     * </p>
     *
     * @return The {@link GuiFactory} instance.
     */
    GuiFactory getGuiFactory();

    /**
     * Provides access to the addon manager.
     * <p>
     * Handles loading, enabling, disabling, and
     * unloading of all TreexClans addons.
     * </p>
     *
     * @return The {@link AddonManager} instance.
     */
    AddonManager getAddonManager();

    /**
     * Provides access to the global event registrar.
     *
     * <p>
     * Used to register and unregister Bukkit listeners for addons.
     * Each addon can manage its own event lifecycle safely through this service.
     * </p>
     *
     * @return the {@link EventRegistrar} instance
     */
    EventRegistrar getEventRegistrar();

    /**
     * Provides access to the configuration service.
     * <p>
     * Used to read and write configuration files,
     * both global and addon-specific.
     * </p>
     *
     * @return The {@link ServiceConfiguration} instance.
     */
    ServiceConfiguration getServiceConfiguration();
}
